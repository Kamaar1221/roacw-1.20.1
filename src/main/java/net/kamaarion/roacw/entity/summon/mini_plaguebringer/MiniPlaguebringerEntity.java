package net.kamaarion.roacw.entity.summon.mini_plaguebringer;

import io.redspace.ironsspellbooks.capabilities.magic.SummonManager;
import io.redspace.ironsspellbooks.entity.mobs.IMagicSummon;
import net.kamaarion.roacw.entity.projectile.homing_plague_missile.HomingPlagueMissileEntity;
import net.kamaarion.roacw.entity.projectile.plague_missile.PlagueMissileEntity;
import net.kamaarion.roacw.entity.summon.baby_plaguebringer.BabyPlaguebringerEntity;
import net.kamaarion.roacw.entity.summon.virili.ViriliEntity;
import net.kamaarion.roacw.registeries.ROACWEntityRegistry;
import net.kamaarion.roacw.registeries.ROACWSpellRegistry;
import net.kamaarion.roacw.registeries.ROACWSoundRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.RawAnimation;
import software.bernie.geckolib.util.GeckoLibUtil;

import javax.annotation.Nullable;
import java.util.EnumSet;

public class MiniPlaguebringerEntity extends PathfinderMob implements GeoEntity, IMagicSummon {

    private static final EntityDataAccessor<Boolean> DATA_ATTACKING =
            SynchedEntityData.defineId(
                    MiniPlaguebringerEntity.class,
                    EntityDataSerializers.BOOLEAN
            );

    private static final EntityDataAccessor<Integer> DATA_FORMATION_INDEX =
            SynchedEntityData.defineId(
                    MiniPlaguebringerEntity.class,
                    EntityDataSerializers.INT
            );

    private static final EntityDataAccessor<Integer> DATA_FORMATION_COUNT =
            SynchedEntityData.defineId(
                    MiniPlaguebringerEntity.class,
                    EntityDataSerializers.INT
            );

    private static final double FORMATION_CORNER_DISTANCE = 2.75D;
    private static final double FORMATION_REAR_DISTANCE = 1.75D;
    private static final double FORMATION_SLOT_SPACING_X = 1.75D;
    private static final double FORMATION_SLOT_SPACING_Z = 1.75D;
    private static final double FORMATION_HEIGHT = 2.0D;
    private static final double FORMATION_FOLLOW_SPEED = 0.5D;
    private static final double FORMATION_STOP_DISTANCE = 0.40D;
    private static final double FORMATION_VELOCITY_LERP = 0.18D;
    private static final double FORMATION_TELEPORT_DISTANCE = 40.0D;
    private static final int FORMATION_RESOLVE_STEPS = 10;
    private static final int FORMATION_STUCK_TICKS = 15;
    private static final double FORMATION_STUCK_MIN_DISTANCE = 1.0D;
    private static final double FORMATION_STUCK_MIN_MOVE_SQR = 0.03D * 0.03D;

    public static final double TARGET_RANGE = 20.0D;
    public static final double ATTACK_RANGE = 20.0D;

    private static final int OWNER_TARGET_FRESHNESS_TICKS = 100;

    private static final float ATTACK_DAMAGE = 8.0F;
    private static final int ATTACK_COOLDOWN = 30;
    private static final int ATTACK_STAGGER_TICKS = 10;
    private static final int ATTACK_ANIMATION_TICKS = 14;
    private static final int ATTACK_FIRE_TICK = 7;
    private static final float HOMING_MISSILE_CHANCE = 0.35F;

    private int attackCooldown;
    private int attackAnimationTicks;

    @Nullable
    private LivingEntity pendingAttackTarget;

    private boolean missileFired;

    private Vec3 lastFormationPosition = Vec3.ZERO;
    private int formationStuckTicks;

    private static final RawAnimation IDLE_ANIMATION =
            RawAnimation.begin().thenLoop("Idle");

    private static final RawAnimation ATTACK_ANIMATION =
            RawAnimation.begin().thenPlay("Attack");

    private final AnimatableInstanceCache cache =
            GeckoLibUtil.createInstanceCache(this);

    private float spellPower;

    public MiniPlaguebringerEntity(
            EntityType<? extends PathfinderMob> entityType,
            Level level
    ) {
        super(entityType, level);
        this.setNoGravity(true);
        this.noPhysics = false;
    }

    public static AttributeSupplier.Builder createAttributes() {
        return PathfinderMob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 20.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.35D)
                .add(Attributes.FOLLOW_RANGE, TARGET_RANGE)
                .add(Attributes.ATTACK_DAMAGE, ATTACK_DAMAGE)
                .add(Attributes.KNOCKBACK_RESISTANCE, 1.0D);
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(DATA_ATTACKING, false);
        this.entityData.define(DATA_FORMATION_INDEX, 0);
        this.entityData.define(DATA_FORMATION_COUNT, 1);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(1, new MiniPlaguebringerAttackGoal(this));
        this.goalSelector.addGoal(2, new LookAtPlayerGoal(this, Player.class, 8.0F));

        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
        this.targetSelector.addGoal(
                2,
                new NearestAttackableTargetGoal<>(
                        this,
                        Monster.class,
                        10,
                        true,
                        false,
                        this::isValidTarget
                )
        );
    }

    @Override
    public void tick() {
        super.tick();

        if (this.level().isClientSide) {
            return;
        }

        if (this.attackCooldown > 0) {
            this.attackCooldown--;
        }

        if (this.attackAnimationTicks > 0) {
            this.attackAnimationTicks--;

            int elapsedTicks = ATTACK_ANIMATION_TICKS - this.attackAnimationTicks;

            if (!this.missileFired && elapsedTicks == ATTACK_FIRE_TICK) {
                this.fireMissile();
                this.missileFired = true;
            }

            if (this.attackAnimationTicks <= 0) {
                this.setAttacking(false);
                this.pendingAttackTarget = null;
            }
        }

        LivingEntity owner = this.getSummonOwner();

        if (owner == null || !owner.isAlive()) {
            this.setTarget(null);
            this.pendingAttackTarget = null;
            return;
        }

        this.updateFormationMovement(owner);

        LivingEntity ownerTarget = owner.getLastHurtMob();

        if (ownerTarget != null
                && ownerTarget.isAlive()
                && ownerTarget != owner
                && owner.distanceToSqr(ownerTarget) <= TARGET_RANGE * TARGET_RANGE
                && owner.getLastHurtMobTimestamp()
                >= owner.tickCount - OWNER_TARGET_FRESHNESS_TICKS
                && this.isValidTarget(ownerTarget)) {
            this.setTarget(ownerTarget);
        }

        LivingEntity target = this.getTarget();

        if (target != null
                && (!this.isValidTarget(target)
                || owner.distanceToSqr(target) > TARGET_RANGE * TARGET_RANGE)) {
            this.setTarget(null);
            target = null;
        }

        if (target != null) {
            double dx = target.getX() - this.getX();
            double dz = target.getZ() - this.getZ();

            if (Math.abs(dx) > 1.0E-6D || Math.abs(dz) > 1.0E-6D) {
                float targetYaw = (float) (
                        Math.atan2(dz, dx) * (180.0D / Math.PI)
                ) - 90.0F;

                this.setYRot(targetYaw);
                this.setYHeadRot(targetYaw);
                this.setYBodyRot(targetYaw);

                this.yRotO = targetYaw;
                this.yHeadRotO = targetYaw;
            }

            this.getLookControl().setLookAt(
                    target.getX(),
                    target.getY() + target.getBbHeight() * 0.5D,
                    target.getZ(),
                    180.0F,
                    20.0F
            );
        } else {
            float ownerYaw = owner.getYRot();

            this.setYRot(ownerYaw);
            this.setYHeadRot(ownerYaw);
            this.setYBodyRot(ownerYaw);

            this.yRotO = ownerYaw;
            this.yHeadRotO = ownerYaw;
        }
    }

    public boolean isTargetInAttackRange(@Nullable LivingEntity target) {
        if (!this.isValidTarget(target)) {
            return false;
        }

        LivingEntity owner = this.getSummonOwner();

        return owner != null
                && owner.isAlive()
                && owner.distanceToSqr(target) <= ATTACK_RANGE * ATTACK_RANGE;
    }

    public boolean isTargetInTargetRange(@Nullable LivingEntity target) {
        if (!this.isValidTarget(target)) {
            return false;
        }

        LivingEntity owner = this.getSummonOwner();

        return owner != null
                && owner.isAlive()
                && owner.distanceToSqr(target) <= TARGET_RANGE * TARGET_RANGE;
    }

    private boolean isBoxFreeAt(Vec3 position) {
        AABB box = this.getBoundingBox().move(position.subtract(this.position()));

        return !this.level().getBlockCollisions(this, box).iterator().hasNext();
    }

    private boolean isWallBetween(Vec3 from, Vec3 to) {
        BlockHitResult hit = this.level().clip(new ClipContext(
                from,
                to,
                ClipContext.Block.COLLIDER,
                ClipContext.Fluid.NONE,
                this
        ));

        return hit.getType() != HitResult.Type.MISS;
    }

    private Vec3 resolveFormationPosition(LivingEntity owner, Vec3 desiredPosition) {
        Vec3 anchor = owner.position().add(0.0D, owner.getBbHeight() * 0.5D, 0.0D);

        for (int step = 0; step <= FORMATION_RESOLVE_STEPS; step++) {
            double t = 1.0D - (double) step / FORMATION_RESOLVE_STEPS;
            Vec3 candidate = anchor.lerp(desiredPosition, t);

            if (!this.isBoxFreeAt(candidate)) {
                continue;
            }

            if (this.isWallBetween(anchor, candidate)) {
                continue;
            }

            return candidate;
        }

        return anchor;
    }

    private void updateFormationMovement(LivingEntity owner) {
        Vec3 formationOffset = this.getFormationOffset();

        Vec3 rotatedOffset = formationOffset.yRot(
                -owner.getYRot() * ((float) Math.PI / 180.0F)
        );

        Vec3 desiredPosition = this.resolveFormationPosition(
                owner,
                owner.position().add(rotatedOffset)
        );

        Vec3 difference = desiredPosition.subtract(this.position());
        double distanceSqr = difference.lengthSqr();

        double movedSqr = this.position().distanceToSqr(this.lastFormationPosition);
        this.lastFormationPosition = this.position();

        if (distanceSqr > FORMATION_TELEPORT_DISTANCE * FORMATION_TELEPORT_DISTANCE) {
            this.formationStuckTicks = 0;

            if (this.level().noCollision(
                    this,
                    this.getBoundingBox().move(difference)
            )) {
                this.teleportTo(
                        desiredPosition.x,
                        desiredPosition.y,
                        desiredPosition.z
                );
                this.setDeltaMovement(Vec3.ZERO);
            } else {
                this.setDeltaMovement(Vec3.ZERO);
            }

            return;
        }

        double distance = Math.sqrt(distanceSqr);

        boolean embedded = !this.isBoxFreeAt(this.position());
        boolean notProgressing = distance > FORMATION_STUCK_MIN_DISTANCE
                && movedSqr < FORMATION_STUCK_MIN_MOVE_SQR;

        if (embedded || notProgressing) {
            this.formationStuckTicks++;
        } else {
            this.formationStuckTicks = 0;
        }

        if (this.formationStuckTicks >= FORMATION_STUCK_TICKS) {
            this.formationStuckTicks = 0;
            this.teleportTo(
                    desiredPosition.x,
                    desiredPosition.y,
                    desiredPosition.z
            );
            this.setDeltaMovement(Vec3.ZERO);
            this.lastFormationPosition = this.position();
            return;
        }

        if (distance <= FORMATION_STOP_DISTANCE) {
            this.setDeltaMovement(this.getDeltaMovement().scale(0.80D));
            return;
        }

        Vec3 desiredVelocity = difference.normalize().scale(FORMATION_FOLLOW_SPEED);

        Vec3 smoothedVelocity = this.getDeltaMovement().lerp(
                desiredVelocity,
                FORMATION_VELOCITY_LERP
        );

        Vec3 predictedMovement = smoothedVelocity;
        AABB predictedBox = this.getBoundingBox().move(predictedMovement);

        if (!this.level().noCollision(this, predictedBox)) {
            boolean canMoveX = Math.abs(predictedMovement.x) > 1.0E-6D
                    && this.level().noCollision(
                    this,
                    this.getBoundingBox().move(predictedMovement.x, 0.0D, 0.0D)
            );

            boolean canMoveZ = Math.abs(predictedMovement.z) > 1.0E-6D
                    && this.level().noCollision(
                    this,
                    this.getBoundingBox().move(0.0D, 0.0D, predictedMovement.z)
            );

            boolean canMoveY = Math.abs(predictedMovement.y) > 1.0E-6D
                    && this.level().noCollision(
                    this,
                    this.getBoundingBox().move(0.0D, predictedMovement.y, 0.0D)
            );

            double safeX = canMoveX ? predictedMovement.x : 0.0D;
            double safeY = canMoveY ? predictedMovement.y : 0.0D;
            double safeZ = canMoveZ ? predictedMovement.z : 0.0D;

            if (Math.abs(safeX) <= 1.0E-6D
                    && Math.abs(safeY) <= 1.0E-6D
                    && Math.abs(safeZ) <= 1.0E-6D) {
                this.setDeltaMovement(Vec3.ZERO);
                return;
            }

            smoothedVelocity = new Vec3(safeX, safeY, safeZ);
        }

        this.setDeltaMovement(smoothedVelocity);
    }

    public Vec3 getFormationOffset() {
        int index = Math.max(0, this.getFormationIndex());

        boolean rightSide = index % 2 == 0;
        int sideIndex = index / 2;
        int column = sideIndex % 2;
        int row = sideIndex / 2;

        double localX = (column - 0.5D) * FORMATION_SLOT_SPACING_X;
        double localZ = -(row * FORMATION_SLOT_SPACING_Z);

        if (!rightSide) {
            localX = -localX;
        }

        double sideCenterX = rightSide
                ? FORMATION_CORNER_DISTANCE
                : -FORMATION_CORNER_DISTANCE;

        double sideCenterZ = -FORMATION_REAR_DISTANCE;

        return new Vec3(
                sideCenterX + localX,
                FORMATION_HEIGHT,
                sideCenterZ + localZ
        );
    }

    public void setFormation(int index, int count) {
        this.entityData.set(DATA_FORMATION_INDEX, Math.max(0, index));
        this.entityData.set(DATA_FORMATION_COUNT, Math.max(1, count));
    }

    public int getFormationIndex() {
        return this.entityData.get(DATA_FORMATION_INDEX);
    }

    public int getFormationCount() {
        return this.entityData.get(DATA_FORMATION_COUNT);
    }

    public boolean isAttacking() {
        return this.entityData.get(DATA_ATTACKING);
    }

    public void setAttacking(boolean attacking) {
        this.entityData.set(DATA_ATTACKING, attacking);
    }

    public boolean canAttackNow() {
        if (this.attackCooldown > 0) {
            return false;
        }

        int count = Math.max(1, this.getFormationCount());
        int cycleTicks = Math.max(ATTACK_COOLDOWN, count * ATTACK_STAGGER_TICKS);
        int slotStart = (Math.max(0, this.getFormationIndex()) % count) * ATTACK_STAGGER_TICKS;
        long phase = this.level().getGameTime() % cycleTicks;

        return phase >= slotStart && phase < slotStart + ATTACK_STAGGER_TICKS;
    }

    public void performAttack(LivingEntity target) {
        if (!this.canAttackNow()
                || !this.isValidTarget(target)
                || !this.isTargetInAttackRange(target)) {
            return;
        }

        this.pendingAttackTarget = target;
        this.missileFired = false;
        this.attackCooldown = ATTACK_COOLDOWN;
        this.triggerAttackAnimation();
    }

    private void fireMissile() {
        LivingEntity target = this.pendingAttackTarget;

        if (!this.isValidTarget(target)
                || !this.isTargetInAttackRange(target)) {
            this.pendingAttackTarget = null;
            this.setTarget(null);
            return;
        }

        if (!(this.level() instanceof ServerLevel serverLevel)) {
            return;
        }

        Vec3 spawnPosition = this.position();

        Vec3 direction = target.position()
                .add(0.0D, target.getBbHeight() * 0.5D, 0.0D)
                .subtract(spawnPosition);

        if (direction.lengthSqr() <= 1.0E-6D) {
            return;
        }

        this.level().playSound(
                null,
                this.getX(),
                this.getY(),
                this.getZ(),
                ROACWSoundRegistry.PLAGUE_MISSILE_LAUNCH.get(),
                SoundSource.HOSTILE,
                1.0F,
                1.0F
        );

        float missileDamage = Math.max(1.0F, this.getSpellPower());
        float missileSplashDamage = Math.max(0.25F, this.getSpellPower() * 0.25F);

        boolean fireHoming = this.random.nextFloat() < HOMING_MISSILE_CHANCE;

        if (fireHoming) {
            HomingPlagueMissileEntity homingMissile =
                    new HomingPlagueMissileEntity(
                            serverLevel,
                            this,
                            target,
                            missileDamage,
                            spawnPosition,
                            ROACWSpellRegistry.SUMMON_MINI_PLAGUEBRINGER.get()
                    );

            serverLevel.addFreshEntity(homingMissile);
            return;
        }

        PlagueMissileEntity missile = new PlagueMissileEntity(
                ROACWEntityRegistry.PLAGUE_MISSILE.get(),
                serverLevel,
                this,
                this.getSummonOwner(),
                spawnPosition,
                direction,
                ROACWSpellRegistry.SUMMON_MINI_PLAGUEBRINGER.get()
        );

        missile.setDamage(missileDamage);
        missile.setSplashDamage(missileSplashDamage);

        serverLevel.addFreshEntity(missile);
    }

    private boolean isValidTarget(@Nullable LivingEntity target) {
        if (target == null
                || target == this
                || !target.isAlive()
                || target.isRemoved()
                || target.isSpectator()) {
            return false;
        }

        if (this.isFriendlySummon(target)) {
            return false;
        }

        LivingEntity owner = this.getSummonOwner();

        return owner != null
                && owner.isAlive()
                && owner.distanceToSqr(target) <= TARGET_RANGE * TARGET_RANGE;
    }

    private void triggerAttackAnimation() {
        this.setAttacking(true);
        this.attackAnimationTicks = ATTACK_ANIMATION_TICKS;
    }

    @Nullable
    public LivingEntity getSummonOwner() {
        Entity owner = SummonManager.getOwner(this);
        return owner instanceof LivingEntity living ? living : null;
    }

    @Nullable
    private LivingEntity getEntitySummonOwner(Entity entity) {
        if (entity instanceof MiniPlaguebringerEntity mini) {
            return mini.getSummonOwner();
        }

        if (entity instanceof ViriliEntity virili) {
            return virili.getOwner();
        }

        if (entity instanceof BabyPlaguebringerEntity baby) {
            return baby.getOwner();
        }

        if (entity instanceof IMagicSummon) {
            Entity owner = SummonManager.getOwner(entity);

            if (owner instanceof LivingEntity living) {
                return living;
            }
        }

        return null;
    }

    public boolean isFriendlySummon(@Nullable LivingEntity entity) {
        if (entity == null || entity == this) {
            return entity == this;
        }

        LivingEntity owner = this.getSummonOwner();

        if (owner == null) {
            return this.isSummonEntity(entity);
        }

        if (entity == owner || entity.getUUID().equals(owner.getUUID())) {
            return true;
        }

        if (entity.isAlliedTo(owner) || owner.isAlliedTo(entity)) {
            return true;
        }

        LivingEntity targetOwner = this.getEntitySummonOwner(entity);

        if (targetOwner != null) {
            if (owner.getUUID().equals(targetOwner.getUUID())) {
                return true;
            }

            if (owner.isAlliedTo(targetOwner) || targetOwner.isAlliedTo(owner)) {
                return true;
            }
        } else if (this.isSummonEntity(entity)) {
            return true;
        }

        return false;
    }

    private boolean isSummonEntity(Entity entity) {
        return entity instanceof IMagicSummon
                || entity instanceof MiniPlaguebringerEntity
                || entity instanceof BabyPlaguebringerEntity
                || entity instanceof ViriliEntity;
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        Entity attacker = source.getEntity();

        if (attacker instanceof LivingEntity livingAttacker
                && this.isFriendlySummon(livingAttacker)) {
            return false;
        }

        return super.hurt(source, amount);
    }

    @Override
    public boolean isPushable() {
        return false;
    }

    @Override
    protected void doPush(Entity entity) {
    }

    @Override
    protected void pushEntities() {
    }

    @Override
    public boolean canBeCollidedWith() {
        return false;
    }

    @Override
    public boolean isPickable() {
        return true;
    }

    @Override
    public void travel(Vec3 travelVector) {
        this.move(MoverType.SELF, this.getDeltaMovement());
    }

    @Override
    protected void checkFallDamage(
            double y,
            boolean onGround,
            BlockState state,
            BlockPos pos
    ) {
    }

    @Override
    public boolean causeFallDamage(
            float fallDistance,
            float multiplier,
            DamageSource source
    ) {
        return false;
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return null;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return null;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return null;
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);

        tag.putInt("FormationIndex", this.getFormationIndex());
        tag.putInt("FormationCount", this.getFormationCount());
        tag.putFloat("SpellPower", this.spellPower);
        tag.putInt("AttackCooldown", this.attackCooldown);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);

        this.setFormation(
                tag.getInt("FormationIndex"),
                Math.max(1, tag.getInt("FormationCount"))
        );

        this.spellPower = tag.getFloat("SpellPower");
        this.attackCooldown = Math.max(0, tag.getInt("AttackCooldown"));
    }

    public float getSpellPower() {
        return this.spellPower;
    }

    public void setSpellPower(float spellPower) {
        this.spellPower = Math.max(0.0F, spellPower);
    }

    @Override
    public void onUnSummon() {
        this.discard();
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(
                new AnimationController<>(
                        this,
                        "Idle",
                        0,
                        state -> {
                            if (this.isAttacking()) {
                                return state.setAndContinue(ATTACK_ANIMATION);
                            }

                            return state.setAndContinue(IDLE_ANIMATION);
                        }
                )
        );
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.cache;
    }

    @Override
    public boolean shouldDropExperience() {
        return false;
    }

    @Override
    public boolean removeWhenFarAway(double distanceToClosestPlayer) {
        return false;
    }

    @Override
    public boolean canAttack(LivingEntity target) {
        return this.isValidTarget(target) && super.canAttack(target);
    }

    @Override
    public boolean canBeSeenAsEnemy() {
        return false;
    }

    @Override
    public boolean isNoAi() {
        return false;
    }

    @Override
    public void setTarget(@Nullable LivingEntity target) {
        if (target != null && !this.isValidTarget(target)) {
            super.setTarget(null);
            this.pendingAttackTarget = null;
            return;
        }

        super.setTarget(target);
    }

    @Override
    public void die(DamageSource source) {
        super.die(source);
    }

    private static class MiniPlaguebringerAttackGoal extends Goal {

        private final MiniPlaguebringerEntity plaguebringer;

        public MiniPlaguebringerAttackGoal(MiniPlaguebringerEntity plaguebringer) {
            this.plaguebringer = plaguebringer;
            this.setFlags(EnumSet.of(Flag.LOOK));
        }

        @Override
        public boolean canUse() {
            LivingEntity target = this.plaguebringer.getTarget();

            return this.plaguebringer.isValidTarget(target)
                    && this.plaguebringer.isTargetInAttackRange(target);
        }

        @Override
        public boolean canContinueToUse() {
            LivingEntity target = this.plaguebringer.getTarget();

            return this.plaguebringer.isValidTarget(target)
                    && this.plaguebringer.isTargetInAttackRange(target);
        }

        @Override
        public void tick() {
            LivingEntity target = this.plaguebringer.getTarget();

            if (!this.plaguebringer.isValidTarget(target)) {
                this.plaguebringer.setTarget(null);
                return;
            }

            this.plaguebringer.getLookControl().setLookAt(target, 180.0F, 180.0F);

            if (this.plaguebringer.canAttackNow()) {
                this.plaguebringer.performAttack(target);
            }
        }
    }
}