package net.kamaarion.roacw.entity.armorset_summon.unstable_energy_cannon;

import io.redspace.ironsspellbooks.registries.ParticleRegistry;
import io.redspace.ironsspellbooks.registries.SoundRegistry;
import net.kamaarion.roacw.entity.projectile.energy_cannon_beam.EnergyCannonBeamEntity;
import net.kamaarion.roacw.registeries.ROACWEntityRegistry;
import net.kamaarion.roacw.registeries.ROACWSoundRegistry;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector4f;
import net.minecraftforge.network.NetworkHooks;
import net.minecraftforge.registries.ForgeRegistries;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.AnimationState;
import software.bernie.geckolib.core.animation.RawAnimation;
import software.bernie.geckolib.core.object.PlayState;
import software.bernie.geckolib.util.GeckoLibUtil;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Supplier;

public class UnstableEnergyCannonEntity
        extends Entity
        implements GeoEntity {

    private static final double CULLING_BOX_SIZE = 4.0D;

    private static final double SIDE_OFFSET = -1.70D;
    private static final double HEIGHT_OFFSET = 0.75D;
    private static final double FORWARD_OFFSET = 1.7D;

    private static final int SPAWN_PARTICLE_DURATION = 8;
    private static final int SPAWN_SMOKE_PARTICLES_PER_TICK = 20;
    private static final int SPAWN_ELECTRICITY_PARTICLES_PER_TICK = 10;

    private static final double SPAWN_SMOKE_RISE_SPEED = 0.02D;

    private static final double SPAWN_PARTICLE_WIDTH = 0.85D;
    private static final double SPAWN_PARTICLE_HEIGHT = 2.22D;
    private static final double SPAWN_PARTICLE_LENGTH = 3.80D;

    private static final double SPAWN_PARTICLE_Y_CENTER = 0.516D;
    private static final double SPAWN_PARTICLE_Z_CENTER = -0.368D;

    private static final int CHARGE_TICKS = 15;
    private static final int SHOOT_TICKS = 20;

    private static final int ATTACK_COOLDOWN_TICKS = 30;
    private static final int ABORTED_CHARGE_COOLDOWN_TICKS = 10;
    private static final int TARGET_SEARCH_INTERVAL_TICKS = 5;

    private static final int LOCK_GRACE_TICKS = 20;

    private static final Supplier<SoundEvent> CHARGE_SOUND =
            () -> ROACWSoundRegistry.ENERGY_CANNON_CHARGE.get();

    private static final Supplier<SoundEvent> FIRE_SOUND =
            () -> ROACWSoundRegistry.ENERGY_CANNON_FIRE.get();

    private static final float CHARGE_SOUND_VOLUME = 1.0F;
    private static final float FIRE_SOUND_VOLUME = 1.0F;
    private static final float SOUND_PITCH_VARIATION = 0.0F;

    private static final int SPAWN_ANIMATION_TICKS = 38;

    private static final int ANIMATION_TRANSITION_TICKS = 2;

    private static final double ATTACK_RANGE = 24.0D;

    private static final float AIM_TURN_SPEED_DEGREES_PER_TICK = 15.0F;
    public static final float AIM_MAX_PITCH_DEGREES = 40.0F;

    public static final float AIM_MAX_YAW_DEGREES = 90.0F;

    public static final float AIM_PIVOT_Y = 5.772F / 16.0F;

    private static final int OWNER_ATTACKED_MAX_AGE_TICKS = 100;

    private static final int OWNER_LAST_HURT_MAX_AGE_TICKS = 200;

    private static final boolean DEBUG_MUZZLE_PARTICLE = false;

    private static final double RENDER_SIDE_OFFSET = 2.2D;
    private static final double RENDER_HEIGHT_OFFSET = 0.95D;
    private static final double RENDER_FORWARD_OFFSET = -0.67D;
    private static final float RENDER_INWARD_ANGLE = 12.0F;

    private static final double MODEL_MUZZLE_Y = 5.772D / 16.0D;
    private static final double MODEL_MUZZLE_Z = -33.629D / 16.0D;

    private static final double BARREL_REAR_Z = 23.0D / 16.0D;
    private static final double PLAYER_CLEARANCE = 0.3D;
    private static final double LOCK_ACQUIRE_EXTRA_CLEARANCE = 0.25D;

    private static final byte STATE_IDLE = 0;
    private static final byte STATE_CHARGING = 1;
    private static final byte STATE_SHOOTING = 2;

    private static final RawAnimation SPAWN_ANIMATION =
            RawAnimation.begin()
                    .thenPlayAndHold("UnstableEnergyCannonSpawn");

    private static final RawAnimation IDLE_ANIMATION =
            RawAnimation.begin()
                    .thenLoop("UnstableEnergyCannonIdle");

    private static final RawAnimation CHARGEUP_ANIMATION =
            RawAnimation.begin()
                    .thenPlay("UnstableEnergyCannonChargeup");

    private static final RawAnimation SHOOT_ANIMATION =
            RawAnimation.begin()
                    .thenPlay("UnstableEnergyCannonShoot");

    private static final EntityDataAccessor<Optional<UUID>> DATA_OWNER_UUID =
            SynchedEntityData.defineId(
                    UnstableEnergyCannonEntity.class,
                    EntityDataSerializers.OPTIONAL_UUID
            );

    private static final EntityDataAccessor<Integer> DATA_LOCK_TARGET_ID =
            SynchedEntityData.defineId(
                    UnstableEnergyCannonEntity.class,
                    EntityDataSerializers.INT
            );

    private static final EntityDataAccessor<Float> DATA_AIM_YAW_OFFSET =
            SynchedEntityData.defineId(
                    UnstableEnergyCannonEntity.class,
                    EntityDataSerializers.FLOAT
            );

    private static final EntityDataAccessor<Float> DATA_AIM_PITCH =
            SynchedEntityData.defineId(
                    UnstableEnergyCannonEntity.class,
                    EntityDataSerializers.FLOAT
            );

    private static final EntityDataAccessor<Byte> DATA_ATTACK_STATE =
            SynchedEntityData.defineId(
                    UnstableEnergyCannonEntity.class,
                    EntityDataSerializers.BYTE
            );

    private final AnimatableInstanceCache cache =
            GeckoLibUtil.createInstanceCache(this);

    private int spawnParticleTicks = 0;
    private boolean spawnEffectStarted = false;

    private Matrix4f clientModelPose = null;
    private double clientModelOriginX = 0.0D;
    private double clientModelOriginY = 0.0D;
    private double clientModelOriginZ = 0.0D;

    private float visualAimYawOffset = 0.0F;
    private float visualAimPitch = 0.0F;
    private float visualAimLastTime = -1.0F;

    private float clientAimYawOffsetO = 0.0F;
    private float clientAimYawOffset = 0.0F;
    private float clientAimPitchO = 0.0F;
    private float clientAimPitch = 0.0F;

    private Vec3 clientMuzzlePending = null;
    private Vec3 clientMuzzleCurrent = null;
    private Vec3 clientMuzzlePrevious = null;

    private int attackStateTicks = 0;
    private int attackCooldown = 0;
    private UUID lockTargetUUID = null;
    private int lockLostTicks = 0;

    public UnstableEnergyCannonEntity(
            EntityType<? extends UnstableEnergyCannonEntity> entityType,
            Level level
    ) {
        super(entityType, level);

        this.noPhysics = true;
        this.setNoGravity(true);
        this.noCulling = true;
    }

    public UnstableEnergyCannonEntity(
            Level level,
            Player owner
    ) {
        this(
                ROACWEntityRegistry.UNSTABLE_ENERGY_CANNON.get(),
                level
        );

        this.setOwner(owner);

        this.updateAttachmentPosition(owner);
    }

    @Override
    protected void defineSynchedData() {
        this.entityData.define(
                DATA_OWNER_UUID,
                Optional.empty()
        );

        this.entityData.define(
                DATA_ATTACK_STATE,
                STATE_IDLE
        );

        this.entityData.define(
                DATA_LOCK_TARGET_ID,
                -1
        );

        this.entityData.define(
                DATA_AIM_YAW_OFFSET,
                0.0F
        );

        this.entityData.define(
                DATA_AIM_PITCH,
                0.0F
        );
    }

    @Override
    public Packet<ClientGamePacketListener> getAddEntityPacket() {
        return NetworkHooks.getEntitySpawningPacket(this);
    }

    public void setOwner(Player owner) {
        this.entityData.set(
                DATA_OWNER_UUID,
                Optional.of(owner.getUUID())
        );
    }

    public UUID getOwnerUUID() {
        return this.entityData
                .get(DATA_OWNER_UUID)
                .orElse(null);
    }

    public Player getOwner() {
        UUID ownerUUID = this.getOwnerUUID();

        if (ownerUUID == null) {
            return null;
        }

        return this.level().getPlayerByUUID(ownerUUID);
    }

    private byte getAttackState() {
        return this.entityData.get(DATA_ATTACK_STATE);
    }

    private void setAttackState(byte state) {
        this.entityData.set(DATA_ATTACK_STATE, state);
    }

    private void updateAttachmentPosition(Player owner) {

        double bodyYawRadians =
                Math.toRadians(owner.yBodyRot);

        Vec3 forward = new Vec3(
                -Math.sin(bodyYawRadians),
                0.0D,
                Math.cos(bodyYawRadians)
        );

        Vec3 left = new Vec3(
                Math.cos(bodyYawRadians),
                0.0D,
                Math.sin(bodyYawRadians)
        );

        Vec3 desiredPosition =
                new Vec3(
                        owner.getX(),
                        owner.getY(),
                        owner.getZ()
                )
                        .add(left.scale(SIDE_OFFSET))
                        .add(forward.scale(FORWARD_OFFSET))
                        .add(
                                0.0D,
                                HEIGHT_OFFSET,
                                0.0D
                        );

        this.setPos(
                desiredPosition.x,
                desiredPosition.y,
                desiredPosition.z
        );

        float bodyYaw = owner.yBodyRot;

        this.setYRot(bodyYaw);
        this.yRotO = bodyYaw;

        this.setXRot(0.0F);
        this.xRotO = 0.0F;

        this.setDeltaMovement(Vec3.ZERO);
        this.hasImpulse = false;
    }

    @Override
    public void tick() {
        super.tick();

        this.noPhysics = true;
        this.setNoGravity(true);

        this.setDeltaMovement(Vec3.ZERO);
        this.hasImpulse = false;

        Player owner = this.getOwner();

        if (owner == null || !owner.isAlive()) {
            if (!this.level().isClientSide) {
                this.discard();
            }

            return;
        }

        this.updateAttachmentPosition(owner);

        if (!this.level().isClientSide) {
            this.tickAim(owner);
            this.tickAttack(owner);
        } else {
            this.clientAimYawOffsetO = this.clientAimYawOffset;
            this.clientAimYawOffset =
                    this.entityData.get(DATA_AIM_YAW_OFFSET);

            this.clientAimPitchO = this.clientAimPitch;
            this.clientAimPitch =
                    this.entityData.get(DATA_AIM_PITCH);
        }

        if (this.level().isClientSide) {

            this.advanceClientMuzzleSample();

            if (DEBUG_MUZZLE_PARTICLE && this.tickCount % 10 == 0) {

                Vec3 debugSample =
                        this.getInterpolatedClientMuzzlePosition(1.0F);

                if (debugSample != null) {
                    this.level().addParticle(
                            ParticleTypes.END_ROD,
                            debugSample.x,
                            debugSample.y,
                            debugSample.z,
                            0.0D,
                            0.0D,
                            0.0D
                    );
                }

                Vec3 debugEstimate = this.getMuzzlePosition(owner);

                this.level().addParticle(
                        ParticleTypes.FLAME,
                        debugEstimate.x,
                        debugEstimate.y,
                        debugEstimate.z,
                        0.0D,
                        0.0D,
                        0.0D
                );
            }

            if (!this.spawnEffectStarted
                    && this.clientModelPose != null) {
                this.spawnEffectStarted = true;
                this.spawnParticleTicks =
                        SPAWN_PARTICLE_DURATION;
            }

            if (this.spawnParticleTicks > 0) {
                this.spawnParticleEffect();
                this.spawnParticleTicks--;
            }
        }
    }

    public void setClientMuzzleSample(Vec3 worldPosition) {
        this.clientMuzzlePending = worldPosition;
    }

    private void advanceClientMuzzleSample() {

        if (this.clientMuzzlePending == null) {
            return;
        }

        this.clientMuzzlePrevious =
                this.clientMuzzleCurrent != null
                        ? this.clientMuzzleCurrent
                        : this.clientMuzzlePending;

        this.clientMuzzleCurrent = this.clientMuzzlePending;
    }

    public Vec3 getInterpolatedClientMuzzlePosition(float partialTick) {

        if (this.clientMuzzleCurrent == null) {
            return null;
        }

        Vec3 previous =
                this.clientMuzzlePrevious != null
                        ? this.clientMuzzlePrevious
                        : this.clientMuzzleCurrent;

        return previous.lerp(this.clientMuzzleCurrent, partialTick);
    }

    private void tickAttack(Player owner) {

        if (this.tickCount < SPAWN_ANIMATION_TICKS) {
            return;
        }

        this.maintainLock(owner);

        switch (this.getAttackState()) {

            case STATE_CHARGING -> this.tickCharging(owner);

            case STATE_SHOOTING -> this.tickShooting(owner);

            default -> this.tickIdle(owner);
        }
    }

    private void tickIdle(Player owner) {

        if (this.attackCooldown > 0) {
            this.attackCooldown--;
            return;
        }

        if (this.tickCount < SPAWN_ANIMATION_TICKS) {
            return;
        }

        LivingEntity target = this.lookupLockTarget();

        if (target != null && this.canAimAt(owner, target, 0.0D)) {
            this.beginCharge();
        }
    }

    private void maintainLock(Player owner) {

        LivingEntity locked = this.lookupLockTarget();

        if (locked == null && this.lockTargetUUID != null) {
            this.setLockTarget(null);
        }

        if (this.tickCount % TARGET_SEARCH_INTERVAL_TICKS == 0
                && this.getAttackState() == STATE_IDLE) {

            LivingEntity attacker = this.findAttacker(owner);

            if (attacker != null
                    && (locked == null || attacker != locked)) {
                this.setLockTarget(attacker);
                this.lockLostTicks = 0;
                return;
            }
        }

        if (locked != null && !this.canAimAt(owner, locked, 0.0D)) {
            this.setLockTarget(null);
            this.lockLostTicks = 0;
            locked = null;
        }

        if (locked != null) {

            if (this.isValidTarget(owner, locked)) {
                this.lockLostTicks = 0;
                return;
            }

            if (this.lockLostTicks < LOCK_GRACE_TICKS) {
                this.lockLostTicks++;
                return;
            }

            this.setLockTarget(null);
            this.lockLostTicks = 0;
        }

        if (this.tickCount % TARGET_SEARCH_INTERVAL_TICKS != 0) {
            return;
        }

        LivingEntity found = this.findTarget(owner);

        if (found != null) {
            this.setLockTarget(found);
            this.lockLostTicks = 0;
        }
    }

    private void setLockTarget(LivingEntity target) {

        this.lockTargetUUID =
                target != null ? target.getUUID() : null;

        this.entityData.set(
                DATA_LOCK_TARGET_ID,
                target != null ? target.getId() : -1
        );
    }

    private void beginCharge() {
        this.attackStateTicks = 0;

        this.setAttackState(STATE_CHARGING);

        this.playAttackSound(CHARGE_SOUND, CHARGE_SOUND_VOLUME);
    }

    private void tickCharging(Player owner) {

        LivingEntity target = this.lookupLockTarget();

        if (target == null || !this.canAimAt(owner, target, 0.0D)) {
            this.endAttack(ABORTED_CHARGE_COOLDOWN_TICKS);
            return;
        }

        this.attackStateTicks++;

        if (this.attackStateTicks >= CHARGE_TICKS) {
            this.beginShoot(target);
        }
    }

    private void beginShoot(LivingEntity target) {
        this.attackStateTicks = 0;

        this.setAttackState(STATE_SHOOTING);

        this.playAttackSound(FIRE_SOUND, FIRE_SOUND_VOLUME);

        this.fireBeam(target);
    }

    private void tickShooting(Player owner) {

        this.attackStateTicks++;

        if (this.attackStateTicks >= SHOOT_TICKS) {
            this.endAttack(ATTACK_COOLDOWN_TICKS);
        }
    }

    private void endAttack(int cooldownTicks) {
        this.attackStateTicks = 0;
        this.attackCooldown = cooldownTicks;

        this.setAttackState(STATE_IDLE);
    }

    private void playAttackSound(Supplier<SoundEvent> sound, float volume) {

        float pitch =
                1.0F
                        + (this.random.nextFloat() - 0.5F)
                        * SOUND_PITCH_VARIATION;

        this.level().playSound(
                null,
                this.getX(),
                this.getY(),
                this.getZ(),
                sound.get(),
                SoundSource.PLAYERS,
                volume,
                pitch
        );
    }

    private void fireBeam(LivingEntity target) {

        Player owner = this.getOwner();

        if (owner == null) {
            return;
        }

        Vec3 muzzle = this.getMuzzlePosition(owner);

        Vec3 targetPoint =
                target.position().add(
                        0.0D,
                        target.getBbHeight() * 0.5D,
                        0.0D
                );

        Vec3 direction = targetPoint.subtract(muzzle);

        if (direction.lengthSqr() < 0.000001D) {
            return;
        }

        EnergyCannonBeamEntity beam =
                new EnergyCannonBeamEntity(
                        ROACWEntityRegistry.ENERGY_CANNON_BEAM.get(),
                        this.level()
                );

        beam.setupBeam(
                this,
                muzzle,
                direction,
                targetPoint
        );

        this.level().addFreshEntity(beam);
    }

    private Vec3 getModelOrigin(Player owner) {

        double yawRadians = Math.toRadians(owner.yBodyRot);

        Vec3 forward = new Vec3(
                -Math.sin(yawRadians),
                0.0D,
                Math.cos(yawRadians)
        );

        Vec3 right = new Vec3(
                -Math.cos(yawRadians),
                0.0D,
                -Math.sin(yawRadians)
        );

        return owner.position()
                .add(right.scale(RENDER_SIDE_OFFSET))
                .add(forward.scale(RENDER_FORWARD_OFFSET))
                .add(0.0D, RENDER_HEIGHT_OFFSET, 0.0D);
    }

    private Vec3 aimedModelPoint(
            Player owner,
            float aimYawOffset,
            float aimPitch,
            double y,
            double z
    ) {
        float barrelYaw =
                owner.yBodyRot - RENDER_INWARD_ANGLE + aimYawOffset;

        Matrix4f transform =
                new Matrix4f()
                        .rotateY((float) Math.toRadians(-barrelYaw))
                        .translate(0.0F, AIM_PIVOT_Y, 0.0F)
                        .rotateX((float) Math.toRadians(aimPitch))
                        .translate(0.0F, -AIM_PIVOT_Y, 0.0F)
                        .rotateY((float) Math.PI);

        Vector3f point = new Vector3f(0.0F, (float) y, (float) z);

        transform.transformPosition(point);

        return this.getModelOrigin(owner).add(point.x, point.y, point.z);
    }

    private Vec3 getMuzzlePosition(Player owner) {
        return this.aimedModelPoint(
                owner,
                this.entityData.get(DATA_AIM_YAW_OFFSET),
                this.entityData.get(DATA_AIM_PITCH),
                MODEL_MUZZLE_Y,
                MODEL_MUZZLE_Z
        );
    }

    private void tickAim(Player owner) {

        float targetYawOffset = 0.0F;
        float targetPitch = 0.0F;

        LivingEntity target = this.lookupLockTarget();

        if (target != null) {

            float[] aim = this.computeAimAngles(owner, target);

            targetYawOffset =
                    Mth.clamp(
                            aim[0],
                            -AIM_MAX_YAW_DEGREES,
                            AIM_MAX_YAW_DEGREES
                    );

            targetPitch =
                    Mth.clamp(
                            aim[1],
                            -AIM_MAX_PITCH_DEGREES,
                            AIM_MAX_PITCH_DEGREES
                    );
        }

        float currentYaw = this.entityData.get(DATA_AIM_YAW_OFFSET);
        float currentPitch = this.entityData.get(DATA_AIM_PITCH);

        float newYaw =
                currentYaw
                        + Mth.clamp(
                        Mth.wrapDegrees(targetYawOffset - currentYaw),
                        -AIM_TURN_SPEED_DEGREES_PER_TICK,
                        AIM_TURN_SPEED_DEGREES_PER_TICK
                );

        float newPitch =
                currentPitch
                        + Mth.clamp(
                        targetPitch - currentPitch,
                        -AIM_TURN_SPEED_DEGREES_PER_TICK,
                        AIM_TURN_SPEED_DEGREES_PER_TICK
                );

        newYaw = Mth.wrapDegrees(newYaw);

        if (Math.abs(newYaw - currentYaw) > 0.001F) {
            this.entityData.set(DATA_AIM_YAW_OFFSET, newYaw);
        }

        if (Math.abs(newPitch - currentPitch) > 0.001F) {
            this.entityData.set(DATA_AIM_PITCH, newPitch);
        }
    }

    private float[] computeAimAngles(Player owner, LivingEntity target) {

        Vec3 aimOrigin =
                this.getModelOrigin(owner)
                        .add(0.0D, AIM_PIVOT_Y, 0.0D);

        Vec3 delta =
                target.position()
                        .add(0.0D, target.getBbHeight() * 0.5D, 0.0D)
                        .subtract(aimOrigin);

        double horizontal =
                Math.sqrt(delta.x * delta.x + delta.z * delta.z);

        float desiredYaw =
                (float) (Mth.atan2(-delta.x, delta.z)
                        * (180.0D / Math.PI));

        float desiredPitch =
                (float) (-(Mth.atan2(delta.y, horizontal)
                        * (180.0D / Math.PI)));

        float restYaw = owner.yBodyRot - RENDER_INWARD_ANGLE;

        return new float[]{
                Mth.wrapDegrees(desiredYaw - restYaw),
                desiredPitch
        };
    }

    private boolean canAimAt(
            Player owner,
            LivingEntity target,
            double extraClearance
    ) {
        float[] aim = this.computeAimAngles(owner, target);

        if (Math.abs(aim[0]) > AIM_MAX_YAW_DEGREES
                || Math.abs(aim[1]) > AIM_MAX_PITCH_DEGREES) {
            return false;
        }

        Vec3 tip =
                this.aimedModelPoint(
                        owner,
                        aim[0],
                        aim[1],
                        MODEL_MUZZLE_Y,
                        MODEL_MUZZLE_Z
                );

        Vec3 rear =
                this.aimedModelPoint(
                        owner,
                        aim[0],
                        aim[1],
                        MODEL_MUZZLE_Y,
                        BARREL_REAR_Z
                );

        AABB ownerBox =
                owner.getBoundingBox()
                        .inflate(PLAYER_CLEARANCE + extraClearance);

        return !(ownerBox.contains(tip)
                || ownerBox.contains(rear)
                || ownerBox.clip(tip, rear).isPresent());
    }

    private LivingEntity lookupLockTarget() {

        if (this.lockTargetUUID == null
                || !(this.level() instanceof ServerLevel serverLevel)) {
            return null;
        }

        Entity entity = serverLevel.getEntity(this.lockTargetUUID);

        if (entity instanceof LivingEntity living && living.isAlive()) {
            return living;
        }

        return null;
    }

    public LivingEntity getClientLockTarget() {

        int id = this.entityData.get(DATA_LOCK_TARGET_ID);

        if (id < 0) {
            return null;
        }

        Entity entity = this.level().getEntity(id);

        if (entity instanceof LivingEntity living && living.isAlive()) {
            return living;
        }

        return null;
    }

    public void updateVisualAim(
            float desiredYawOffset,
            float desiredPitch,
            float partialTick
    ) {
        float now = this.tickCount + partialTick;

        float dt =
                this.visualAimLastTime < 0.0F
                        ? 1.0F
                        : Mth.clamp(now - this.visualAimLastTime, 0.0F, 5.0F);

        this.visualAimLastTime = now;

        float step = AIM_TURN_SPEED_DEGREES_PER_TICK * dt;

        this.visualAimYawOffset =
                Mth.wrapDegrees(
                        this.visualAimYawOffset
                                + Mth.clamp(
                                Mth.wrapDegrees(
                                        desiredYawOffset
                                                - this.visualAimYawOffset
                                ),
                                -step,
                                step
                        )
                );

        this.visualAimPitch +=
                Mth.clamp(
                        desiredPitch - this.visualAimPitch,
                        -step,
                        step
                );
    }

    public float getVisualAimYawOffset() {
        return this.visualAimYawOffset;
    }

    public float getVisualAimPitch() {
        return this.visualAimPitch;
    }

    public float getAimYawOffset(float partialTick) {
        return Mth.rotLerp(
                partialTick,
                this.clientAimYawOffsetO,
                this.clientAimYawOffset
        );
    }

    public float getAimPitch(float partialTick) {
        return Mth.lerp(
                partialTick,
                this.clientAimPitchO,
                this.clientAimPitch
        );
    }

    private LivingEntity findAttacker(Player owner) {

        LivingEntity attacker = owner.getLastHurtByMob();

        if (attacker != null
                && owner.tickCount - owner.getLastHurtByMobTimestamp()
                < OWNER_ATTACKED_MAX_AGE_TICKS
                && this.isValidTarget(owner, attacker)
                && this.canAimAt(owner, attacker, LOCK_ACQUIRE_EXTRA_CLEARANCE)) {
            return attacker;
        }

        return null;
    }

    private LivingEntity findTarget(Player owner) {

        LivingEntity lastHurt = owner.getLastHurtMob();

        if (lastHurt != null
                && owner.tickCount - owner.getLastHurtMobTimestamp()
                < OWNER_LAST_HURT_MAX_AGE_TICKS
                && this.isValidTarget(owner, lastHurt)
                && this.canAimAt(owner, lastHurt, LOCK_ACQUIRE_EXTRA_CLEARANCE)) {
            return lastHurt;
        }

        AABB searchBox =
                this.getBoundingBox().inflate(ATTACK_RANGE);

        List<LivingEntity> candidates =
                this.level().getEntitiesOfClass(
                        LivingEntity.class,
                        searchBox,
                        entity -> this.isValidTarget(owner, entity)
                                && this.canAimAt(
                                owner,
                                entity,
                                LOCK_ACQUIRE_EXTRA_CLEARANCE
                        )
                );

        return candidates.stream()
                .min(Comparator.comparingDouble(this::distanceToSqr))
                .orElse(null);
    }

    private boolean isValidTarget(Player owner, LivingEntity candidate) {

        if (candidate == null
                || !candidate.isAlive()
                || candidate == owner
                || candidate instanceof Player
                || candidate.isSpectator()) {
            return false;
        }

        if (owner.isAlliedTo(candidate)) {
            return false;
        }

        if (this.distanceToSqr(candidate) > ATTACK_RANGE * ATTACK_RANGE) {
            return false;
        }

        if (!this.isHostileTo(owner, candidate)) {
            return false;
        }

        if (!this.canAimAt(owner, candidate, 0.0D)) {
            return false;
        }

        return owner.hasLineOfSight(candidate);
    }

    private boolean isHostileTo(Player owner, LivingEntity candidate) {

        if (candidate instanceof Enemy) {
            return true;
        }

        if (candidate instanceof Mob mob && mob.getTarget() == owner) {
            return true;
        }

        ResourceLocation key =
                ForgeRegistries.ENTITY_TYPES.getKey(candidate.getType());

        return key != null && "dummmmmmy".equals(key.getNamespace());
    }

    private void spawnParticleEffect() {

        if (!this.isAlive() || this.clientModelPose == null) {
            return;
        }

        double progress =
                (double) this.spawnParticleTicks
                        / SPAWN_PARTICLE_DURATION;

        double scale =
                0.35D
                        + (1.0D - progress)
                        * 0.65D;

        Vec3 center =
                this.modelPointToWorld(
                        0.0D,
                        SPAWN_PARTICLE_Y_CENTER,
                        SPAWN_PARTICLE_Z_CENTER
                );

        for (int i = 0; i < SPAWN_SMOKE_PARTICLES_PER_TICK; i++) {
            this.emitSpawnParticle(
                    ParticleTypes.LARGE_SMOKE,
                    scale,
                    center,
                    SPAWN_SMOKE_RISE_SPEED
            );
        }

        for (int i = 0; i < SPAWN_ELECTRICITY_PARTICLES_PER_TICK; i++) {
            this.emitSpawnParticle(
                    ParticleRegistry.ELECTRICITY_PARTICLE.get(),
                    scale,
                    center,
                    0.0D
            );
        }
    }

    private void emitSpawnParticle(
            ParticleOptions type,
            double scale,
            Vec3 center,
            double riseSpeed
    ) {
        double localX =
                (this.random.nextDouble() - 0.5D)
                        * SPAWN_PARTICLE_WIDTH
                        * scale;

        double localY =
                SPAWN_PARTICLE_Y_CENTER
                        + (this.random.nextDouble() - 0.5D)
                        * SPAWN_PARTICLE_HEIGHT
                        * scale;

        double localZ =
                SPAWN_PARTICLE_Z_CENTER
                        + (this.random.nextDouble() - 0.5D)
                        * SPAWN_PARTICLE_LENGTH
                        * scale;

        Vec3 particle =
                this.modelPointToWorld(localX, localY, localZ);

        Vec3 outward = particle.subtract(center);

        double length = outward.length();

        double velocityX = 0.0D;
        double velocityY = 0.0D;
        double velocityZ = 0.0D;

        if (length > 0.001D) {
            velocityX = (outward.x / length) * 0.015D;
            velocityY = (outward.y / length) * 0.015D;
            velocityZ = (outward.z / length) * 0.015D;
        }

        this.level().addParticle(
                type,
                particle.x,
                particle.y,
                particle.z,
                velocityX,
                velocityY + riseSpeed,
                velocityZ
        );
    }

    public void setClientModelPose(
            Matrix4f pose,
            double originX,
            double originY,
            double originZ
    ) {
        this.clientModelPose = pose;
        this.clientModelOriginX = originX;
        this.clientModelOriginY = originY;
        this.clientModelOriginZ = originZ;
    }

    private Vec3 modelPointToWorld(double x, double y, double z) {

        Vector4f point =
                new Vector4f((float) x, (float) y, (float) z, 1.0F);

        point.mul(this.clientModelPose);

        return new Vec3(
                this.clientModelOriginX + point.x(),
                this.clientModelOriginY + point.y(),
                this.clientModelOriginZ + point.z()
        );
    }

    @Override
    public AABB getBoundingBoxForCulling() {
        return this.getBoundingBox()
                .inflate(CULLING_BOX_SIZE);
    }

    @Override
    public boolean isPickable() {
        return false;
    }

    @Override
    public boolean canBeCollidedWith() {
        return false;
    }

    @Override
    public boolean isAttackable() {
        return false;
    }

    @Override
    public boolean isInvulnerableTo(
            DamageSource source
    ) {
        return true;
    }

    @Override
    public boolean hurt(
            DamageSource source,
            float amount
    ) {
        return false;
    }

    @Override
    public void push(Entity entity) {
    }

    @Override
    public void push(
            double x,
            double y,
            double z
    ) {
    }

    @Override
    protected void addAdditionalSaveData(
            CompoundTag tag
    ) {
        UUID ownerUUID =
                this.getOwnerUUID();

        if (ownerUUID != null) {
            tag.putUUID(
                    "Owner",
                    ownerUUID
            );
        }
    }

    @Override
    protected void readAdditionalSaveData(
            CompoundTag tag
    ) {
        if (tag.hasUUID("Owner")) {
            this.entityData.set(
                    DATA_OWNER_UUID,
                    Optional.of(
                            tag.getUUID("Owner")
                    )
            );
        }
    }

    @Override
    public void registerControllers(
            AnimatableManager.ControllerRegistrar controllers
    ) {
        controllers.add(
                new AnimationController<>(
                        this,
                        "unstable_energy_cannon_controller",
                        ANIMATION_TRANSITION_TICKS,
                        this::animationPredicate
                )
        );
    }

    private PlayState animationPredicate(
            AnimationState<UnstableEnergyCannonEntity> state
    ) {
        if (this.tickCount < SPAWN_ANIMATION_TICKS) {
            state.getController().transitionLength(0);

            return state.setAndContinue(SPAWN_ANIMATION);
        }

        state.getController().transitionLength(ANIMATION_TRANSITION_TICKS);

        return switch (this.getAttackState()) {
            case STATE_CHARGING ->
                    state.setAndContinue(CHARGEUP_ANIMATION);

            case STATE_SHOOTING ->
                    state.setAndContinue(SHOOT_ANIMATION);

            default ->
                    state.setAndContinue(IDLE_ANIMATION);
        };
    }

    @Override
    public AnimatableInstanceCache
    getAnimatableInstanceCache() {
        return this.cache;
    }
}