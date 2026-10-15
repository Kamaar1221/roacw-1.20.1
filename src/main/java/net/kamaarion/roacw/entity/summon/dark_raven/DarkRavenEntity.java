package net.kamaarion.roacw.entity.summon.dark_raven;

import io.redspace.ironsspellbooks.capabilities.magic.SummonManager;
import io.redspace.ironsspellbooks.damage.DamageSources;
import io.redspace.ironsspellbooks.entity.mobs.IMagicSummon;
import net.kamaarion.roacw.ROACW;
import net.kamaarion.roacw.registeries.ROACWSoundRegistry;
import net.kamaarion.roacw.registeries.ROACWSpellRegistry;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.OwnableEntity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.AnimationState;
import software.bernie.geckolib.core.animation.RawAnimation;
import software.bernie.geckolib.core.object.PlayState;
import software.bernie.geckolib.util.GeckoLibUtil;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class DarkRavenEntity extends PathfinderMob implements GeoEntity, IMagicSummon {

    private static final int CHARGE_HIT_FOLLOWTHROUGH_TICKS = 5;

    private static final EntityDataAccessor<Boolean> DATA_CHARGING =
            SynchedEntityData.defineId(DarkRavenEntity.class, EntityDataSerializers.BOOLEAN);

    private static final EntityDataAccessor<Boolean> DATA_SHOW_CHARGE_ANIM =
            SynchedEntityData.defineId(DarkRavenEntity.class, EntityDataSerializers.BOOLEAN);

    private static final RawAnimation IDLE_ANIM =
            RawAnimation.begin().thenLoop("ravenIdle");

    private static final RawAnimation CHARGE_START_ANIM =
            RawAnimation.begin().thenPlay("ravenchargeStart");

    private static final RawAnimation CHARGE_LOOP_ANIM =
            RawAnimation.begin().thenLoop("ravenchargeLoop");

    private static final int CHARGE_WINDUP_TICKS = 8;
    private static final double WINDUP_HOVER_BOB_AMPLITUDE = 0.08D;
    private static final double WINDUP_HOVER_BOB_SPEED = Math.PI / 2.0D;
    private static final double CHARGE_SPEED = 2.5D;
    private static final double CHARGE_OVERSHOOT_DISTANCE = 5.0D;
    private static final int CHARGE_MAX_TICKS = 40;
    private static final float DASH_SPELLPOWER_MULTIPLIER = 1.0F;
    private static final float ATTACK_SOUND_CHANCE = 0.2F;
    private static final int ATTACK_SOUND_GROUP_COOLDOWN_TICKS = 60;

    private static final Map<UUID, Long> lastAttackSoundGameTime = new HashMap<>();

    private static final int IDLE_SOUND_MIN_DELAY_TICKS = 160;
    private static final int IDLE_SOUND_MAX_DELAY_TICKS = 400;
    private static final int IDLE_SOUND_GROUP_MIN_COOLDOWN_TICKS = 80;
    private static final int IDLE_SOUND_GROUP_MAX_COOLDOWN_TICKS = 200;
    private static final int IDLE_SOUND_RETRY_MIN_TICKS = 20;
    private static final int IDLE_SOUND_RETRY_MAX_TICKS = 60;

    private static final Map<UUID, Long> nextIdleSoundAllowedGameTime = new HashMap<>();

    private static final double CHARGE_TARGET_HEIGHT = 1.75D;
    private static final double CHARGE_GROUND_CLEARANCE = 1.25D;
    private static final double CHARGE_GROUND_SEARCH_DISTANCE = 16.0D;
    private static final double CHARGE_COLLISION_PADDING = 0.05D;
    private static final double CHARGE_TARGET_PADDING = 0.30D;
    private static final double CHARGE_PATH_STEP = 0.10D;

    static final float TELEPORT_CHARGE_CHANCE = 0.2F;

    private static final double TELEPORT_CHARGE_MIN_DISTANCE = 3.0D;
    private static final double TELEPORT_CHARGE_MAX_DISTANCE = 6.0D;
    private static final int TELEPORT_CHARGE_MAX_ATTEMPTS = 8;

    private static final int OWNER_TARGET_FRESHNESS_TICKS = 100;
    private static final double MAX_TARGET_DISTANCE_FROM_OWNER = 24.0D;
    private static final double MAX_TARGET_DISTANCE_FROM_OWNER_SQR =
            MAX_TARGET_DISTANCE_FROM_OWNER * MAX_TARGET_DISTANCE_FROM_OWNER;

    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);
    private final Set<UUID> chargeVictims = new HashSet<>();

    @Nullable
    private UUID ownerUUID;

    private boolean aiNeedsResume;
    private float spellPower = 2.0F;

    @Nullable
    private Vec3 chargeDestination;

    @Nullable
    private Vec3 chargeStart;

    private int chargeTicks = -1;
    private int chargeAnimReleaseTicks = -1;
    private boolean finishedCharge;
    private float chargeYaw;
    private int idleSoundCooldownTicks;
    private double chargeCeilingY = Double.POSITIVE_INFINITY;

    public DarkRavenEntity(EntityType<? extends PathfinderMob> type, Level level) {
        super(type, level);
        this.setPersistenceRequired();
        this.setNoGravity(true);
        this.moveControl = new DarkRavenMoveControl(this);
        resetIdleSoundCooldown();
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(DATA_CHARGING, false);
        this.entityData.define(DATA_SHOW_CHARGE_ANIM, false);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 20.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.35D)
                .add(Attributes.FOLLOW_RANGE, 24.0D)
                .add(Attributes.ATTACK_DAMAGE, 10.0D);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(1, new DarkRavenAttackGoal(this));
        this.goalSelector.addGoal(2, new DarkRavenFollowOwnerGoal(this));
        this.goalSelector.addGoal(3, new LookAtPlayerGoal(this, Player.class, 8.0F));

        this.targetSelector.addGoal(1, new HurtByTargetGoal(this).setAlertOthers());
        this.targetSelector.addGoal(
                2,
                new NearestAttackableTargetGoal<>(
                        this,
                        Monster.class,
                        10,
                        true,
                        false,
                        target -> !this.isAlly(target)
                )
        );
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putFloat("SpellPower", this.spellPower);

        if (this.ownerUUID != null) {
            tag.putUUID("OwnerUUID", this.ownerUUID);
        }
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);

        if (tag.contains("SpellPower")) {
            this.spellPower = tag.getFloat("SpellPower");
        }

        if (tag.hasUUID("OwnerUUID")) {
            this.ownerUUID = tag.getUUID("OwnerUUID");
        }

        resetTransientAiState();
        this.setTarget(null);
        this.aiNeedsResume = true;
    }

    public void setOwnerUUID(@Nullable UUID ownerUUID) {
        this.ownerUUID = ownerUUID;
    }

    @Nullable
    public UUID getOwnerUUID() {
        return this.ownerUUID;
    }

    @Nullable
    public LivingEntity getSummonOwner() {
        if (this.ownerUUID != null && this.level() instanceof ServerLevel serverLevel) {
            Entity player = serverLevel.getServer().getPlayerList().getPlayer(this.ownerUUID);

            if (player instanceof LivingEntity living) {
                return living;
            }

            Entity resolved = serverLevel.getEntity(this.ownerUUID);

            if (resolved instanceof LivingEntity living) {
                return living;
            }
        }

        var owner = SummonManager.getOwner(this);

        if (owner instanceof LivingEntity living) {
            if (this.ownerUUID == null) {
                this.ownerUUID = living.getUUID();
            }

            return living;
        }

        return null;
    }

    private void resetTransientAiState() {
        this.chargeVictims.clear();
        this.chargeDestination = null;
        this.chargeStart = null;
        this.chargeTicks = -1;
        this.chargeAnimReleaseTicks = -1;
        this.finishedCharge = false;
        this.chargeYaw = this.getYRot();
        this.chargeCeilingY = Double.POSITIVE_INFINITY;
        this.setCharging(false);
        this.setChargeAnimating(false);
        this.setDeltaMovement(Vec3.ZERO);
        this.getNavigation().stop();

        if (this.moveControl instanceof DarkRavenMoveControl ravenMoveControl) {
            ravenMoveControl.stop();
        }
    }

    private void resumeAiAfterReload() {
        if (!this.aiNeedsResume) {
            return;
        }

        LivingEntity owner = this.getSummonOwner();

        if (owner == null) {
            return;
        }

        resetTransientAiState();
        this.setTarget(null);
        this.aiNeedsResume = false;
    }

    public void setSpellPower(float spellPower) {
        this.spellPower = spellPower;
    }

    public float getSpellPower() {
        return this.spellPower;
    }

    @Override
    protected net.minecraft.sounds.SoundEvent getAmbientSound() {
        return null;
    }

    @Override
    protected net.minecraft.sounds.SoundEvent getDeathSound() {
        return ROACWSoundRegistry.DARK_RAVEN_HURT.get();
    }

    @Override
    protected float getSoundVolume() {
        return 0.7F;
    }

    @Override
    public float getVoicePitch() {
        return 0.9F + this.random.nextFloat() * 0.12F;
    }

    @Override
    public int getAmbientSoundInterval() {
        return 60;
    }

    private void resetIdleSoundCooldown() {
        this.idleSoundCooldownTicks = IDLE_SOUND_MIN_DELAY_TICKS
                + this.random.nextInt(IDLE_SOUND_MAX_DELAY_TICKS - IDLE_SOUND_MIN_DELAY_TICKS + 1);
    }

    private void resetIdleSoundRetryCooldown() {
        this.idleSoundCooldownTicks = IDLE_SOUND_RETRY_MIN_TICKS
                + this.random.nextInt(IDLE_SOUND_RETRY_MAX_TICKS - IDLE_SOUND_RETRY_MIN_TICKS + 1);
    }

    private void tryPlayIdleSound() {
        if (this.isCharging()) {
            return;
        }

        if (this.idleSoundCooldownTicks > 0) {
            this.idleSoundCooldownTicks--;
            return;
        }

        long gameTime = this.level().getGameTime();
        LivingEntity summonOwner = this.getSummonOwner();
        UUID ownerId = summonOwner != null ? summonOwner.getUUID() : this.ownerUUID;

        Long nextAllowed = nextIdleSoundAllowedGameTime.get(ownerId);

        if (nextAllowed != null
                && gameTime < nextAllowed
                && nextAllowed - gameTime <= IDLE_SOUND_GROUP_MAX_COOLDOWN_TICKS) {
            resetIdleSoundRetryCooldown();
            return;
        }

        this.playSound(
                ROACWSoundRegistry.DARK_RAVEN_IDLE.get(),
                0.7F,
                0.85F + this.random.nextFloat() * 0.20F
        );

        resetIdleSoundCooldown();

        int groupCooldown = IDLE_SOUND_GROUP_MIN_COOLDOWN_TICKS
                + this.random.nextInt(
                IDLE_SOUND_GROUP_MAX_COOLDOWN_TICKS - IDLE_SOUND_GROUP_MIN_COOLDOWN_TICKS + 1
        );

        nextIdleSoundAllowedGameTime.put(ownerId, gameTime + groupCooldown);
    }

    @Override
    public void setTarget(@Nullable LivingEntity target) {
        if (this.isCharging() && target != this.getTarget()) {
            return;
        }

        super.setTarget(target);
    }

    public boolean isCharging() {
        return this.entityData.get(DATA_CHARGING);
    }

    private void setCharging(boolean charging) {
        this.entityData.set(DATA_CHARGING, charging);
    }

    private boolean isChargeAnimating() {
        return this.entityData.get(DATA_SHOW_CHARGE_ANIM);
    }

    private void setChargeAnimating(boolean animating) {
        this.entityData.set(DATA_SHOW_CHARGE_ANIM, animating);
    }

    public boolean consumeFinishedCharge() {
        if (!finishedCharge) {
            return false;
        }

        finishedCharge = false;
        return true;
    }

    public DarkRavenMoveControl getRavenMoveControl() {
        return (DarkRavenMoveControl) this.moveControl;
    }

    public void moveToward(double x, double y, double z, double speed) {
        getRavenMoveControl().moveToward(x, y, z, speed);
    }

    public void moveToward(Entity target, double speed) {
        if (target == null) {
            return;
        }

        moveToward(target.getX(), target.getY(), target.getZ(), speed);
    }

    public void moveToRange(LivingEntity target, double desiredDistance, double speed) {
        if (target == null) {
            return;
        }

        Vec3 toSelf = this.position().subtract(target.position());

        if (toSelf.lengthSqr() < 0.001D) {
            toSelf = this.getLookAngle();
        }

        Vec3 destination = target.position().add(toSelf.normalize().scale(desiredDistance));

        getRavenMoveControl().moveToward(destination.x, destination.y, destination.z, speed);
    }

    public void stopMoving() {
        this.getNavigation().stop();
        getRavenMoveControl().stop();
        this.setDeltaMovement(Vec3.ZERO);
    }

    public void faceTarget(LivingEntity target, float maxTurn) {
        if (target == null) {
            return;
        }

        double dx = target.getX() - this.getX();
        double dz = target.getZ() - this.getZ();

        float targetYaw = (float) (-Math.toDegrees(Math.atan2(dx, dz)));
        float yaw = Mth.rotateIfNecessary(this.getYRot(), targetYaw, maxTurn);

        this.setYRot(yaw);
        this.yRotO = yaw;
        this.setYBodyRot(yaw);
        this.yBodyRotO = yaw;
        this.setYHeadRot(yaw);
        this.yHeadRotO = yaw;
    }

    public void faceDirection(Vec3 targetPos, float maxTurn) {
        double dx = targetPos.x - this.getX();
        double dz = targetPos.z - this.getZ();

        if (dx * dx + dz * dz < 1.0E-4D) {
            return;
        }

        float targetYaw = (float) (-Math.toDegrees(Math.atan2(dx, dz)));
        float yaw = Mth.rotateIfNecessary(this.getYRot(), targetYaw, maxTurn);

        this.setYRot(yaw);
        this.yRotO = yaw;
        this.setYBodyRot(yaw);
        this.yBodyRotO = yaw;
    }

    public boolean isFriendlyRaven(Entity other) {
        if (!(other instanceof DarkRavenEntity that)) {
            return false;
        }

        LivingEntity myOwner = this.getSummonOwner();
        LivingEntity theirOwner = that.getSummonOwner();

        return myOwner != null
                && theirOwner != null
                && myOwner.getUUID().equals(theirOwner.getUUID());
    }

    public boolean isAlly(@Nullable Entity other) {
        if (other == null) {
            return false;
        }

        if (other == this || this.isFriendlyRaven(other)) {
            return true;
        }

        LivingEntity owner = this.getSummonOwner();

        if (owner == null) {
            return false;
        }

        if (other == owner || owner.isAlliedTo(other)) {
            return true;
        }

        UUID ownerId = owner.getUUID();

        if (other instanceof OwnableEntity ownable && ownerId.equals(ownable.getOwnerUUID())) {
            return true;
        }

        if (other instanceof IMagicSummon) {
            Entity theirOwner = SummonManager.getOwner((Entity & IMagicSummon) other);

            return theirOwner != null && ownerId.equals(theirOwner.getUUID());
        }

        return false;
    }

    @Override
    public boolean isPushable() {
        return false;
    }

    @Override
    public boolean canBeCollidedWith() {
        return false;
    }

    @Override
    public boolean canCollideWith(Entity entity) {
        return false;
    }

    @Override
    public void push(Entity entity) {
    }

    @Override
    public void push(double x, double y, double z) {
    }

    @Override
    public void travel(Vec3 travelVector) {
        if (this.isCharging()) {
            this.calculateEntityAnimation(true);
            return;
        }

        this.move(MoverType.SELF, this.getDeltaMovement());
        this.calculateEntityAnimation(true);
    }

    private double getGroundY(double x, double y, double z) {
        double searchTop = Math.max(y + 2.0D, this.level().getMaxBuildHeight());
        double searchBottom = Math.max(
                this.level().getMinBuildHeight(),
                y - CHARGE_GROUND_SEARCH_DISTANCE
        );

        Vec3 start = new Vec3(x, searchTop, z);
        Vec3 end = new Vec3(x, searchBottom, z);

        HitResult hit = this.level().clip(
                new ClipContext(
                        start,
                        end,
                        ClipContext.Block.COLLIDER,
                        ClipContext.Fluid.NONE,
                        this
                )
        );

        if (hit.getType() == HitResult.Type.BLOCK) {
            return hit.getLocation().y;
        }

        return Double.NEGATIVE_INFINITY;
    }

    private double getMinimumChargeY(double x, double y, double z) {
        double groundY = getGroundY(x, y, z);

        if (groundY == Double.NEGATIVE_INFINITY) {
            return Double.NEGATIVE_INFINITY;
        }

        double bottomOffset = this.getBoundingBox().minY - this.getY();
        double floor = groundY + CHARGE_GROUND_CLEARANCE - bottomOffset;

        return Math.min(floor, chargeCeilingY);
    }

    private boolean canOccupyChargePosition(Vec3 position) {
        AABB box = this.getBoundingBox()
                .move(position.subtract(this.position()))
                .inflate(CHARGE_COLLISION_PADDING);

        return this.level().noCollision(this, box);
    }

    private void performDashMovement(Vec3 motion) {
        Vec3 start = this.position();
        Vec3 intendedEnd = start.add(motion);

        double minimumY = getMinimumChargeY(intendedEnd.x, intendedEnd.y, intendedEnd.z);
        Vec3 end = intendedEnd;

        if (minimumY != Double.NEGATIVE_INFINITY && end.y < minimumY) {
            end = new Vec3(end.x, minimumY, end.z);
        }

        HitResult obstruction = this.level().clip(
                new ClipContext(
                        start,
                        end,
                        ClipContext.Block.COLLIDER,
                        ClipContext.Fluid.NONE,
                        this
                )
        );

        if (obstruction.getType() != HitResult.Type.MISS) {
            Vec3 raisedEnd = new Vec3(
                    end.x,
                    Math.max(
                            end.y,
                            obstruction.getLocation().y + CHARGE_GROUND_CLEARANCE
                    ),
                    end.z
            );

            if (canOccupyChargePosition(raisedEnd)) {
                end = raisedEnd;
            } else {
                stopCharge(false);
                return;
            }
        }

        if (!canOccupyChargePosition(end)) {
            double raisedY = end.y + 0.5D;
            double raisedMinimumY = getMinimumChargeY(end.x, raisedY, end.z);

            if (raisedMinimumY != Double.NEGATIVE_INFINITY) {
                raisedY = Math.max(raisedY, raisedMinimumY);
            }

            Vec3 raisedEnd = new Vec3(end.x, raisedY, end.z);

            if (canOccupyChargePosition(raisedEnd)) {
                end = raisedEnd;
            } else {
                stopCharge(false);
                return;
            }
        }

        AABB ravenBox = this.getBoundingBox();

        double minX = Math.min(
                start.x + ravenBox.minX - this.getX(),
                end.x + ravenBox.minX - this.getX()
        );

        double minY = Math.min(
                start.y + ravenBox.minY - this.getY(),
                end.y + ravenBox.minY - this.getY()
        );

        double minZ = Math.min(
                start.z + ravenBox.minZ - this.getZ(),
                end.z + ravenBox.minZ - this.getZ()
        );

        double maxX = Math.max(
                start.x + ravenBox.maxX - this.getX(),
                end.x + ravenBox.maxX - this.getX()
        );

        double maxY = Math.max(
                start.y + ravenBox.maxY - this.getY(),
                end.y + ravenBox.maxY - this.getY()
        );

        double maxZ = Math.max(
                start.z + ravenBox.maxZ - this.getZ(),
                end.z + ravenBox.maxZ - this.getZ()
        );

        AABB sweepBox = new AABB(minX, minY, minZ, maxX, maxY, maxZ)
                .inflate(CHARGE_TARGET_PADDING);

        for (LivingEntity entity : this.level().getEntitiesOfClass(
                LivingEntity.class,
                sweepBox,
                e -> e != this
                        && !(e instanceof DarkRavenEntity)
                        && !this.isAlly(e)
                        && e.isAlive()
        )) {
            if (!intersectsDashPath(start, end, entity)) {
                continue;
            }

            if (!this.chargeVictims.add(entity.getUUID())) {
                continue;
            }

            Vec3 previousVelocity = entity.getDeltaMovement();
            entity.invulnerableTime = 0;

            boolean hit = this.doHurtTarget(entity);

            entity.setDeltaMovement(previousVelocity);

            if (hit) {
                this.level().broadcastEntityEvent(this, (byte) 4);
            }
        }

        this.setPos(end.x, end.y, end.z);
    }

    private boolean intersectsDashPath(Vec3 start, Vec3 end, LivingEntity target) {
        AABB targetBox = target.getBoundingBox().inflate(CHARGE_TARGET_PADDING);
        Vec3 direction = end.subtract(start);
        double length = direction.length();

        if (length < 1.0E-6D) {
            AABB ravenBox = this.getBoundingBox().move(start.subtract(this.position()));
            return ravenBox.inflate(CHARGE_TARGET_PADDING).intersects(targetBox);
        }

        Vec3 step = direction.normalize();

        for (double distance = 0.0D; distance <= length; distance += CHARGE_PATH_STEP) {
            Vec3 position = start.add(step.scale(distance));
            AABB ravenBox = this.getBoundingBox()
                    .move(position.subtract(this.position()))
                    .inflate(CHARGE_TARGET_PADDING);

            if (ravenBox.intersects(targetBox)) {
                return true;
            }
        }

        AABB finalBox = this.getBoundingBox()
                .move(end.subtract(this.position()))
                .inflate(CHARGE_TARGET_PADDING);

        return finalBox.intersects(targetBox);
    }

    @Override
    public boolean causeFallDamage(float fallDistance, float multiplier, DamageSource source) {
        return false;
    }

    @Override
    public void tick() {
        if (!this.level().isClientSide && this.aiNeedsResume) {
            this.resumeAiAfterReload();
        }

        super.tick();

        if (this.level().isClientSide) {
            return;
        }

        tryPlayIdleSound();

        if (this.isCharging()) {
            this.setYRot(chargeYaw);
            this.yBodyRot = chargeYaw;
            this.yHeadRot = chargeYaw;
            this.yBodyRotO = chargeYaw;
            this.yHeadRotO = chargeYaw;
            this.yRotO = chargeYaw;
        }

        LivingEntity owner = getSummonOwner();

        if (this.getTarget() != null) {
            LivingEntity currentTarget = this.getTarget();

            boolean invalid = !currentTarget.isAlive()
                    || (owner != null
                    && owner.distanceToSqr(currentTarget) > MAX_TARGET_DISTANCE_FROM_OWNER_SQR);

            if (invalid) {
                this.setTarget(null);
            }
        }

        if (owner != null && this.getTarget() == null) {
            LivingEntity ownerTarget = owner.getLastHurtMob();

            boolean recentlyHurt = owner.tickCount - owner.getLastHurtMobTimestamp()
                    <= OWNER_TARGET_FRESHNESS_TICKS;

            if (ownerTarget != null
                    && recentlyHurt
                    && ownerTarget.isAlive()
                    && this.distanceToSqr(ownerTarget) < 900.0D) {
                this.setTarget(ownerTarget);
            }
        }

        if (chargeAnimReleaseTicks >= 0) {
            chargeAnimReleaseTicks++;

            if (chargeAnimReleaseTicks >= CHARGE_HIT_FOLLOWTHROUGH_TICKS) {
                setChargeAnimating(false);
                chargeAnimReleaseTicks = -1;
            }
        }

        if (!this.isCharging()) {
            return;
        }

        chargeTicks++;

        if (chargeTicks < CHARGE_WINDUP_TICKS) {
            this.setDeltaMovement(Vec3.ZERO);
            applyWindupHoverBob();
            return;
        }

        if (chargeTicks == CHARGE_WINDUP_TICKS) {
            setChargeAnimating(true);
        }

        if (this.getDeltaMovement().lengthSqr() < 1.0E-4D) {
            Vec3 direction = chargeDestination.subtract(this.position());

            if (direction.lengthSqr() > 1.0E-4D) {
                this.setDeltaMovement(direction.normalize().scale(CHARGE_SPEED));
            }
        }

        Vec3 motion = this.getDeltaMovement();

        if (motion.lengthSqr() > 1.0E-4D) {
            performDashMovement(motion);

            if (!this.isCharging()) {
                return;
            }
        }

        Vec3 chargeLine = chargeDestination.subtract(chargeStart);
        Vec3 current = this.position().subtract(chargeStart);

        if (current.dot(chargeLine) >= chargeLine.lengthSqr()) {
            stopCharge(false);
            return;
        }

        if (chargeTicks >= CHARGE_MAX_TICKS) {
            stopCharge(false);
        }
    }

    public void teleportChargeTo(LivingEntity target) {
        if (target == null || !target.isAlive() || this.isCharging()) {
            return;
        }

        Vec3 landingSpot = findTeleportLandingSpot(target);

        if (landingSpot != null) {
            startCharge(target, landingSpot);
        }
    }

    @Nullable
    private Vec3 findTeleportLandingSpot(LivingEntity target) {
        for (int attempt = 0; attempt < TELEPORT_CHARGE_MAX_ATTEMPTS; attempt++) {
            double angle = this.random.nextDouble() * Math.PI * 2.0D;
            double distance = TELEPORT_CHARGE_MIN_DISTANCE
                    + this.random.nextDouble()
                    * (TELEPORT_CHARGE_MAX_DISTANCE - TELEPORT_CHARGE_MIN_DISTANCE);

            double x = target.getX() + Math.cos(angle) * distance;
            double z = target.getZ() + Math.sin(angle) * distance;
            double y = target.getY() + this.random.nextDouble() * 1.5D;

            AABB testBox = this.getBoundingBox().move(
                    x - this.getX(),
                    y - this.getY(),
                    z - this.getZ()
            );

            if (this.level().noCollision(this, testBox)) {
                return new Vec3(x, y, z);
            }
        }

        return null;
    }

    private void playTeleportEffects(Vec3 position) {
        if (!(this.level() instanceof ServerLevel serverLevel)) {
            return;
        }

        serverLevel.playSound(
                null,
                position.x,
                position.y,
                position.z,
                SoundEvents.ENDERMAN_TELEPORT,
                SoundSource.NEUTRAL,
                0.6F,
                1.0F + this.random.nextFloat() * 0.2F
        );

        serverLevel.sendParticles(
                ParticleTypes.REVERSE_PORTAL,
                position.x,
                position.y + 0.3D,
                position.z,
                12,
                0.3D,
                0.3D,
                0.3D,
                0.02D
        );
    }

    private void applyWindupHoverBob() {
        if (chargeStart == null) {
            return;
        }

        double bobOffset = Math.sin(chargeTicks * WINDUP_HOVER_BOB_SPEED)
                * WINDUP_HOVER_BOB_AMPLITUDE;

        this.setPos(chargeStart.x, chargeStart.y + bobOffset, chargeStart.z);
    }

    public void startCharge(LivingEntity target) {
        startCharge(target, null);
    }

    private void startCharge(LivingEntity target, @Nullable Vec3 teleportTo) {
        if (target == null || !target.isAlive() || this.isCharging()) {
            return;
        }

        getRavenMoveControl().stop();
        chargeVictims.clear();
        chargeTicks = 0;
        setCharging(true);

        if (teleportTo != null) {
            playTeleportEffects(this.position());
            this.teleportTo(teleportTo.x, teleportTo.y, teleportTo.z);
            this.setDeltaMovement(Vec3.ZERO);
            playTeleportEffects(teleportTo);
        }

        long gameTime = this.level().getGameTime();
        LivingEntity summonOwner = getSummonOwner();
        UUID ownerId = summonOwner != null ? summonOwner.getUUID() : this.ownerUUID;

        Long lastPlayed = lastAttackSoundGameTime.get(ownerId);
        boolean groupOnCooldown = lastPlayed != null
                && gameTime >= lastPlayed
                && gameTime - lastPlayed < ATTACK_SOUND_GROUP_COOLDOWN_TICKS;

        if (!groupOnCooldown && this.random.nextFloat() < ATTACK_SOUND_CHANCE) {
            this.playSound(
                    ROACWSoundRegistry.DARK_RAVEN_ATTACK.get(),
                    0.7F,
                    0.85F + this.random.nextFloat() * 0.20F
            );

            lastAttackSoundGameTime.put(ownerId, gameTime);
        }

        chargeStart = this.position();

        AABB targetBox = target.getBoundingBox();
        chargeCeilingY = targetBox.maxY;

        Vec3 targetPoint = new Vec3(target.getX(), targetBox.getCenter().y, target.getZ());

        if (targetBox.getYsize() < 0.1D) {
            targetPoint = new Vec3(
                    target.getX(),
                    target.getY() + CHARGE_TARGET_HEIGHT,
                    target.getZ()
            );
        }

        Vec3 toTarget = targetPoint.subtract(this.position());
        Vec3 direction;

        if (toTarget.lengthSqr() > 1.0E-4D) {
            direction = toTarget.normalize();
        } else {
            direction = getLookAngle().normalize();
        }

        Vec3 pointAtTarget = chargeStart.add(
                direction.scale(Math.max(toTarget.length(), 1.0D))
        );

        double minimumTargetY = getMinimumChargeY(
                pointAtTarget.x,
                pointAtTarget.y,
                pointAtTarget.z
        );

        if (minimumTargetY != Double.NEGATIVE_INFINITY) {
            pointAtTarget = new Vec3(
                    pointAtTarget.x,
                    Math.max(pointAtTarget.y, minimumTargetY),
                    pointAtTarget.z
            );
        }

        Vec3 horizontalDirection = new Vec3(direction.x, 0.0D, direction.z);

        if (horizontalDirection.lengthSqr() < 1.0E-4D) {
            Vec3 lookHorizontal = new Vec3(
                    getLookAngle().x,
                    0.0D,
                    getLookAngle().z
            );

            horizontalDirection = lookHorizontal.lengthSqr() > 1.0E-4D
                    ? lookHorizontal.normalize()
                    : new Vec3(1.0D, 0.0D, 0.0D);
        } else {
            horizontalDirection = horizontalDirection.normalize();
        }

        chargeDestination = pointAtTarget.add(
                horizontalDirection.scale(CHARGE_OVERSHOOT_DISTANCE)
        );

        double minimumDestinationY = getMinimumChargeY(
                chargeDestination.x,
                chargeDestination.y,
                chargeDestination.z
        );

        if (minimumDestinationY != Double.NEGATIVE_INFINITY) {
            chargeDestination = new Vec3(
                    chargeDestination.x,
                    Math.max(chargeDestination.y, minimumDestinationY),
                    chargeDestination.z
            );
        }

        float yaw = (float) (-Math.toDegrees(Math.atan2(direction.x, direction.z)));
        chargeYaw = yaw;

        this.setYRot(yaw);
        this.setYBodyRot(yaw);
        this.setYHeadRot(yaw);
        this.yRotO = yaw;
        this.setDeltaMovement(Vec3.ZERO);
        this.hurtMarked = true;
    }

    public void stopCharge(boolean hit) {
        finishedCharge = true;
        setCharging(false);
        chargeDestination = null;
        chargeStart = null;
        chargeTicks = -1;
        chargeCeilingY = Double.POSITIVE_INFINITY;
        this.getNavigation().stop();
        this.setDeltaMovement(Vec3.ZERO);
        getRavenMoveControl().stop();

        if (hit) {
            chargeAnimReleaseTicks = 0;
        } else {
            setChargeAnimating(false);
            chargeAnimReleaseTicks = -1;
        }
    }

    @Override
    public boolean doHurtTarget(Entity target) {
        if (this.isAlly(target)) {
            return false;
        }

        float baseDamage = (float) this.getAttributeValue(Attributes.ATTACK_DAMAGE);
        float damage = baseDamage + (this.spellPower * DASH_SPELLPOWER_MULTIPLIER);
        LivingEntity summonOwner = this.getSummonOwner();

        if (summonOwner == null) {
            return false;
        }

        DamageSource damageSource = ROACWSpellRegistry.SUMMON_DARK_RAVEN.get()
                .getDamageSource(this, summonOwner);

        boolean success = DamageSources.applyDamage(target, damage, damageSource);

        if (success) {
            this.setLastHurtMob(target);
        }

        return success;
    }

    @Override
    public void setLastHurtByMob(@Nullable LivingEntity entity) {
        if (entity != null && this.isAlly(entity)) {
            return;
        }

        super.setLastHurtByMob(entity);
    }

    @Override
    public void onUnSummon() {
        if (this.level() instanceof ServerLevel serverLevel) {
            serverLevel.playSound(
                    null,
                    this.blockPosition(),
                    ROACWSoundRegistry.DARK_RAVEN_SUMMON.get(),
                    SoundSource.NEUTRAL,
                    0.7F,
                    1.3F
            );
        }

        this.discard();
    }

    @Override
    public void onRemovedFromWorld() {
        super.onRemovedFromWorld();
        onRemovedHelper(this);
    }

    @Override
    public void die(DamageSource damageSource) {
        onDeathHelper();
        super.die(damageSource);
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        if (shouldIgnoreDamage(source)) {
            return false;
        }

        return super.hurt(source, amount);
    }

    @Override
    protected ResourceLocation getDefaultLootTable() {
        return ResourceLocation.fromNamespaceAndPath(ROACW.MODID, "entities/dark_raven");
    }

    @Override
    public boolean removeWhenFarAway(double distanceSq) {
        return false;
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "controller", 3, this::animationPredicate));
    }

    private PlayState animationPredicate(AnimationState<DarkRavenEntity> state) {
        if (this.isChargeAnimating()) {
            state.getController().setAnimation(CHARGE_LOOP_ANIM);
            return PlayState.CONTINUE;
        }

        if (this.isCharging()) {
            state.getController().setAnimation(CHARGE_START_ANIM);
            return PlayState.CONTINUE;
        }

        state.getController().setAnimation(IDLE_ANIM);
        return PlayState.CONTINUE;
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return cache;
    }
}