package net.kamaarion.roacw.entity.projectile.homing_plague_missile;

import io.redspace.ironsspellbooks.api.spells.AbstractSpell;
import io.redspace.ironsspellbooks.capabilities.magic.SummonManager;
import io.redspace.ironsspellbooks.damage.DamageSources;
import net.kamaarion.roacw.Utils;
import net.kamaarion.roacw.entity.summon.mini_plaguebringer.MiniPlaguebringerEntity;
import net.kamaarion.roacw.entity.summon.virili.ViriliEntity;
import net.kamaarion.roacw.registeries.ROACWEffectRegistry;
import net.kamaarion.roacw.registeries.ROACWEntityRegistry;
import net.kamaarion.roacw.registeries.ROACWSoundRegistry;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.projectile.AbstractHurtingProjectile;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.util.GeckoLibUtil;

import javax.annotation.Nullable;
import java.util.Comparator;
import java.util.List;

public class HomingPlagueMissileEntity extends AbstractHurtingProjectile implements GeoEntity {

    private static final EntityDataAccessor<Float> DATA_MISSILE_DAMAGE =
            SynchedEntityData.defineId(
                    HomingPlagueMissileEntity.class,
                    EntityDataSerializers.FLOAT
            );

    private static final float HOMING_DAMAGE_MULTIPLIER = 0.5F;
    private static final float MAX_TURN_DEGREES_PER_TICK = 25.0F;
    private static final double SPEED = 1.25D;
    private static final double HIT_TOLERANCE = 0.3D;
    private static final int MAX_LIFETIME = 200;
    private static final int NO_TARGET_MAX_LIFE_TICKS = 100;
    private static final double REACQUIRE_BASE_RADIUS = 10.0D;
    private static final double REACQUIRE_GROWTH_PER_SECOND = 6.0D;
    private static final double REACQUIRE_MAX_RADIUS = 32.0D;
    private static final int REACQUIRE_INTERVAL = 5;
    private static final float MAX_BANK_ANGLE = 18.0F;
    private static final float BANK_SMOOTHING = 0.20F;
    private static final int PLAGUE_DURATION = 200;
    private static final float DEFAULT_DAMAGE = 2.0F;
    private static final double SMOKE_MIN_BACK_DISTANCE = 0.40D;
    private static final double SMOKE_MAX_BACK_DISTANCE = 0.65D;
    private static final double SMOKE_SPREAD = 0.18D;

    @Nullable
    private LivingEntity target;

    private int life;
    private float bankAngle;
    private int ticksSinceReacquire;

    @Nullable
    private AbstractSpell summonSpell;

    private final AnimatableInstanceCache cache =
            GeckoLibUtil.createInstanceCache(this);

    public HomingPlagueMissileEntity(
            EntityType<? extends HomingPlagueMissileEntity> type,
            Level level
    ) {
        super(type, level);
        this.noPhysics = false;
    }

    public HomingPlagueMissileEntity(
            Level level,
            LivingEntity shooter,
            LivingEntity target,
            float damage,
            Vec3 spawnPosition,
            AbstractSpell summonSpell
    ) {
        this(
                ROACWEntityRegistry.HOMING_PLAGUE_MISSILE.get(),
                level
        );

        this.setOwner(shooter);
        this.target = target;
        this.summonSpell = summonSpell;
        this.setPos(spawnPosition.x, spawnPosition.y, spawnPosition.z);
        this.setMissileDamage(damage * HOMING_DAMAGE_MULTIPLIER);

        Vec3 direction = target.position()
                .add(0.0D, target.getBbHeight() * 0.5D, 0.0D)
                .subtract(spawnPosition);

        if (direction.lengthSqr() < 1.0E-8D) {
            direction = new Vec3(0.0D, 0.0D, 1.0D);
        }

        direction = direction.normalize();
        this.setDeltaMovement(direction.scale(SPEED));

        this.setYRot(
                (float) (
                        Mth.atan2(direction.z, direction.x)
                                * (180F / Math.PI)
                ) - 90.0F
        );

        this.setXRot(
                (float) -(
                        Mth.atan2(
                                direction.y,
                                Math.sqrt(
                                        direction.x * direction.x
                                                + direction.z * direction.z
                                )
                        ) * (180F / Math.PI)
                )
        );

        this.yRotO = this.getYRot();
        this.xRotO = this.getXRot();
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(DATA_MISSILE_DAMAGE, DEFAULT_DAMAGE);
    }

    private void setMissileDamage(float damage) {
        this.entityData.set(DATA_MISSILE_DAMAGE, Math.max(0.0F, damage));
    }

    public float getMissileDamage() {
        return this.entityData.get(DATA_MISSILE_DAMAGE);
    }

    @Nullable
    public AbstractSpell getSummonSpell() {
        return this.summonSpell;
    }

    public void setSummonSpell(@Nullable AbstractSpell summonSpell) {
        this.summonSpell = summonSpell;
    }

    @Override
    public void tick() {
        this.clearFire();
        super.baseTick();

        if (this.level().isClientSide) {
            Vec3 clientMovement = this.getDeltaMovement();

            if (clientMovement.lengthSqr() > 1.0E-8D) {
                this.move(MoverType.SELF, clientMovement);
                this.updateRotationFromVelocity();
            }

            return;
        }

        this.life++;

        if (this.life > MAX_LIFETIME) {
            this.discard();
            return;
        }

        if (!this.isValidTarget(this.target)) {
            this.target = null;
        }

        if (this.target == null) {
            this.ticksSinceReacquire++;

            if (this.life >= NO_TARGET_MAX_LIFE_TICKS) {
                this.discard();
                return;
            }

            if (this.ticksSinceReacquire >= REACQUIRE_INTERVAL) {
                this.ticksSinceReacquire = 0;
                this.tryAcquireTarget();
            }
        } else {
            this.ticksSinceReacquire = 0;
        }

        if (this.target != null) {
            this.homeTowards(this.target);
        } else {
            Vec3 movement = this.getDeltaMovement();

            if (movement.lengthSqr() < 1.0E-8D) {
                movement = new Vec3(0.0D, 0.0D, SPEED);
            }

            this.setDeltaMovement(movement.normalize().scale(SPEED));
        }

        Vec3 movement = this.getDeltaMovement();
        Vec3 end = this.position().add(movement);

        HitResult hitResult = ProjectileUtil.getHitResultOnMoveVector(
                this,
                this::canHitEntity
        );

        if (hitResult.getType() != HitResult.Type.MISS) {
            end = hitResult.getLocation();
        }

        this.setPos(end.x, end.y, end.z);
        this.updateRotationFromVelocity();

        movement = this.getDeltaMovement();

        if (movement.lengthSqr() > 1.0E-8D) {
            this.setDeltaMovement(movement.normalize().scale(SPEED));
        }

        double contactReach = (this.getBbWidth() * 0.5D) + HIT_TOLERANCE;

        if (this.target != null
                && this.isValidTarget(this.target)
                && this.target.getBoundingBox()
                .inflate(contactReach)
                .contains(this.position())) {
            this.hitTarget(this.target);
            return;
        }

        if (hitResult.getType() != HitResult.Type.MISS) {
            this.onHit(hitResult);
            return;
        }

        this.spawnTrailParticles();
    }

    private void homeTowards(LivingEntity target) {
        Vec3 currentVelocity = this.getDeltaMovement();

        if (currentVelocity.lengthSqr() < 1.0E-8D) {
            currentVelocity = new Vec3(0.0D, 0.0D, SPEED);
        }

        Vec3 currentDirection = currentVelocity.normalize();

        Vec3 targetPosition = target.position()
                .add(0.0D, target.getBbHeight() * 0.5D, 0.0D);

        Vec3 toTarget = targetPosition.subtract(this.position());

        if (toTarget.lengthSqr() < 1.0E-8D) {
            return;
        }

        Vec3 desiredDirection = toTarget.normalize();

        double dot = Mth.clamp(
                currentDirection.dot(desiredDirection),
                -1.0D,
                1.0D
        );

        double angleRadians = Math.acos(dot);
        double maxTurnRadians = Math.toRadians(MAX_TURN_DEGREES_PER_TICK);

        Vec3 newDirection;

        if (angleRadians <= maxTurnRadians) {
            newDirection = desiredDirection;
        } else {
            Vec3 axis = currentDirection.cross(desiredDirection);

            if (axis.lengthSqr() < 1.0E-8D) {
                if (currentDirection.dot(desiredDirection) > 0.0D) {
                    newDirection = currentDirection;
                } else {
                    axis = currentDirection.cross(new Vec3(0.0D, 1.0D, 0.0D));

                    if (axis.lengthSqr() < 1.0E-8D) {
                        axis = currentDirection.cross(new Vec3(1.0D, 0.0D, 0.0D));
                    }

                    axis = axis.normalize();

                    double cos = Math.cos(maxTurnRadians);
                    double sin = Math.sin(maxTurnRadians);

                    newDirection = currentDirection.scale(cos)
                            .add(axis.cross(currentDirection).scale(sin))
                            .add(
                                    axis.scale(
                                            axis.dot(currentDirection)
                                                    * (1.0D - cos)
                                    )
                            )
                            .normalize();
                }
            } else {
                axis = axis.normalize();

                double cos = Math.cos(maxTurnRadians);
                double sin = Math.sin(maxTurnRadians);

                newDirection = currentDirection.scale(cos)
                        .add(axis.cross(currentDirection).scale(sin))
                        .add(
                                axis.scale(
                                        axis.dot(currentDirection)
                                                * (1.0D - cos)
                                )
                        )
                        .normalize();
            }
        }

        Vec3 horizontalVelocity = new Vec3(
                currentDirection.x,
                0.0D,
                currentDirection.z
        );

        Vec3 horizontalDesired = new Vec3(
                desiredDirection.x,
                0.0D,
                desiredDirection.z
        );

        float targetBank = 0.0F;

        if (horizontalVelocity.lengthSqr() > 1.0E-8D
                && horizontalDesired.lengthSqr() > 1.0E-8D) {
            horizontalVelocity = horizontalVelocity.normalize();
            horizontalDesired = horizontalDesired.normalize();

            double crossY = horizontalVelocity.x * horizontalDesired.z
                    - horizontalVelocity.z * horizontalDesired.x;

            double horizontalDot = Mth.clamp(
                    horizontalVelocity.dot(horizontalDesired),
                    -1.0D,
                    1.0D
            );

            double horizontalAngle = Math.toDegrees(Math.acos(horizontalDot));

            targetBank = (float) (
                    Math.signum(crossY)
                            * Math.min(horizontalAngle, MAX_BANK_ANGLE)
            );
        }

        this.bankAngle = Mth.lerp(
                BANK_SMOOTHING,
                this.bankAngle,
                targetBank
        );

        this.setDeltaMovement(newDirection.scale(SPEED));
    }

    private void updateRotationFromVelocity() {
        Vec3 velocity = this.getDeltaMovement();

        if (velocity.lengthSqr() < 1.0E-8D) {
            return;
        }

        velocity = velocity.normalize();

        float yaw = (float) (
                Mth.atan2(velocity.z, velocity.x) * (180F / Math.PI)
        ) - 90.0F;

        float pitch = (float) -(
                Mth.atan2(
                        velocity.y,
                        Math.sqrt(
                                velocity.x * velocity.x
                                        + velocity.z * velocity.z
                        )
                ) * (180F / Math.PI)
        );

        this.setYRot(yaw);
        this.setXRot(pitch);
    }

    private void tryAcquireTarget() {
        double radius = Math.min(
                REACQUIRE_MAX_RADIUS,
                REACQUIRE_BASE_RADIUS
                        + (this.life / 20.0D) * REACQUIRE_GROWTH_PER_SECOND
        );

        AABB searchBox = this.getBoundingBox().inflate(radius);

        List<LivingEntity> candidates = this.level().getEntitiesOfClass(
                LivingEntity.class,
                searchBox,
                this::isValidTarget
        );

        if (candidates.isEmpty()) {
            return;
        }

        candidates.sort(Comparator.comparingDouble(this::distanceToSqr));
        this.target = candidates.get(0);
    }

    private boolean isValidTarget(@Nullable LivingEntity candidate) {
        if (candidate == null || !candidate.isAlive() || candidate.isSpectator()) {
            return false;
        }

        Entity projectileOwner = this.getOwner();

        if (candidate == projectileOwner) {
            return false;
        }

        LivingEntity summonOwner = this.getSummonOwner();

        if (candidate == summonOwner) {
            return false;
        }

        if (projectileOwner != null && areSameOrAllied(projectileOwner, candidate)) {
            return false;
        }

        if (summonOwner != null && areSameOrAllied(summonOwner, candidate)) {
            return false;
        }

        if (candidate.isAlliedTo(this) || this.isAlliedTo(candidate)) {
            return false;
        }

        Entity candidateOwner = this.getResolvedSummonOwner(candidate);

        if (candidateOwner != null) {
            if (candidateOwner == projectileOwner || candidateOwner == summonOwner) {
                return false;
            }

            if (summonOwner != null && areSameOrAllied(summonOwner, candidateOwner)) {
                return false;
            }

            if (projectileOwner != null && areSameOrAllied(projectileOwner, candidateOwner)) {
                return false;
            }
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

    @Override
    protected boolean canHitEntity(Entity entity) {
        if (!super.canHitEntity(entity)) {
            return false;
        }

        if (!(entity instanceof LivingEntity livingEntity)) {
            return false;
        }

        return this.isValidTarget(livingEntity);
    }

    @Override
    protected void onHit(HitResult result) {
        if (result.getType() == HitResult.Type.ENTITY
                && result instanceof EntityHitResult entityHitResult
                && entityHitResult.getEntity() instanceof LivingEntity livingEntity
                && this.isValidTarget(livingEntity)) {
            this.hitTarget(livingEntity);
            return;
        }

        this.discard();
    }

    private void hitTarget(LivingEntity target) {
        if (!this.isValidTarget(target)) {
            this.target = null;
            return;
        }

        LivingEntity summoner = this.getSummonOwner();

        if (summoner == null) {
            this.discard();
            return;
        }

        AbstractSpell spell = this.getSummonSpell();

        if (spell == null) {
            this.discard();
            return;
        }

        float damage = this.getMissileDamage();
        var source = spell.getDamageSource(this, summoner);

        target.invulnerableTime = 0;

        boolean damaged = DamageSources.applyDamage(target, damage, source);

        if (damaged) {
            int amplifier = Utils.getNatureScaledPlagueAmplifier(summoner);

            target.addEffect(
                    new MobEffectInstance(
                            ROACWEffectRegistry.PLAGUE.get(),
                            PLAGUE_DURATION,
                            amplifier,
                            false,
                            true,
                            true
                    ),
                    summoner
            );
        }

        this.level().playSound(
                null,
                this.getX(),
                this.getY(),
                this.getZ(),
                ROACWSoundRegistry.HOMING_PLAGUE_MISSILE_EXPLOSION.get(),
                SoundSource.HOSTILE,
                1.0F,
                1.0F
        );

        this.discard();
    }

    private void spawnTrailParticles() {
        if (!(this.level() instanceof ServerLevel serverLevel)) {
            return;
        }

        Vec3 velocity = this.getDeltaMovement();

        if (velocity.lengthSqr() < 1.0E-8D) {
            return;
        }

        Vec3 forward = velocity.normalize();
        Vec3 right = forward.cross(new Vec3(0.0D, 1.0D, 0.0D));

        if (right.lengthSqr() < 1.0E-8D) {
            right = new Vec3(1.0D, 0.0D, 0.0D);
        } else {
            right = right.normalize();
        }

        double smokeBackDistance = Mth.lerp(
                this.random.nextDouble(),
                SMOKE_MIN_BACK_DISTANCE,
                SMOKE_MAX_BACK_DISTANCE
        );

        double side = (this.random.nextDouble() - 0.5D) * SMOKE_SPREAD;

        Vec3 smokePosition = this.position()
                .subtract(forward.scale(smokeBackDistance))
                .add(right.scale(side))
                .add(0.0D, 0.15D, 0.0D);

        serverLevel.sendParticles(
                ParticleTypes.SMOKE,
                smokePosition.x,
                smokePosition.y,
                smokePosition.z,
                1,
                0.0D,
                0.0D,
                0.0D,
                0.0D
        );
    }

    @Override
    protected float getInertia() {
        return 1.0F;
    }

    public double getGravity() {
        return 0.0D;
    }

    @Override
    protected boolean shouldBurn() {
        return false;
    }

    @Override
    public boolean isPickable() {
        return false;
    }

    @Override
    public boolean ignoreExplosion() {
        return true;
    }

    @Nullable
    public LivingEntity getSummonOwner() {
        Entity projectileOwner = this.getOwner();

        if (projectileOwner == null) {
            return null;
        }

        if (projectileOwner instanceof ViriliEntity virili) {
            LivingEntity viriliOwner = virili.getOwner();

            if (viriliOwner != null) {
                return viriliOwner;
            }
        }

        Entity owner = SummonManager.getOwner(projectileOwner);

        if (owner instanceof LivingEntity livingEntity) {
            return livingEntity;
        }

        return null;
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putFloat("MissileDamage", this.getMissileDamage());
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);

        if (tag.contains("MissileDamage")) {
            this.setMissileDamage(tag.getFloat("MissileDamage"));
        }
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
        this.setPos(x, y, z);
        this.setYRot(yRot);
        this.setXRot(xRot);
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.cache;
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
    }
}