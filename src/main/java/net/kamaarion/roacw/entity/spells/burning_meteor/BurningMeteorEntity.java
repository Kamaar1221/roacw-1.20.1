package net.kamaarion.roacw.entity.spells.burning_meteor;

import io.redspace.ironsspellbooks.entity.spells.AbstractMagicProjectile;
import io.redspace.ironsspellbooks.registries.ParticleRegistry;
import net.kamaarion.roacw.effects.DragonfireEffect;
import net.kamaarion.roacw.registeries.ROACWEntityRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.ForgeEventFactory;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.util.GeckoLibUtil;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

public class BurningMeteorEntity extends AbstractMagicProjectile implements GeoEntity {
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    private static final double INITIAL_SPEED = 0.35D;
    private static final double ACCELERATION = 0.055D;
    private static final double MAX_SPEED = 1.8D;

    private static final float DEFAULT_DAMAGE = 10.0F;
    private static final float DEFAULT_AOE_DAMAGE = 5.0F;
    private static final float DEFAULT_AOE_RADIUS = 2.5F;

    private static final float DRAGONFIRE_CHANCE = 0.25F;
    private static final int DRAGONFIRE_COOLDOWN_TICKS = 6;
    private static final String DRAGONFIRE_COOLDOWN_TAG = "ROACW_DragonfireMeteorCooldown";

    private static final double TRAIL_OFFSET = 0.35D;

    private float aoeDamage = DEFAULT_AOE_DAMAGE;
    private float aoeRadius = DEFAULT_AOE_RADIUS;

    private boolean hasHit;

    @Nullable
    private Vec3 targetPosition;

    public BurningMeteorEntity(EntityType<? extends AbstractMagicProjectile> type, Level level) {
        super(type, level);
        this.setNoGravity(true);
        this.setDamage(DEFAULT_DAMAGE);
    }

    public BurningMeteorEntity(
            Level level,
            LivingEntity owner,
            Vec3 spawnPosition,
            Vec3 targetPosition
    ) {
        super(ROACWEntityRegistry.BURNING_METEOR.get(), level);

        this.setOwner(owner);
        this.targetPosition = targetPosition;

        this.setNoGravity(true);
        this.setDamage(DEFAULT_DAMAGE);

        Vec3 direction = targetPosition.subtract(spawnPosition).normalize();
        this.setDeltaMovement(direction.scale(INITIAL_SPEED));
        this.setPos(spawnPosition.x, spawnPosition.y, spawnPosition.z);
    }

    @Override
    public void setDamage(float damage) {
        super.setDamage(damage);
    }

    @Override
    public float getDamage() {
        return super.getDamage();
    }

    public void setAoeDamage(float aoeDamage) {
        this.aoeDamage = aoeDamage;
    }

    public float getAoeDamage() {
        return aoeDamage;
    }

    public void setAoeRadius(float aoeRadius) {
        this.aoeRadius = aoeRadius;
    }

    public float getAoeRadius() {
        return aoeRadius;
    }

    public void setTargetPosition(Vec3 targetPosition) {
        this.targetPosition = targetPosition;
    }

    @Nullable
    public Vec3 getTargetPosition() {
        return targetPosition;
    }

    @Override
    public void tick() {
        Entity owner = this.getOwner();

        if (this.level().isClientSide
                || ((owner == null || !owner.isRemoved())
                && this.level().hasChunkAt(this.blockPosition()))) {

            this.baseTick();
            this.clearFire();

            HitResult hitResult = ProjectileUtil.getHitResultOnMoveVector(
                    this,
                    this::canHitEntity
            );

            if (hitResult.getType() != HitResult.Type.MISS
                    && !ForgeEventFactory.onProjectileImpact(this, hitResult)) {
                this.onHit(hitResult);
            }

            if (!this.level().isClientSide
                    && this.targetPosition != null
                    && this.distanceToTarget() <= 0.9D) {

                this.onHit(new BlockHitResult(
                        this.targetPosition,
                        Direction.UP,
                        BlockPos.containing(this.targetPosition),
                        false
                ));

                return;
            }

            if (this.targetPosition != null) {
                Vec3 offset = this.targetPosition.subtract(this.position());

                if (offset.lengthSqr() > 1.0E-8D) {
                    Vec3 direction = offset.normalize();
                    Vec3 currentMotion = this.getDeltaMovement();

                    double speed = Math.max(
                            INITIAL_SPEED,
                            currentMotion.length() + ACCELERATION
                    );
                    speed = Math.min(MAX_SPEED, speed);

                    Vec3 desiredMotion = direction.scale(speed);
                    Vec3 newMotion = currentMotion.scale(0.75D)
                            .add(desiredMotion.scale(0.25D));

                    this.setDeltaMovement(newMotion);
                }
            }

            Vec3 motion = this.getDeltaMovement();

            this.setPos(
                    this.getX() + motion.x,
                    this.getY() + motion.y,
                    this.getZ() + motion.z
            );

            ProjectileUtil.rotateTowardsMovement(this, 0.2F);

            if (this.level().isClientSide) {
                this.trailParticles();
            }
        } else {
            this.discard();
        }
    }

    private double distanceToTarget() {
        if (this.targetPosition == null) {
            return Double.MAX_VALUE;
        }

        return this.position().distanceTo(this.targetPosition);
    }

    @Override
    public void trailParticles() {
        Vec3 motion = this.getDeltaMovement();
        Vec3 position = this.position();

        if (motion.lengthSqr() > 1.0E-6D) {
            Vec3 trailOffset = motion.normalize().scale(-TRAIL_OFFSET);

            spawnTrailParticles(
                    position.x + trailOffset.x,
                    position.y + trailOffset.y,
                    position.z + trailOffset.z
            );
        } else {
            spawnTrailParticles(position.x, position.y, position.z);
        }
    }

    private void spawnTrailParticles(double x, double y, double z) {
        this.level().addParticle(
                ParticleRegistry.FIERY_SMOKE_PARTICLE.get(),
                x, y, z,
                0.0D, 0.0D, 0.0D
        );

        this.level().addParticle(
                ParticleTypes.FLAME,
                x, y, z,
                0.0D, 0.0D, 0.0D
        );

        if (this.tickCount % 2 == 0) {
            this.level().addParticle(
                    ParticleTypes.SMOKE,
                    x, y, z,
                    0.0D, 0.0D, 0.0D
            );
        }

        if (this.tickCount % 5 == 0) {
            this.level().addParticle(
                    ParticleTypes.LARGE_SMOKE,
                    x, y, z,
                    0.0D, 0.0D, 0.0D
            );
        }

        if (this.tickCount % 2 == 0) {
            double offsetX = (this.random.nextDouble() - 0.5D) * 0.3D;
            double offsetY = (this.random.nextDouble() - 0.5D) * 0.3D;
            double offsetZ = (this.random.nextDouble() - 0.5D) * 0.3D;

            this.level().addParticle(
                    ParticleTypes.FLAME,
                    x + offsetX,
                    y + offsetY,
                    z + offsetZ,
                    0.0D, 0.0D, 0.0D
            );
        }
    }

    @Override
    protected void onHit(HitResult result) {
        super.onHit(result);

        if (this.hasHit || this.level().isClientSide) {
            return;
        }

        this.hasHit = true;

        Entity directHit = result instanceof EntityHitResult entityHit
                ? entityHit.getEntity()
                : null;

        if (directHit instanceof LivingEntity living && shouldAffect(living)) {
            living.hurt(
                    this.damageSources().indirectMagic(this, this.getOwner()),
                    this.getDamage()
            );

            living.setSecondsOnFire(5);
            tryApplyDragonfire(living);
        }

        applyAreaDamage(directHit);
        this.discard();
    }

    @Override
    public void impactParticles(double x, double y, double z) {
        if (this.level() instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(
                    ParticleRegistry.FIERY_SMOKE_PARTICLE.get(),
                    x, y + 0.25D, z,
                    40, 1.0D, 0.5D, 1.0D, 0.08D
            );

            serverLevel.sendParticles(
                    ParticleTypes.LARGE_SMOKE,
                    x, y + 0.25D, z,
                    15, 0.7D, 0.3D, 0.7D, 0.04D
            );

            serverLevel.sendParticles(
                    ParticleTypes.EXPLOSION,
                    x, y, z,
                    1, 0.0D, 0.0D, 0.0D, 0.0D
            );

            serverLevel.sendParticles(
                    ParticleTypes.FLAME,
                    x, y, z,
                    25, 0.5D, 1.0D, 0.5D, 0.05D
            );
        }
    }

    @Override
    public Optional<Supplier<SoundEvent>> getImpactSound() {
        return Optional.empty();
    }

    @Override
    public float getSpeed() {
        return (float) INITIAL_SPEED;
    }

    private void tryApplyDragonfire(LivingEntity target) {
        if (!shouldAffect(target)) {
            return;
        }

        long currentGameTime = target.level().getGameTime();
        long cooldownEnd = target.getPersistentData().getLong(DRAGONFIRE_COOLDOWN_TAG);

        if (currentGameTime < cooldownEnd) {
            return;
        }

        if (this.random.nextFloat() >= DRAGONFIRE_CHANCE) {
            return;
        }

        DragonfireEffect.addStack(target);

        target.getPersistentData().putLong(
                DRAGONFIRE_COOLDOWN_TAG,
                currentGameTime + DRAGONFIRE_COOLDOWN_TICKS
        );
    }

    private void applyAreaDamage(@Nullable Entity directHit) {
        AABB area = this.getBoundingBox().inflate(this.aoeRadius);

        List<LivingEntity> entities = this.level().getEntitiesOfClass(
                LivingEntity.class,
                area
        );

        for (LivingEntity entity : entities) {
            if (entity == directHit || !shouldAffect(entity)) {
                continue;
            }

            double distance = this.distanceTo(entity);

            if (distance > this.aoeRadius) {
                continue;
            }

            float multiplier = (float) (1.0D - distance / this.aoeRadius);
            float finalDamage = this.aoeDamage * multiplier;

            if (finalDamage <= 0.0F) {
                continue;
            }

            entity.hurt(
                    this.damageSources().indirectMagic(this, this.getOwner()),
                    finalDamage
            );

            entity.setSecondsOnFire(5);
            tryApplyDragonfire(entity);
        }
    }

    private boolean shouldAffect(LivingEntity entity) {
        if (entity == this.getOwner()) {
            return false;
        }

        LivingEntity owner = this.getOwner() instanceof LivingEntity livingOwner
                ? livingOwner
                : null;

        return owner == null || !owner.isAlliedTo(entity);
    }

    @Override
    public boolean isPickable() {
        return false;
    }

    @Override
    public boolean ignoreExplosion() {
        return true;
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return cache;
    }
}