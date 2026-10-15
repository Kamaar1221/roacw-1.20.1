package net.kamaarion.roacw.entity.summon.virili;

import io.redspace.ironsspellbooks.api.spells.AbstractSpell;
import io.redspace.ironsspellbooks.capabilities.magic.SummonManager;
import net.kamaarion.roacw.entity.projectile.homing_plague_missile.HomingPlagueMissileEntity;
import net.kamaarion.roacw.entity.projectile.plague_bee.PlagueBeeEntity;
import net.kamaarion.roacw.entity.summon.baby_plaguebringer.BabyPlaguebringerEntity;
import net.kamaarion.roacw.entity.summon.mini_plaguebringer.MiniPlaguebringerEntity;
import net.kamaarion.roacw.network.ModMessages;
import net.kamaarion.roacw.network.bonespawn.PacketViriliMissileSpawnC2S;
import net.kamaarion.roacw.registeries.ROACWEntityRegistry;
import net.kamaarion.roacw.registeries.ROACWParticleRegistry;
import net.kamaarion.roacw.registeries.ROACWSpellRegistry;
import net.kamaarion.roacw.registeries.ROACWSoundRegistry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.control.MoveControl;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.SmallFireball;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.PlayMessages;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.AnimationState;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.core.animation.RawAnimation;
import software.bernie.geckolib.core.object.PlayState;
import software.bernie.geckolib.util.GeckoLibUtil;

import javax.annotation.Nullable;
import java.util.UUID;

public class ViriliEntity extends PathfinderMob implements GeoEntity {

    private static final RawAnimation ANIM_IDLE =
            RawAnimation.begin().thenLoop("animation.Virili.idle");

    private static final RawAnimation ANIM_CHARGE_START =
            RawAnimation.begin().thenPlay("animation.Virili.ForwardChargeStart");

    private static final RawAnimation ANIM_CHARGE_LOOP =
            RawAnimation.begin().thenLoop("animation.Virili.ForwardChargeLoop");

    private static final RawAnimation ANIM_CHARGE_END =
            RawAnimation.begin().thenPlay("animation.Virili.ForwardChargeEnd");

    private static final RawAnimation ANIM_CHARGE =
            RawAnimation.begin().thenLoop("animation.Virili.Charge");

    private static final RawAnimation ANIM_SWARM =
            RawAnimation.begin().thenPlay("animation.Virili.Swarm");

    private static final RawAnimation ANIM_SWARM_IDLE =
            RawAnimation.begin().thenLoop("animation.Virili.SwarmIdle");

    private static final double MISSILE_BONE_LOCAL_Y = 1.4765625D;
    private static final double MISSILE_BONE_LOCAL_Z = -0.3828125D;
    private static final double MISSILE_VOLLEY_SIDE_SPACING = 0.50D;
    private static final long MISSILE_SPAWN_SYNC_MAX_STALENESS_TICKS = 3L;

    private static final EntityDataAccessor<Boolean> DATA_CHARGING =
            SynchedEntityData.defineId(
                    ViriliEntity.class,
                    EntityDataSerializers.BOOLEAN
            );

    private static final EntityDataAccessor<Boolean> DATA_SHOW_CHARGE_ANIM =
            SynchedEntityData.defineId(
                    ViriliEntity.class,
                    EntityDataSerializers.BOOLEAN
            );

    private static final EntityDataAccessor<Boolean> DATA_SHOW_CHARGE_END_ANIM =
            SynchedEntityData.defineId(
                    ViriliEntity.class,
                    EntityDataSerializers.BOOLEAN
            );

    private static final EntityDataAccessor<Boolean> DATA_REMOTE_CHARGING =
            SynchedEntityData.defineId(
                    ViriliEntity.class,
                    EntityDataSerializers.BOOLEAN
            );

    private static final EntityDataAccessor<Boolean> DATA_CHARGE_WINDUP =
            SynchedEntityData.defineId(
                    ViriliEntity.class,
                    EntityDataSerializers.BOOLEAN
            );

    private static final EntityDataAccessor<Boolean> DATA_ATTACK_ROTATION_LOCKED =
            SynchedEntityData.defineId(
                    ViriliEntity.class,
                    EntityDataSerializers.BOOLEAN
            );

    private static final EntityDataAccessor<Float> DATA_ATTACK_LOCKED_YAW =
            SynchedEntityData.defineId(
                    ViriliEntity.class,
                    EntityDataSerializers.FLOAT
            );

    private static final EntityDataAccessor<Float> DATA_ATTACK_LOCKED_PITCH =
            SynchedEntityData.defineId(
                    ViriliEntity.class,
                    EntityDataSerializers.FLOAT
            );

    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    @Nullable
    private Vec3 serverMissileSpawnPosition;

    private Vec3 clientMissileSpawnPosition = Vec3.ZERO;
    private Vec3 clientPreviousMissileSpawnPosition = Vec3.ZERO;
    private long serverMissileSpawnPositionTick = Long.MIN_VALUE;

    @Nullable
    private UUID ownerUUID;

    private int formationIndex = 0;
    private float spellPower = 1.0F;
    private boolean flightControlLocked = false;
    private boolean aiNeedsResume = false;

    private boolean attackRotationLocked = false;
    private float attackLockedYaw = 0.0F;
    private float attackLockedPitch = 0.0F;

    private int chargeStartAnimationTicks = 0;
    private int chargeEndAnimationTicks = -1;

    private static final int CHARGE_START_ANIMATION_TICKS = 17;
    private static final int CHARGE_END_ANIMATION_TICKS = 33;

    public ViriliEntity(EntityType<? extends ViriliEntity> entityType, Level level) {
        super(entityType, level);
        this.moveControl = new ImpulseFlightMoveControl(this);
        this.setNoGravity(true);
    }

    public ViriliEntity(PlayMessages.SpawnEntity spawnEntity, Level level) {
        this(ROACWEntityRegistry.VIRILI.get(), level);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 40.0D)
                .add(Attributes.ATTACK_DAMAGE, 5.0D)
                .add(Attributes.FLYING_SPEED, 0.6D)
                .add(Attributes.MOVEMENT_SPEED, 0.3D)
                .add(Attributes.FOLLOW_RANGE, 24.0D);
    }

    public void setClientMissileSpawnPosition(Vec3 position) {
        if (position == null) {
            return;
        }

        this.clientPreviousMissileSpawnPosition = this.clientMissileSpawnPosition;
        this.clientMissileSpawnPosition = position;
    }

    public Vec3 getClientMissileSpawnPosition() {
        return this.clientMissileSpawnPosition;
    }

    public Vec3 getPreviousClientMissileSpawnPosition() {
        return this.clientPreviousMissileSpawnPosition;
    }

    public Vec3 getInterpolatedClientMissileSpawnPosition(float partialTick) {
        return this.clientPreviousMissileSpawnPosition.lerp(
                this.clientMissileSpawnPosition,
                partialTick
        );
    }

    public void setServerMissileSpawnPosition(double worldX, double worldY, double worldZ) {
        this.serverMissileSpawnPosition = new Vec3(worldX, worldY, worldZ);
        this.serverMissileSpawnPositionTick = this.level().getGameTime();
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();

        this.entityData.define(DATA_CHARGING, false);
        this.entityData.define(DATA_SHOW_CHARGE_ANIM, false);
        this.entityData.define(DATA_SHOW_CHARGE_END_ANIM, false);
        this.entityData.define(DATA_REMOTE_CHARGING, false);
        this.entityData.define(DATA_CHARGE_WINDUP, false);
        this.entityData.define(DATA_ATTACK_ROTATION_LOCKED, false);
        this.entityData.define(DATA_ATTACK_LOCKED_YAW, 0.0F);
        this.entityData.define(DATA_ATTACK_LOCKED_PITCH, 0.0F);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new ViriliAttackGoal(this));
        this.goalSelector.addGoal(1, new ViriliOwnerHoverGoal(this));
        this.goalSelector.addGoal(2, new LookAtPlayerGoal(this, Player.class, 16.0F));

        this.targetSelector.addGoal(
                0,
                new NearestAttackableTargetGoal<>(
                        this,
                        LivingEntity.class,
                        10,
                        true,
                        false,
                        this::isValidTarget
                )
        );
    }

    private boolean isValidTarget(@Nullable LivingEntity candidate) {
        if (candidate == null
                || candidate == this
                || !candidate.isAlive()
                || candidate.isSpectator()
                || candidate.isRemoved()) {
            return false;
        }

        LivingEntity owner = this.getOwner();

        if (candidate == owner) {
            return false;
        }

        if (owner != null && areSameOrAllied(owner, candidate)) {
            return false;
        }

        if (candidate instanceof ViriliEntity otherVirili) {
            UUID otherOwnerUUID = otherVirili.getOwnerUUID();

            if (this.ownerUUID != null
                    && this.ownerUUID.equals(otherOwnerUUID)) {
                return false;
            }

            LivingEntity otherOwner = otherVirili.getOwner();

            if (owner != null
                    && otherOwner != null
                    && areSameOrAllied(owner, otherOwner)) {
                return false;
            }
        }

        if (candidate instanceof MiniPlaguebringerEntity miniPlaguebringer) {
            LivingEntity otherOwner = miniPlaguebringer.getSummonOwner();

            if (owner != null
                    && otherOwner != null
                    && areSameOrAllied(owner, otherOwner)) {
                return false;
            }
        }

        Entity candidateOwner = getResolvedSummonOwner(candidate);

        if (candidateOwner != null) {
            if (candidateOwner == this || candidateOwner == owner) {
                return false;
            }

            if (owner != null && areSameOrAllied(owner, candidateOwner)) {
                return false;
            }
        }

        if (candidate.isAlliedTo(this) || this.isAlliedTo(candidate)) {
            return false;
        }

        return true;
    }

    private static boolean areSameOrAllied(
            @Nullable Entity first,
            @Nullable Entity second
    ) {
        if (first == null || second == null) {
            return false;
        }

        return first == second
                || first.isAlliedTo(second)
                || second.isAlliedTo(first);
    }

    @Nullable
    private Entity getResolvedSummonOwner(Entity entity) {
        if (entity instanceof ViriliEntity virili) {
            LivingEntity owner = virili.getOwner();

            if (owner != null) {
                return owner;
            }
        }

        if (entity instanceof MiniPlaguebringerEntity miniPlaguebringer) {
            LivingEntity owner = miniPlaguebringer.getSummonOwner();

            if (owner != null) {
                return owner;
            }
        }

        Entity owner = SummonManager.getOwner(entity);

        if (owner != null && owner != entity) {
            return owner;
        }

        return null;
    }

    public void playChargeSound() {
        this.playViriliSound(ROACWSoundRegistry.VIRILI_CHARGE.get());
    }

    public void playSummonSound() {
        this.playViriliSound(ROACWSoundRegistry.VIRILI_SUMMON.get());
    }

    public void playRandomPhaseSwitchSound() {
        SoundEvent sound = this.getRandom().nextBoolean()
                ? ROACWSoundRegistry.VIRILI_PHASE_1.get()
                : ROACWSoundRegistry.VIRILI_PHASE_2.get();

        this.playViriliSound(sound);
    }

    private void playViriliSound(SoundEvent sound) {
        if (this.level().isClientSide) {
            return;
        }

        this.level().playSound(
                null,
                this.getX(),
                this.getY(),
                this.getZ(),
                sound,
                SoundSource.HOSTILE,
                1.0F,
                1.0F
        );
    }

    public void fireRocketAt(LivingEntity target) {
        if (this.level().isClientSide || !this.isValidTarget(target)) {
            return;
        }

        Vec3 origin = this.position().add(0.0D, this.getBbHeight() * 0.5D, 0.0D);
        Vec3 targetPosition = target.position().add(0.0D, target.getBbHeight() * 0.5D, 0.0D);
        Vec3 direction = targetPosition.subtract(origin).normalize();

        SmallFireball fireball = new SmallFireball(
                this.level(),
                origin.x,
                origin.y,
                origin.z,
                direction.x,
                direction.y,
                direction.z
        );

        fireball.setOwner(this);
        this.level().addFreshEntity(fireball);
    }

    private void tickClientMissileSpawnSync() {
        if (!this.level().isClientSide) {
            return;
        }

        if ((this.tickCount & 1) != 0) {
            return;
        }

        ModMessages.sendToServer(
                new PacketViriliMissileSpawnC2S(
                        this.getId(),
                        this.clientMissileSpawnPosition.x,
                        this.clientMissileSpawnPosition.y,
                        this.clientMissileSpawnPosition.z,
                        this.getX(),
                        this.getY(),
                        this.getZ()
                )
        );
    }

    private Vec3 getMissileSpawnBoneOffset() {
        double yawRadians = Math.toRadians(this.getYRot());
        double pitchRadians = Math.toRadians(this.getXRot());

        double cosPitch = Math.cos(pitchRadians);
        double sinPitch = Math.sin(pitchRadians);

        double localY = MISSILE_BONE_LOCAL_Y * cosPitch
                - MISSILE_BONE_LOCAL_Z * sinPitch;

        double localZ = MISSILE_BONE_LOCAL_Y * sinPitch
                + MISSILE_BONE_LOCAL_Z * cosPitch;

        double cosYaw = Math.cos(yawRadians);
        double sinYaw = Math.sin(yawRadians);

        double worldX = -localZ * sinYaw;
        double worldZ = localZ * cosYaw;
        double worldY = localY;

        return new Vec3(worldX, worldY, worldZ);
    }

    private Vec3 resolveMissileSpawnPosition() {
        if (this.serverMissileSpawnPosition != null) {
            long staleness = this.level().getGameTime()
                    - this.serverMissileSpawnPositionTick;

            if (staleness >= 0
                    && staleness <= MISSILE_SPAWN_SYNC_MAX_STALENESS_TICKS) {
                return this.serverMissileSpawnPosition;
            }
        }

        return this.position().add(this.getMissileSpawnBoneOffset());
    }

    public void fireHomingPlagueMissileAt(LivingEntity target) {
        this.fireHomingPlagueMissileAt(target, 0.0D);
    }

    public void fireHomingPlagueMissileAt(LivingEntity target, double sideOffset) {
        if (this.level().isClientSide || !this.isValidTarget(target)) {
            return;
        }

        Vec3 forward = this.getLookAngle().normalize();
        Vec3 right = new Vec3(-forward.z, 0.0D, forward.x);

        if (right.lengthSqr() > 0.0001D) {
            right = right.normalize();
        } else {
            float yawRadians = this.getYRot() * Mth.DEG_TO_RAD;
            right = new Vec3(
                    -Mth.sin(yawRadians),
                    0.0D,
                    Mth.cos(yawRadians)
            ).normalize();
        }

        Vec3 spawnPosition = this.resolveMissileSpawnPosition();

        if (Math.abs(sideOffset) > 0.0001D) {
            spawnPosition = spawnPosition.add(right.scale(sideOffset));
        }

        float damage = 2.0F * this.spellPower;

        HomingPlagueMissileEntity missile = new HomingPlagueMissileEntity(
                this.level(),
                this,
                target,
                damage,
                spawnPosition,
                ROACWSpellRegistry.SUMMON_VIRILI.get()
        );

        this.level().addFreshEntity(missile);
    }

    public void fireHomingPlagueMissileVolley(LivingEntity target) {
        if (this.level().isClientSide || !this.isValidTarget(target)) {
            return;
        }

        this.fireHomingPlagueMissileAt(target, -MISSILE_VOLLEY_SIDE_SPACING);
        this.fireHomingPlagueMissileAt(target, 0.0D);
        this.fireHomingPlagueMissileAt(target, MISSILE_VOLLEY_SIDE_SPACING);
    }

    public void spawnSwarmSupport(@Nullable LivingEntity target) {
        if (this.level().isClientSide
                || !(this.level() instanceof ServerLevel serverLevel)
                || !this.isValidTarget(target)) {
            return;
        }

        Vec3 spawnPosition = this.resolveMissileSpawnPosition();
        boolean spawnedBee = false;

        for (int spreadIndex = 0; spreadIndex < 3; spreadIndex++) {
            PlagueBeeEntity bee = new PlagueBeeEntity(
                    ROACWEntityRegistry.PLAGUE_BEE.get(),
                    serverLevel,
                    this,
                    target,
                    spreadIndex
            );

            bee.setPos(spawnPosition.x, spawnPosition.y, spawnPosition.z);
            bee.setDeltaMovement(Vec3.ZERO);
            bee.setSpellPower(this.spellPower);

            if (serverLevel.addFreshEntity(bee)) {
                spawnedBee = true;
            }
        }

        if (spawnedBee) {
            this.playSummonSound();
        }

        serverLevel.sendParticles(
                ROACWParticleRegistry.PLAGUE_CLOUD.get(),
                spawnPosition.x,
                spawnPosition.y,
                spawnPosition.z,
                8,
                0.2D,
                0.25D,
                0.2D,
                0.02D
        );
    }

    public void spawnBabyPlaguebringer(@Nullable LivingEntity target) {
        if (this.level().isClientSide
                || !(this.level() instanceof ServerLevel serverLevel)
                || !this.isValidTarget(target)) {
            return;
        }

        BabyPlaguebringerEntity baby = ROACWEntityRegistry.BABY_PLAGUEBRINGER.get()
                .create(serverLevel);

        if (baby == null) {
            return;
        }

        Vec3 spawnPosition = this.resolveMissileSpawnPosition();

        baby.moveTo(
                spawnPosition.x,
                spawnPosition.y,
                spawnPosition.z,
                this.getYRot(),
                0.0F
        );

        baby.setOwnerUUID(this.ownerUUID);
        baby.setSpellPower(this.spellPower);
        baby.setTarget(target);

        if (!serverLevel.addFreshEntity(baby)) {
            return;
        }

        this.playSummonSound();

        serverLevel.sendParticles(
                ROACWParticleRegistry.PLAGUE_CLOUD.get(),
                spawnPosition.x,
                spawnPosition.y + 0.5D,
                spawnPosition.z,
                20,
                0.3D,
                0.4D,
                0.3D,
                0.02D
        );
    }

    @Override
    public void tick() {
        if (!this.level().isClientSide && this.aiNeedsResume) {
            this.resumeAiAfterReload();
        }

        super.tick();

        this.setNoGravity(true);
        this.resetFallDistance();

        if (this.attackRotationLocked) {
            this.forceAttackRotation();
        }

        if (this.level().isClientSide) {
            this.tickClientMissileSpawnSync();
        }

        if (!this.level().isClientSide) {
            LivingEntity target = this.getTarget();

            if (target != null && !this.isValidTarget(target)) {
                this.setTarget(null);
            }

            if (this.isCharging() && !this.isChargeAnimating() && !this.isChargeEnding()) {
                this.chargeStartAnimationTicks++;

                if (this.chargeStartAnimationTicks >= CHARGE_START_ANIMATION_TICKS) {
                    this.setChargeAnimating(true);
                    this.chargeStartAnimationTicks = 0;
                }
            }

            if (this.isChargeEnding()) {
                if (this.chargeEndAnimationTicks < 0) {
                    this.chargeEndAnimationTicks = 0;
                }

                this.chargeEndAnimationTicks++;

                if (this.chargeEndAnimationTicks >= CHARGE_END_ANIMATION_TICKS) {
                    this.setChargeEnding(false);
                    this.chargeEndAnimationTicks = -1;
                }
            }
        }
    }

    private void resumeAiAfterReload() {
        this.aiNeedsResume = false;
        this.resetTransientAiState();
        this.setTarget(null);

        double range = 24.0D;
        AABB searchBox = this.getBoundingBox().inflate(range);

        LivingEntity bestTarget = null;
        double bestDistance = Double.MAX_VALUE;

        for (LivingEntity candidate : this.level().getEntitiesOfClass(
                LivingEntity.class,
                searchBox,
                this::isValidTarget
        )) {
            double distance = this.distanceToSqr(candidate);

            if (distance < bestDistance) {
                bestDistance = distance;
                bestTarget = candidate;
            }
        }

        if (bestTarget != null) {
            this.setTarget(bestTarget);
        }
    }

    public void setAttackRotationLocked(boolean locked, float yaw, float pitch) {
        this.attackRotationLocked = locked;
        this.entityData.set(DATA_ATTACK_ROTATION_LOCKED, locked);

        if (locked) {
            this.attackLockedYaw = yaw;
            this.attackLockedPitch = pitch;

            this.entityData.set(DATA_ATTACK_LOCKED_YAW, yaw);
            this.entityData.set(DATA_ATTACK_LOCKED_PITCH, pitch);

            this.forceAttackRotation();
        }
    }

    public void setAttackRotationLocked(boolean locked, float yaw) {
        this.setAttackRotationLocked(locked, yaw, 0.0F);
    }

    public void setAttackRotationLocked(boolean locked) {
        this.attackRotationLocked = locked;
        this.entityData.set(DATA_ATTACK_ROTATION_LOCKED, locked);

        if (locked) {
            this.forceAttackRotation();
        }
    }

    public boolean isAttackRotationLocked() {
        return this.attackRotationLocked;
    }

    public float getAttackLockedYaw() {
        return this.attackLockedYaw;
    }

    public float getAttackLockedPitch() {
        return this.attackLockedPitch;
    }

    public void forceAttackRotation() {
        if (!this.attackRotationLocked) {
            return;
        }

        this.applyImmediateRotation(this.attackLockedYaw, this.attackLockedPitch);
    }

    public void applyImmediateRotation(float yaw, float pitch) {
        this.setYRot(yaw);
        this.setYHeadRot(yaw);
        this.setXRot(pitch);

        this.yRotO = yaw;
        this.yHeadRotO = yaw;
        this.xRotO = pitch;
        this.yBodyRot = yaw;
        this.yBodyRotO = yaw;
    }

    public void applyImmediateRotation(float yaw) {
        this.applyImmediateRotation(yaw, 0.0F);
    }

    @Override
    public void lerpTo(
            double x,
            double y,
            double z,
            float yRot,
            float xRot,
            int steps,
            boolean teleport
    ) {
        super.lerpTo(x, y, z, yRot, xRot, steps, teleport);

        if (this.entityData.get(DATA_ATTACK_ROTATION_LOCKED)) {
            this.applyImmediateRotation(
                    this.entityData.get(DATA_ATTACK_LOCKED_YAW),
                    this.entityData.get(DATA_ATTACK_LOCKED_PITCH)
            );
        }
    }

    @Override
    public void lerpHeadTo(float yRot, int steps) {
        super.lerpHeadTo(yRot, steps);

        if (this.entityData.get(DATA_ATTACK_ROTATION_LOCKED)) {
            this.applyImmediateRotation(
                    this.entityData.get(DATA_ATTACK_LOCKED_YAW),
                    this.entityData.get(DATA_ATTACK_LOCKED_PITCH)
            );
        }
    }

    public void resetTransientAiState() {
        this.flightControlLocked = false;
        this.attackRotationLocked = false;
        this.attackLockedYaw = this.getYRot();
        this.attackLockedPitch = 0.0F;

        if (this.entityData != null) {
            this.entityData.set(DATA_ATTACK_ROTATION_LOCKED, false);
            this.entityData.set(DATA_ATTACK_LOCKED_YAW, this.getYRot());
            this.entityData.set(DATA_ATTACK_LOCKED_PITCH, 0.0F);
        }

        this.setDeltaMovement(Vec3.ZERO);
        this.finishChargeAnimation();
        this.finishRemoteChargeAnimation();
        this.setChargeWindupActive(false);
    }

    public boolean isCharging() {
        return this.entityData.get(DATA_CHARGING);
    }

    public void setCharging(boolean charging) {
        this.entityData.set(DATA_CHARGING, charging);

        if (!charging) {
            this.chargeStartAnimationTicks = 0;
        }
    }

    public boolean isChargeAnimating() {
        return this.entityData.get(DATA_SHOW_CHARGE_ANIM);
    }

    public void setChargeAnimating(boolean active) {
        this.entityData.set(DATA_SHOW_CHARGE_ANIM, active);

        if (!active) {
            this.chargeStartAnimationTicks = 0;
        }
    }

    public boolean isChargeEnding() {
        return this.entityData.get(DATA_SHOW_CHARGE_END_ANIM);
    }

    public void setChargeEnding(boolean active) {
        this.entityData.set(DATA_SHOW_CHARGE_END_ANIM, active);

        if (active) {
            this.chargeEndAnimationTicks = 0;
        } else {
            this.chargeEndAnimationTicks = -1;
        }
    }

    public void beginChargeAnimation() {
        this.setChargeEnding(false);
        this.setChargeAnimating(false);
        this.chargeStartAnimationTicks = 0;
        this.setCharging(true);
    }

    public void beginChargeEndAnimation() {
        this.setCharging(false);
        this.setChargeAnimating(false);
        this.chargeStartAnimationTicks = 0;
        this.setChargeEnding(true);
    }

    public void finishChargeAnimation() {
        this.setCharging(false);
        this.setChargeAnimating(false);
        this.setChargeEnding(false);
        this.chargeStartAnimationTicks = 0;
        this.chargeEndAnimationTicks = -1;
    }

    public boolean isRemoteCharging() {
        return this.entityData.get(DATA_REMOTE_CHARGING);
    }

    public void setRemoteCharging(boolean charging) {
        this.entityData.set(DATA_REMOTE_CHARGING, charging);
    }

    public void beginRemoteChargeAnimation() {
        this.setCharging(false);
        this.setChargeAnimating(false);
        this.setChargeEnding(false);
        this.setChargeWindupActive(false);
        this.setRemoteCharging(true);
    }

    public void finishRemoteChargeAnimation() {
        this.setRemoteCharging(false);
    }

    public void setChargeWindupActive(boolean active) {
        this.entityData.set(DATA_CHARGE_WINDUP, active);
    }

    public boolean isChargeWindupActive() {
        return this.entityData.get(DATA_CHARGE_WINDUP);
    }

    @Nullable
    public LivingEntity getOwner() {
        if (this.ownerUUID == null || !(this.level() instanceof ServerLevel serverLevel)) {
            return null;
        }

        Entity entity = serverLevel.getServer()
                .getPlayerList()
                .getPlayer(this.ownerUUID);

        if (entity instanceof LivingEntity livingEntity) {
            return livingEntity;
        }

        Entity resolved = serverLevel.getEntity(this.ownerUUID);

        if (resolved instanceof LivingEntity livingEntity) {
            return livingEntity;
        }

        return null;
    }

    @Nullable
    public Player getViriliOwner() {
        if (this.ownerUUID == null || !(this.level() instanceof ServerLevel serverLevel)) {
            return null;
        }

        return serverLevel.getServer()
                .getPlayerList()
                .getPlayer(this.ownerUUID);
    }

    public void setOwnerUUID(@Nullable UUID uuid) {
        this.ownerUUID = uuid;
    }

    @Nullable
    public UUID getOwnerUUID() {
        return this.ownerUUID;
    }

    public int getFormationIndex() {
        return this.formationIndex;
    }

    public void setFormationIndex(int index) {
        this.formationIndex = index;
    }

    public float getSpellPower() {
        return this.spellPower;
    }

    public void setSpellPower(float spellPower) {
        this.spellPower = spellPower;
    }

    public RawAnimation getIdleAnimation() {
        return ANIM_IDLE;
    }

    public RawAnimation getChargeStartAnimation() {
        return ANIM_CHARGE_START;
    }

    public RawAnimation getChargeLoopAnimation() {
        return ANIM_CHARGE_LOOP;
    }

    public RawAnimation getChargeEndAnimation() {
        return ANIM_CHARGE_END;
    }

    public RawAnimation getChargeAnimation() {
        return ANIM_CHARGE;
    }

    public RawAnimation getBarrageAnimation() {
        return ANIM_CHARGE;
    }

    public RawAnimation getSwarmAnimation() {
        return ANIM_SWARM;
    }

    public RawAnimation getSwarmIdleAnimation() {
        return ANIM_SWARM_IDLE;
    }

    public float getAssaultChargeDamage() {
        return (float) this.getAttributeValue(Attributes.ATTACK_DAMAGE);
    }

    public DamageSource getAssaultChargeDamageSource(AbstractSpell spell) {
        LivingEntity owner = this.getOwner();

        if (owner != null) {
            return spell.getDamageSource(this, owner);
        }

        return spell.getDamageSource(this);
    }

    public void setFlightControlLocked(boolean locked) {
        this.flightControlLocked = locked;
    }

    public boolean isFlightControlLocked() {
        return this.flightControlLocked;
    }

    @Override
    public boolean causeFallDamage(
            float fallDistance,
            float damageMultiplier,
            DamageSource damageSource
    ) {
        return false;
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);

        if (this.ownerUUID != null) {
            tag.putUUID("OwnerUUID", this.ownerUUID);
        }

        tag.putInt("FormationIndex", this.formationIndex);
        tag.putFloat("SpellPower", this.spellPower);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);

        if (tag.hasUUID("OwnerUUID")) {
            this.ownerUUID = tag.getUUID("OwnerUUID");
        }

        this.formationIndex = tag.getInt("FormationIndex");
        this.spellPower = tag.getFloat("SpellPower");

        this.resetTransientAiState();
        this.setTarget(null);
        this.aiNeedsResume = true;
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(
                new AnimationController<>(
                        this,
                        "attack_controller",
                        3,
                        this::animationPredicate
                )
        );
    }

    private PlayState animationPredicate(AnimationState<ViriliEntity> state) {
        if (this.isRemoteCharging()) {
            state.getController().setAnimation(ANIM_CHARGE);
            return PlayState.CONTINUE;
        }

        if (this.isChargeEnding()) {
            state.getController().setAnimation(ANIM_CHARGE_END);
            return PlayState.CONTINUE;
        }

        if (this.isChargeAnimating()) {
            state.getController().setAnimation(ANIM_CHARGE_LOOP);
            return PlayState.CONTINUE;
        }

        if (this.isCharging()) {
            state.getController().setAnimation(ANIM_CHARGE_START);
            return PlayState.CONTINUE;
        }

        state.getController().setAnimation(ANIM_IDLE);
        return PlayState.CONTINUE;
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.cache;
    }

    private static class ImpulseFlightMoveControl extends MoveControl {

        private final ViriliEntity mob;

        private ImpulseFlightMoveControl(ViriliEntity mob) {
            super(mob);
            this.mob = mob;
        }

        @Override
        public void tick() {
            if (this.mob.isFlightControlLocked()) {
                this.mob.setDeltaMovement(Vec3.ZERO);
                return;
            }

            if (this.operation != Operation.MOVE_TO) {
                this.mob.setDeltaMovement(this.mob.getDeltaMovement().scale(0.8D));
                return;
            }

            Vec3 wanted = new Vec3(this.wantedX, this.wantedY, this.wantedZ);
            Vec3 difference = wanted.subtract(this.mob.position());
            double distance = difference.length();

            if (distance < 0.05D) {
                this.operation = Operation.WAIT;
                this.mob.setDeltaMovement(Vec3.ZERO);
                return;
            }

            Vec3 direction = difference.normalize();
            double speed = this.speedModifier;

            Vec3 movement = this.mob.getDeltaMovement()
                    .scale(0.75D)
                    .add(direction.scale(speed * 0.25D));

            if (movement.lengthSqr() > speed * speed) {
                movement = movement.normalize().scale(speed);
            }

            this.mob.setDeltaMovement(movement);
        }
    }
}