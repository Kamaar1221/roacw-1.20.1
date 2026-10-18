package net.kamaarion.roacw.entity.summon.baby_plaguebringer;

import io.redspace.ironsspellbooks.damage.DamageSources;
import net.kamaarion.roacw.entity.summon.virili.ViriliEntity;
import net.kamaarion.roacw.registeries.ROACWEffectRegistry;
import net.kamaarion.roacw.registeries.ROACWParticleRegistry;
import net.kamaarion.roacw.registeries.ROACWSpellRegistry;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.AnimationState;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.core.animation.RawAnimation;
import software.bernie.geckolib.core.object.PlayState;
import software.bernie.geckolib.util.GeckoLibUtil;

import javax.annotation.Nullable;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

public class BabyPlaguebringerEntity extends PathfinderMob implements GeoEntity {

    private static final double CHARGE_SPEED = 2.5D;
    private static final float NORMAL_CHARGE_DAMAGE = 2.5F;
    private static final float FINAL_CHARGE_DAMAGE = 7.5F;
    private static final float SPELL_POWER_SCALE = 0.35F;
    private static final int MAX_CHARGES = 3;
    private static final int CHARGE_INTERVAL_TICKS = 20;
    private static final int CHARGE_TRANSITION_TICKS = 5;
    private static final int MAX_CHARGE_TICKS = 30;
    private static final double CHARGE_HIT_DISTANCE = 0.35D;
    private static final int CHARGE_FOLLOWTHROUGH_TICKS = 3;
    private static final double CHARGE_DIVERGE_TOLERANCE = 2.0D;
    private static final int CHARGE_DIVERGE_TICKS = 4;
    private static final double TARGET_RANGE = 24.0D;
    private static final int SPAWN_GRACE_TICKS = 30;

    private static final EntityDataAccessor<Boolean> DATA_CHARGING =
            SynchedEntityData.defineId(BabyPlaguebringerEntity.class, EntityDataSerializers.BOOLEAN);

    private static final EntityDataAccessor<Boolean> DATA_CHARGE_TRANSITION =
            SynchedEntityData.defineId(BabyPlaguebringerEntity.class, EntityDataSerializers.BOOLEAN);

    private static final EntityDataAccessor<Integer> DATA_CHARGE_COUNT =
            SynchedEntityData.defineId(BabyPlaguebringerEntity.class, EntityDataSerializers.INT);

    private static final EntityDataAccessor<Boolean> DATA_FOLLOWTHROUGH =
            SynchedEntityData.defineId(BabyPlaguebringerEntity.class, EntityDataSerializers.BOOLEAN);

    private static final RawAnimation IDLE =
            RawAnimation.begin().thenLoop("BabyPBIdle");

    private static final RawAnimation CHARGE_TRANSITION =
            RawAnimation.begin().thenPlay("BabyPBGChargeTransition");

    private static final RawAnimation CHARGE =
            RawAnimation.begin().thenLoop("BabyPBCharge");

    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    private int chargeCount;
    private int chargeTransitionTicks;
    private int chargeTicks;
    private int chargeCooldown;
    private int spawnGraceTicks = SPAWN_GRACE_TICKS;
    private int followthroughTicks;
    private Vec3 chargeDirection = Vec3.ZERO;

    @Nullable
    private LivingEntity chargeHitTarget;

    private double closestChargeDistanceSqr = Double.MAX_VALUE;
    private int chargeDivergeTicks;
    private Vec3 lastChargeCheckPosition = Vec3.ZERO;

    @Nullable
    private UUID ownerUUID;

    private float spellPower = 1.0F;

    public BabyPlaguebringerEntity(EntityType<? extends PathfinderMob> type, Level level) {
        super(type, level);
        this.setPersistenceRequired();
        this.setNoGravity(true);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return PathfinderMob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 20.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.35D)
                .add(Attributes.FOLLOW_RANGE, 24.0D)
                .add(Attributes.KNOCKBACK_RESISTANCE, 1.0D);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(8, new LookAtPlayerGoal(this, Player.class, 12.0F));
        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(
                this,
                Player.class,
                10,
                true,
                false,
                this::isValidTarget
        ));
        this.targetSelector.addGoal(3, new NearestAttackableTargetGoal<>(
                this,
                Monster.class,
                10,
                true,
                false,
                this::isValidTarget
        ));
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(DATA_CHARGING, false);
        this.entityData.define(DATA_CHARGE_TRANSITION, false);
        this.entityData.define(DATA_CHARGE_COUNT, 0);
        this.entityData.define(DATA_FOLLOWTHROUGH, false);
    }

    @Override
    public void tick() {
        this.setNoGravity(true);

        if (!this.level().isClientSide) {
            this.serverTick();
        }

        super.tick();

        if (!this.level().isClientSide && this.isCharging()) {
            this.checkChargeHit();
        }

        if (this.isCharging() || this.isChargeTransition()) {
            this.getNavigation().stop();
        }

        this.updateBodyRotation();

        if (this.level().isClientSide && this.random.nextInt(4) == 0) {
            this.level().addParticle(
                    new DustParticleOptions(new Vector3f(0.42F, 0.62F, 0.35F), 0.8F),
                    this.getX(),
                    this.getY() + this.getBbHeight() * 0.5D,
                    this.getZ(),
                    0.0D,
                    0.02D,
                    0.0D
            );
        }
    }

    private void serverTick() {
        if (this.followthroughTicks > 0) {
            this.followthroughTicks--;
            this.setDeltaMovement(Vec3.ZERO);

            if (this.followthroughTicks <= 0) {
                this.setFollowthrough(false);
                this.finishCharge();
            }

            return;
        }

        if (this.isCharging()) {
            if (!this.isValidTarget(this.getTarget())) {
                this.cancelCharge();
                this.findTarget();
                return;
            }

            this.tickCharge();
            return;
        }

        if (this.isChargeTransition()) {
            this.tickChargeTransition();
            return;
        }

        if (this.spawnGraceTicks > 0) {
            this.spawnGraceTicks--;

            LivingEntity target = this.getTarget();
            if (this.isValidTarget(target)) {
                this.getLookControl().setLookAt(target, 30.0F, 30.0F);
            }

            return;
        }

        if (this.chargeCooldown > 0) {
            this.chargeCooldown--;

            LivingEntity target = this.getTarget();
            if (this.isValidTarget(target)) {
                this.getLookControl().setLookAt(target, 30.0F, 30.0F);
            } else {
                this.findTarget();
            }

            if (this.chargeCooldown <= 0) {
                this.tryStartNextCharge();
            }

            return;
        }

        LivingEntity target = this.getTarget();

        if (!this.isValidTarget(target)) {
            this.setTarget(null);
            this.findTarget();
            target = this.getTarget();
        }

        if (this.isValidTarget(target)) {
            this.tryStartNextCharge();
        }
    }

    private void findTarget() {
        if (!(this.level() instanceof ServerLevel serverLevel)) {
            return;
        }

        List<LivingEntity> candidates = serverLevel.getEntitiesOfClass(
                LivingEntity.class,
                this.getBoundingBox().inflate(TARGET_RANGE),
                this::isValidTarget
        );

        candidates.sort(Comparator.comparingDouble(this::distanceToSqr));

        if (!candidates.isEmpty()) {
            this.setTarget(candidates.get(0));
        } else {
            this.setTarget(null);
        }
    }

    private boolean isValidTarget(@Nullable LivingEntity target) {
        if (target == null
                || target == this
                || !target.isAlive()
                || target.isRemoved()
                || target.isSpectator()
                || target instanceof BabyPlaguebringerEntity
                || target instanceof ViriliEntity
                || this.distanceToSqr(target) > TARGET_RANGE * TARGET_RANGE) {
            return false;
        }

        if (this.isAlliedTo(target) || target.isAlliedTo(this)) {
            return false;
        }

        return !this.isFriendlyToOwner(target);
    }

    public void setOwnerUUID(@Nullable UUID uuid) {
        this.ownerUUID = uuid;
        this.setTarget(null);
    }

    public void setSpellPower(float spellPower) {
        this.spellPower = spellPower * SPELL_POWER_SCALE;
    }

    public float getSpellPower() {
        return this.spellPower;
    }

    @Nullable
    public UUID getOwnerUUID() {
        return this.ownerUUID;
    }

    @Nullable
    public LivingEntity getOwner() {
        if (this.ownerUUID == null || !(this.level() instanceof ServerLevel serverLevel)) {
            return null;
        }

        Entity resolved = serverLevel.getEntity(this.ownerUUID);

        if (resolved instanceof LivingEntity living) {
            return living;
        }

        return serverLevel.getServer().getPlayerList().getPlayer(this.ownerUUID);
    }

    private boolean isFriendlyToOwner(LivingEntity target) {
        if (this.ownerUUID == null) {
            return false;
        }

        if (this.ownerUUID.equals(target.getUUID())) {
            return true;
        }

        if (target instanceof BabyPlaguebringerEntity baby
                && this.ownerUUID.equals(baby.getOwnerUUID())) {
            return true;
        }

        if (target instanceof ViriliEntity virili
                && this.ownerUUID.equals(virili.getOwnerUUID())) {
            return true;
        }

        LivingEntity owner = this.getOwner();

        if (owner == null) {
            return false;
        }

        if (target == owner || owner.isAlliedTo(target) || target.isAlliedTo(owner)) {
            return true;
        }

        Entity targetOwner = this.resolveTargetOwner(target);

        if (targetOwner == null) {
            return false;
        }

        if (targetOwner == owner || this.ownerUUID.equals(targetOwner.getUUID())) {
            return true;
        }

        return owner.isAlliedTo(targetOwner) || targetOwner.isAlliedTo(owner);
    }

    @Nullable
    private Entity resolveTargetOwner(LivingEntity target) {
        if (target instanceof BabyPlaguebringerEntity baby) {
            return baby.getOwner();
        }

        if (target instanceof ViriliEntity virili) {
            return virili.getOwner();
        }

        return null;
    }

    private void tryStartNextCharge() {
        if (this.chargeCount >= MAX_CHARGES) {
            this.discard();
            return;
        }

        LivingEntity target = this.getTarget();

        if (!this.isValidTarget(target)) {
            this.setTarget(null);
            this.findTarget();
            target = this.getTarget();
        }

        if (!this.isValidTarget(target)) {
            return;
        }

        Vec3 targetPosition = target.position().add(0.0D, target.getBbHeight() * 0.5D, 0.0D);
        Vec3 startPosition = this.position().add(0.0D, this.getBbHeight() * 0.5D, 0.0D);
        Vec3 direction = targetPosition.subtract(startPosition);

        if (direction.lengthSqr() < 0.0001D) {
            direction = new Vec3(
                    -Mth.sin(target.getYRot() * Mth.DEG_TO_RAD),
                    0.0D,
                    Mth.cos(target.getYRot() * Mth.DEG_TO_RAD)
            );
        }

        this.chargeDirection = direction.normalize();
        this.chargeTicks = 0;
        this.chargeHitTarget = null;
        this.setChargeTransition(true);
        this.setCharging(false);
        this.chargeTransitionTicks = CHARGE_TRANSITION_TICKS;
        this.faceDirection(this.chargeDirection);
        this.getNavigation().stop();
        this.setDeltaMovement(Vec3.ZERO);
    }

    private void tickChargeTransition() {
        LivingEntity target = this.getTarget();

        if (!this.isValidTarget(target)) {
            this.cancelCharge();
            this.setTarget(null);
            this.findTarget();
            return;
        }

        Vec3 targetPosition = target.position().add(0.0D, target.getBbHeight() * 0.5D, 0.0D);
        Vec3 startPosition = this.position().add(0.0D, this.getBbHeight() * 0.5D, 0.0D);
        Vec3 direction = targetPosition.subtract(startPosition);

        if (direction.lengthSqr() > 0.0001D) {
            this.chargeDirection = direction.normalize();
            this.faceDirection(this.chargeDirection);
        }

        this.chargeTransitionTicks--;

        if (this.chargeTransitionTicks <= 0) {
            this.beginCharge();
        }
    }

    private void beginCharge() {
        this.setChargeTransition(false);
        this.setCharging(true);
        this.chargeTicks = 0;
        this.chargeHitTarget = null;
        this.lastChargeCheckPosition = this.position();
        this.closestChargeDistanceSqr = Double.MAX_VALUE;
        this.chargeDivergeTicks = 0;
        this.getNavigation().stop();
        this.setDeltaMovement(this.chargeDirection.scale(CHARGE_SPEED));
        this.faceDirection(this.chargeDirection);

        this.level().playSound(
                null,
                this.blockPosition(),
                SoundEvents.BEE_LOOP_AGGRESSIVE,
                SoundSource.HOSTILE,
                0.8F,
                1.6F
        );
    }

    private void tickCharge() {
        this.chargeTicks++;
        this.moveWithChargeDirection();

        LivingEntity chargeTarget = this.getTarget();

        if (this.isValidTarget(chargeTarget)) {
            double distanceSqr = this.distanceToSqr(chargeTarget);

            if (distanceSqr < this.closestChargeDistanceSqr) {
                this.closestChargeDistanceSqr = distanceSqr;
                this.chargeDivergeTicks = 0;
            } else {
                double diverged = Math.sqrt(distanceSqr) - Math.sqrt(this.closestChargeDistanceSqr);

                if (diverged >= CHARGE_DIVERGE_TOLERANCE) {
                    this.chargeDivergeTicks++;
                } else {
                    this.chargeDivergeTicks = 0;
                }

                if (this.chargeDivergeTicks >= CHARGE_DIVERGE_TICKS) {
                    this.finishCharge();
                    return;
                }
            }
        }

        if (this.chargeTicks >= MAX_CHARGE_TICKS) {
            this.finishCharge();
        }
    }

    private void moveWithChargeDirection() {
        if (this.chargeDirection.lengthSqr() < 0.0001D) {
            this.setDeltaMovement(Vec3.ZERO);
            return;
        }

        this.setDeltaMovement(this.chargeDirection.scale(CHARGE_SPEED));
        this.faceDirection(this.chargeDirection);
    }

    private void checkChargeHit() {
        if (this.chargeHitTarget != null) {
            return;
        }

        Vec3 previousCenter = this.lastChargeCheckPosition.add(
                0.0D,
                this.getBbHeight() * 0.5D,
                0.0D
        );

        Vec3 currentCenter = this.position().add(
                0.0D,
                this.getBbHeight() * 0.5D,
                0.0D
        );

        this.lastChargeCheckPosition = this.position();

        AABB searchBox = new AABB(previousCenter, currentCenter).inflate(CHARGE_HIT_DISTANCE + 2.0D);

        List<LivingEntity> entities = this.level().getEntitiesOfClass(
                LivingEntity.class,
                searchBox,
                this::isValidTarget
        );

        LivingEntity closest = null;
        double closestDistanceSqr = Double.MAX_VALUE;

        for (LivingEntity entity : entities) {
            double reach = this.getBbWidth() * 0.5D
                    + entity.getBbWidth() * 0.5D
                    + CHARGE_HIT_DISTANCE;

            double distanceSqr = distanceToSegmentSqr(
                    entity.getBoundingBox().getCenter(),
                    previousCenter,
                    currentCenter
            );

            if (distanceSqr <= reach * reach && distanceSqr < closestDistanceSqr) {
                closest = entity;
                closestDistanceSqr = distanceSqr;
            }
        }

        if (closest == null || !this.isValidTarget(closest)) {
            return;
        }

        this.chargeHitTarget = closest;
        this.setDeltaMovement(Vec3.ZERO);
        this.damageChargeTarget(closest);
        this.followthroughTicks = CHARGE_FOLLOWTHROUGH_TICKS;
        this.setFollowthrough(true);
        this.setCharging(false);
    }

    private static double distanceToSegmentSqr(Vec3 point, Vec3 segmentStart, Vec3 segmentEnd) {
        Vec3 segment = segmentEnd.subtract(segmentStart);
        double lengthSqr = segment.lengthSqr();

        if (lengthSqr < 1.0E-7D) {
            return point.distanceToSqr(segmentStart);
        }

        double t = Mth.clamp(
                point.subtract(segmentStart).dot(segment) / lengthSqr,
                0.0D,
                1.0D
        );

        return point.distanceToSqr(segmentStart.add(segment.scale(t)));
    }

    private void damageChargeTarget(LivingEntity target) {
        if (!this.isValidTarget(target)) {
            return;
        }

        boolean finalCharge = this.chargeCount >= MAX_CHARGES - 1;
        float damage = (finalCharge ? FINAL_CHARGE_DAMAGE : NORMAL_CHARGE_DAMAGE) * this.spellPower;
        LivingEntity summonOwner = this.getOwner();

        if (summonOwner == null) {
            return;
        }

        target.invulnerableTime = 0;

        DamageSource damageSource = ROACWSpellRegistry.SUMMON_VIRILI.get()
                .getDamageSource(this, summonOwner);

        boolean successful = DamageSources.applyDamage(target, damage, damageSource);

        if (successful) {
            this.setLastHurtMob(target);
            target.addEffect(new MobEffectInstance(
                    ROACWEffectRegistry.PLAGUE.get(),
                    200,
                    0,
                    false,
                    false,
                    true
            ), summonOwner);
        }

        if (finalCharge && successful) {
            this.spawnFinalImpactPlagueClouds(target);
        }
    }

    private void spawnFinalImpactPlagueClouds(LivingEntity target) {
        if (!(this.level() instanceof ServerLevel serverLevel)) {
            return;
        }

        for (int i = 0; i < 3; i++) {
            double angle = this.random.nextDouble() * Math.PI * 2.0D;
            double radius = this.random.nextDouble() * 0.5D;
            double x = target.getX() + Math.cos(angle) * radius;
            double y = target.getY() + target.getBbHeight() * 0.5D;
            double z = target.getZ() + Math.sin(angle) * radius;

            serverLevel.sendParticles(
                    ROACWParticleRegistry.PLAGUE_CLOUD.get(),
                    x,
                    y,
                    z,
                    5,
                    0.15D,
                    0.2D,
                    0.15D,
                    0.02D
            );
        }
    }

    private void finishCharge() {
        this.setCharging(false);
        this.setChargeTransition(false);
        this.setFollowthrough(false);
        this.setDeltaMovement(Vec3.ZERO);

        this.chargeCount++;
        this.entityData.set(DATA_CHARGE_COUNT, this.chargeCount);

        if (this.chargeCount >= MAX_CHARGES) {
            this.spawnDissipationEffects();
            this.discard();
            return;
        }

        this.chargeCooldown = CHARGE_INTERVAL_TICKS;
        this.chargeHitTarget = null;

        if (!this.isValidTarget(this.getTarget())) {
            this.setTarget(null);
            this.findTarget();
        }
    }

    private void cancelCharge() {
        this.setCharging(false);
        this.setChargeTransition(false);
        this.setFollowthrough(false);
        this.chargeTransitionTicks = 0;
        this.chargeTicks = 0;
        this.followthroughTicks = 0;
        this.setDeltaMovement(Vec3.ZERO);
        this.chargeHitTarget = null;
    }

    private void spawnDissipationEffects() {
        if (!(this.level() instanceof ServerLevel serverLevel)) {
            return;
        }

        for (int i = 0; i < 18; i++) {
            double angle = this.random.nextDouble() * Math.PI * 2.0D;
            double radius = this.random.nextDouble() * 0.75D;
            double x = this.getX() + Math.cos(angle) * radius;
            double y = this.getY() + this.random.nextDouble() * this.getBbHeight();
            double z = this.getZ() + Math.sin(angle) * radius;

            serverLevel.sendParticles(
                    new DustParticleOptions(new Vector3f(0.42F, 0.62F, 0.35F), 1.25F),
                    x,
                    y,
                    z,
                    1,
                    0.0D,
                    0.08D,
                    0.0D,
                    0.03D
            );
        }

        this.level().playSound(
                null,
                this.blockPosition(),
                SoundEvents.BEE_DEATH,
                SoundSource.HOSTILE,
                0.8F,
                1.5F
        );
    }

    private void faceDirection(Vec3 direction) {
        if (direction.lengthSqr() < 0.0001D) {
            return;
        }

        float yaw = (float) (Mth.atan2(direction.z, direction.x) * 180.0D / Math.PI) - 90.0F;
        float horizontal = (float) Math.sqrt(direction.x * direction.x + direction.z * direction.z);
        float pitch = (float) -(Mth.atan2(direction.y, horizontal) * 180.0D / Math.PI);

        this.setYRot(yaw);
        this.setXRot(pitch);
        this.yRotO = yaw;
        this.xRotO = pitch;
    }

    private void updateBodyRotation() {
        Vec3 movement = this.isCharging() || this.isFollowthrough()
                ? this.chargeDirection
                : this.getDeltaMovement();

        if (movement.lengthSqr() < 0.0001D) {
            return;
        }

        float yaw = (float) (Mth.atan2(movement.z, movement.x) * 180.0D / Math.PI) - 90.0F;
        this.setYRot(yaw);
        this.yBodyRot = yaw;
        this.yHeadRot = yaw;
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        Entity attacker = source.getEntity();

        if (attacker == this) {
            return false;
        }

        if (attacker instanceof LivingEntity living && !this.isValidTarget(living)) {
            return false;
        }

        return super.hurt(source, amount);
    }

    @Override
    protected void doPush(Entity entity) {
    }

    @Override
    public boolean isPushable() {
        return false;
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);

        tag.putInt("ChargeCount", this.chargeCount);
        tag.putInt("ChargeCooldown", this.chargeCooldown);
        tag.putInt("ChargeTicks", this.chargeTicks);
        tag.putBoolean("Charging", this.isCharging());

        if (this.ownerUUID != null) {
            tag.putUUID("OwnerUUID", this.ownerUUID);
        }
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);

        this.chargeCount = tag.getInt("ChargeCount");
        this.chargeCooldown = tag.getInt("ChargeCooldown");
        this.chargeTicks = tag.getInt("ChargeTicks");
        this.ownerUUID = tag.hasUUID("OwnerUUID") ? tag.getUUID("OwnerUUID") : null;

        this.setChargeTransition(false);
        this.setCharging(false);
        this.setFollowthrough(false);

        this.chargeTransitionTicks = 0;
        this.followthroughTicks = 0;
        this.chargeHitTarget = null;
        this.chargeDirection = Vec3.ZERO;
        this.closestChargeDistanceSqr = Double.MAX_VALUE;
        this.chargeDivergeTicks = 0;
        this.lastChargeCheckPosition = this.position();

        this.entityData.set(DATA_CHARGE_COUNT, this.chargeCount);

        if (this.chargeCount >= MAX_CHARGES) {
            this.discard();
        }
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(
                this,
                "BabyPlaguebringerController",
                0,
                this::animationPredicate
        ));
    }

    private PlayState animationPredicate(AnimationState<BabyPlaguebringerEntity> state) {
        if (this.isChargeTransition()) {
            state.getController().setAnimation(CHARGE_TRANSITION);
            return PlayState.CONTINUE;
        }

        if (this.isCharging()) {
            state.getController().setAnimation(CHARGE);
            return PlayState.CONTINUE;
        }

        state.getController().setAnimation(IDLE);
        return PlayState.CONTINUE;
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.cache;
    }

    @Override
    public void lerpTo(
            double x,
            double y,
            double z,
            float yRot,
            float xRot,
            int lerpSteps,
            boolean teleport
    ) {
        super.lerpTo(x, y, z, yRot, xRot, lerpSteps, teleport);
    }

    public boolean isCharging() {
        return this.entityData.get(DATA_CHARGING);
    }

    public boolean isFollowthrough() {
        return this.entityData.get(DATA_FOLLOWTHROUGH);
    }

    private void setFollowthrough(boolean followthrough) {
        this.entityData.set(DATA_FOLLOWTHROUGH, followthrough);
    }

    private void setCharging(boolean charging) {
        this.entityData.set(DATA_CHARGING, charging);
    }

    public boolean isChargeTransition() {
        return this.entityData.get(DATA_CHARGE_TRANSITION);
    }

    private void setChargeTransition(boolean transition) {
        this.entityData.set(DATA_CHARGE_TRANSITION, transition);
    }

    public int getChargeCount() {
        return this.entityData.get(DATA_CHARGE_COUNT);
    }

    @Override
    public boolean causeFallDamage(float fallDistance, float multiplier, DamageSource source) {
        return false;
    }

    @Override
    protected void dropCustomDeathLoot(DamageSource source, int looting, boolean recentlyHit) {
    }
}