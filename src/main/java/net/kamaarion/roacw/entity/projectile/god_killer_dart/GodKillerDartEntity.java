package net.kamaarion.roacw.entity.projectile.god_killer_dart;

import io.redspace.ironsspellbooks.entity.spells.AbstractMagicProjectile;
import net.kamaarion.roacw.registeries.ROACWEffectRegistry;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ambient.AmbientCreature;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.animal.WaterAnimal;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.player.Player;
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
import java.util.Optional;
import java.util.function.Supplier;

public class GodKillerDartEntity extends AbstractMagicProjectile implements GeoEntity {

    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    private static final EntityDataAccessor<Integer> TARGET_ID =
            SynchedEntityData.defineId(
                    GodKillerDartEntity.class,
                    EntityDataSerializers.INT
            );

    private static final float MAX_TURN_DEGREES_PER_TICK = 14.0F;
    private static final int MAX_LIFE_TICKS = 200;
    private static final float DAMAGE = 8.0F;
    private static final int HOMING_DELAY_TICKS = 6;
    private static final double HIT_RADIUS = 1.2D;
    private static final int INFERNO_DURATION_TICKS = 100;
    private static final int INFERNO_AMPLIFIER = 0;
    private static final double OVERSHOOT_ANGLE_DEGREES = 100.0D;
    private static final double OVERSHOOT_CHECK_RANGE = 4.0D;
    private static final int NO_TARGET_MAX_LIFE_TICKS = 100;
    private static final double RETARGET_SEARCH_RADIUS_BASE = 10.0D;
    private static final double RETARGET_SEARCH_RADIUS_GROWTH_PER_SECOND = 6.0D;
    private static final double RETARGET_SEARCH_RADIUS_MAX = 40.0D;
    private static final int RETARGET_INTERVAL_TICKS = 5;
    private static final float MAX_BANK_ANGLE = 40.0F;
    private static final float BANK_TURN_SCALE = 2.5F;
    private static final float BANK_SMOOTHING = 0.25F;

    @Nullable
    private LivingEntity target;

    private int life;
    private float bankAngle;

    public GodKillerDartEntity(
            EntityType<? extends AbstractMagicProjectile> type,
            Level level
    ) {
        super(type, level);
        this.setNoGravity(true);
    }

    public GodKillerDartEntity(
            EntityType<? extends AbstractMagicProjectile> type,
            LivingEntity shooter,
            Level level
    ) {
        this(type, level);

        this.setOwner(shooter);
        this.setPos(
                shooter.getX(),
                shooter.getEyeY() - 0.2D,
                shooter.getZ()
        );
        this.setNoGravity(true);
    }

    public void setTarget(@Nullable LivingEntity target) {
        this.target = target;
        this.entityData.set(TARGET_ID, target == null ? -1 : target.getId());
    }

    @Nullable
    public LivingEntity getTarget() {
        if (this.target == null) {
            int id = this.entityData.get(TARGET_ID);

            if (id != -1) {
                Entity entity = this.level().getEntity(id);

                if (entity instanceof LivingEntity livingEntity) {
                    this.target = livingEntity;
                }
            }
        }

        return this.target;
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(TARGET_ID, -1);
    }

    @Override
    public void tick() {
        super.tick();

        if (!this.level().isClientSide) {
            life++;

            if (life > MAX_LIFE_TICKS) {
                this.discard();
                return;
            }

            LivingEntity currentTarget = getTarget();

            if (currentTarget != null && !currentTarget.isAlive()) {
                setTarget(null);
                currentTarget = null;
            }

            if (currentTarget == null && life % RETARGET_INTERVAL_TICKS == 0) {
                currentTarget = tryAcquireTarget();
            }

            if (currentTarget != null && life > HOMING_DELAY_TICKS) {
                homeTowards(currentTarget);
                checkProximityHit(currentTarget);
            } else {
                bankAngle = Mth.lerp(BANK_SMOOTHING, bankAngle, 0.0F);

                if (currentTarget == null && life > NO_TARGET_MAX_LIFE_TICKS) {
                    this.discard();
                    return;
                }
            }
        }
    }

    @Nullable
    private LivingEntity tryAcquireTarget() {
        double elapsedSeconds = this.life / 20.0D;

        double searchRadius = Math.min(
                RETARGET_SEARCH_RADIUS_MAX,
                RETARGET_SEARCH_RADIUS_BASE
                        + elapsedSeconds * RETARGET_SEARCH_RADIUS_GROWTH_PER_SECOND
        );

        AABB searchArea = new AABB(
                this.position(),
                this.position()
        ).inflate(searchRadius);

        var candidates = this.level().getEntitiesOfClass(
                LivingEntity.class,
                searchArea,
                entity -> entity.isAlive()
                        && entity != this.getOwner()
                        && (this.getOwner() == null || !this.getOwner().isAlliedTo(entity))
                        && isViableHostileTarget(entity)
        );

        LivingEntity nearest = null;
        double nearestDistSq = Double.MAX_VALUE;

        for (LivingEntity candidate : candidates) {
            double distSq = candidate.distanceToSqr(this);

            if (distSq < nearestDistSq) {
                nearestDistSq = distSq;
                nearest = candidate;
            }
        }

        if (nearest != null) {
            setTarget(nearest);
        }

        return nearest;
    }

    private static boolean isViableHostileTarget(LivingEntity entity) {
        return !(entity instanceof Animal)
                && !(entity instanceof WaterAnimal)
                && !(entity instanceof AmbientCreature)
                && !(entity instanceof Villager)
                && !(entity instanceof Player);
    }

    private void checkProximityHit(LivingEntity target) {
        if (this.distanceToSqr(target) <= HIT_RADIUS * HIT_RADIUS) {
            dealHitAndDiscard(target);
        }
    }

    private void homeTowards(LivingEntity target) {
        Vec3 toTarget = target.getBoundingBox()
                .getCenter()
                .subtract(this.position())
                .normalize();

        Vec3 currentMotion = this.getDeltaMovement();
        double speed = currentMotion.length();

        if (speed < 1.0E-5D) {
            speed = 0.5D;
        }

        Vec3 normalizedCurrent = currentMotion.normalize();

        double angleToTargetDegrees = Math.toDegrees(
                Math.acos(
                        Mth.clamp(
                                normalizedCurrent.dot(toTarget),
                                -1.0D,
                                1.0D
                        )
                )
        );

        boolean nearTarget = this.distanceToSqr(target)
                <= OVERSHOOT_CHECK_RANGE * OVERSHOOT_CHECK_RANGE;

        if (nearTarget && angleToTargetDegrees > OVERSHOOT_ANGLE_DEGREES) {
            fizzle();
            return;
        }

        Vec3 newDirection = rotateTowards(
                normalizedCurrent,
                toTarget,
                Math.toRadians(MAX_TURN_DEGREES_PER_TICK)
        );

        updateBankAngle(normalizedCurrent, newDirection);

        this.setDeltaMovement(newDirection.scale(speed));

        this.setYRot((float) (
                Mth.atan2(newDirection.x, newDirection.z) * (180.0D / Math.PI)
        ));

        this.setXRot((float) (
                Mth.atan2(newDirection.y, newDirection.horizontalDistance())
                        * (180.0D / Math.PI)
        ));

        this.yRotO = this.getYRot();
        this.xRotO = this.getXRot();
    }

    private void fizzle() {
        if (this.level() instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(
                    ParticleTypes.SMOKE,
                    this.getX(),
                    this.getY(),
                    this.getZ(),
                    8,
                    0.1D,
                    0.1D,
                    0.1D,
                    0.02D
            );
        }

        this.level().playSound(
                null,
                this.getX(),
                this.getY(),
                this.getZ(),
                SoundEvents.ITEM_BREAK,
                SoundSource.NEUTRAL,
                0.5F,
                1.4F
        );

        this.discard();
    }

    private static Vec3 rotateTowards(
            Vec3 from,
            Vec3 to,
            double maxAngleRadians
    ) {
        double dot = Mth.clamp(from.dot(to), -1.0D, 1.0D);
        double angle = Math.acos(dot);

        if (angle <= maxAngleRadians || angle < 1.0E-6D) {
            return to;
        }

        Vec3 axis = from.cross(to);

        if (axis.lengthSqr() < 1.0E-9D) {
            Vec3 fallback = Math.abs(from.x) < 0.9D
                    ? new Vec3(1, 0, 0)
                    : new Vec3(0, 1, 0);

            axis = fallback.cross(from);
        }

        axis = axis.normalize();

        double cos = Math.cos(maxAngleRadians);
        double sin = Math.sin(maxAngleRadians);

        Vec3 term1 = from.scale(cos);
        Vec3 term2 = axis.cross(from).scale(sin);
        Vec3 term3 = axis.scale(axis.dot(from) * (1.0D - cos));

        return term1.add(term2).add(term3).normalize();
    }

    private void updateBankAngle(Vec3 oldDirection, Vec3 newDirection) {
        double cross = oldDirection.x * newDirection.z
                - oldDirection.z * newDirection.x;

        double dot = Mth.clamp(
                oldDirection.x * newDirection.x
                        + oldDirection.z * newDirection.z,
                -1.0D,
                1.0D
        );

        double turnDegrees = Math.toDegrees(Math.acos(dot));

        float sign = cross > 0.0D ? -1.0F : 1.0F;

        float targetBank = Mth.clamp(
                (float) (sign * turnDegrees * BANK_TURN_SCALE),
                -MAX_BANK_ANGLE,
                MAX_BANK_ANGLE
        );

        bankAngle = Mth.lerp(BANK_SMOOTHING, bankAngle, targetBank);
    }

    public float getBankAngle() {
        return bankAngle;
    }

    @Override
    protected void onHitEntity(EntityHitResult result) {
        super.onHitEntity(result);

        if (!this.level().isClientSide && result.getEntity() instanceof LivingEntity hit) {
            dealHitAndDiscard(hit);
        }
    }

    private void dealHitAndDiscard(LivingEntity hit) {
        if (this.level().isClientSide || this.isRemoved()) {
            return;
        }

        hit.invulnerableTime = 0;

        hit.hurt(
                this.damageSources().mobProjectile(
                        this,
                        getOwner() instanceof LivingEntity livingOwner ? livingOwner : null
                ),
                DAMAGE
        );

        hit.addEffect(new MobEffectInstance(
                ROACWEffectRegistry.GOD_SLAYER_INFERNO.get(),
                INFERNO_DURATION_TICKS,
                INFERNO_AMPLIFIER,
                false,
                false
        ));

        this.discard();
    }

    @Override
    protected void onHit(HitResult result) {
        if (result instanceof EntityHitResult entityHitResult) {
            this.onHitEntity(entityHitResult);
        }
    }

    @Override
    public void trailParticles() {
        Vec3 center = this.getBoundingBox().getCenter();

        this.level().addParticle(
                ParticleTypes.SMOKE,
                center.x,
                center.y,
                center.z,
                0.0D,
                0.0D,
                0.0D
        );
    }

    @Override
    public void impactParticles(double x, double y, double z) {
    }

    @Override
    public float getSpeed() {
        return 1.0F;
    }

    @Override
    public Optional<Supplier<SoundEvent>> getImpactSound() {
        return Optional.empty();
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.cache;
    }
}