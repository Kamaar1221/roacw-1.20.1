package net.kamaarion.roacw.entity.spells.burning_meteor;

import io.redspace.ironsspellbooks.registries.ParticleRegistry;
import net.kamaarion.roacw.registeries.ROACWEntityRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.AbstractHurtingProjectile;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.*;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.util.GeckoLibUtil;

import javax.annotation.Nullable;
import java.util.List;

public class BurningMeteorEntity
        extends AbstractHurtingProjectile
        implements GeoEntity {

    private final AnimatableInstanceCache cache =
            GeckoLibUtil.createInstanceCache(this);

    /*
     * ============================================================
     * MOVEMENT
     * ============================================================
     */

    private static final double INITIAL_SPEED = 0.35D;
    private static final double ACCELERATION = 0.055D;
    private static final double MAX_SPEED = 1.8D;

    /*
     * ============================================================
     * DAMAGE
     * ============================================================
     */

    private static final float DEFAULT_DAMAGE = 10.0F;
    private static final float DEFAULT_AOE_DAMAGE = 5.0F;
    private static final float DEFAULT_AOE_RADIUS = 2.5F;

    private float damage = DEFAULT_DAMAGE;
    private float aoeDamage = DEFAULT_AOE_DAMAGE;
    private float aoeRadius = DEFAULT_AOE_RADIUS;

    private boolean hasHit = false;

    /*
     * ============================================================
     * TARGET
     * ============================================================
     *
     * This is the point the meteor is trying to reach.
     */

    @Nullable
    private Vec3 targetPosition;

    /*
     * ============================================================
     * TRAIL
     * ============================================================
     *
     * How far behind the meteor the particles are emitted.
     *
     * Because this is calculated from the meteor's movement
     * direction, the trail automatically follows the meteor's
     * trajectory.
     */
    private static final double TRAIL_OFFSET = 0.35D;

    /*
     * ============================================================
     * CONSTRUCTORS
     * ============================================================
     */

    /**
     * Constructor used by Minecraft/the entity registry.
     */
    public BurningMeteorEntity(
            EntityType<? extends AbstractHurtingProjectile> type,
            Level level
    ) {
        super(type, level);
    }

    /**
     * Constructor used by BurningMeteorSpell.
     */
    public BurningMeteorEntity(
            Level level,
            LivingEntity owner,
            Vec3 spawnPosition,
            Vec3 targetPosition
    ) {
        super(
                ROACWEntityRegistry.BURNING_METEOR.get(),
                spawnPosition.x,
                spawnPosition.y,
                spawnPosition.z,
                0.0D,
                0.0D,
                0.0D,
                level
        );

        this.setOwner(owner);
        this.targetPosition = targetPosition;

        /*
         * Start the meteor moving toward its target.
         */
        Vec3 direction =
                targetPosition
                        .subtract(spawnPosition)
                        .normalize();

        this.setDeltaMovement(
                direction.scale(INITIAL_SPEED)
        );
    }

    /*
     * ============================================================
     * DAMAGE SETTERS / GETTERS
     * ============================================================
     */

    public void setDamage(float damage) {
        this.damage = damage;
    }

    public float getDamage() {
        return damage;
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

    /*
     * ============================================================
     * TICK
     * ============================================================
     */

    @Override
    public void tick() {
        Entity owner = this.getOwner();

        if (this.level().isClientSide
                || (owner == null || !owner.isRemoved())
                && this.level().hasChunkAt(this.blockPosition())) {

            this.baseTick();

            /*
             * The meteor itself should not behave like a normal
             * burning Minecraft projectile.
             */
            this.clearFire();

            /*
             * Check for entity/block collision along the current
             * movement vector.
             */
            HitResult hitResult =
                    ProjectileUtil.getHitResultOnMoveVector(
                            this,
                            this::canHitEntity
                    );

            if (hitResult.getType() != HitResult.Type.MISS
                    && !net.minecraftforge.event.ForgeEventFactory
                    .onProjectileImpact(this, hitResult)) {

                this.onHit(hitResult);
            }

            /*
             * If the meteor reaches its intended target,
             * trigger the impact.
             */
            if (!this.level().isClientSide
                    && this.targetPosition != null
                    && this.distanceToTarget() <= 0.9D) {

                this.onHit(
                        new BlockHitResult(
                                this.targetPosition,
                                Direction.UP,
                                BlockPos.containing(
                                        this.targetPosition
                                ),
                                false
                        )
                );

                return;
            }

            /*
             * ====================================================
             * HOMING / CONVERGENCE
             * ====================================================
             *
             * The meteor continually adjusts its direction toward
             * its target.
             *
             * Because BurningMeteorSpell gives each meteor a
             * slightly different target point, the meteors don't
             * all converge on exactly the same location.
             */
            if (this.targetPosition != null) {

                Vec3 direction =
                        this.targetPosition
                                .subtract(this.position())
                                .normalize();

                Vec3 currentMotion =
                        this.getDeltaMovement();

                double speed =
                        currentMotion.length();

                speed = Math.max(
                        INITIAL_SPEED,
                        speed + ACCELERATION
                );

                speed = Math.min(
                        MAX_SPEED,
                        speed
                );

                Vec3 desiredMotion =
                        direction.scale(speed);

                /*
                 * Smooth steering.
                 *
                 * This keeps the meteor from making an unnatural
                 * instantaneous turn toward the target.
                 */
                Vec3 newMotion =
                        currentMotion
                                .scale(0.75D)
                                .add(
                                        desiredMotion.scale(0.25D)
                                );

                this.setDeltaMovement(newMotion);
            }

            /*
             * ====================================================
             * MOVEMENT
             * ====================================================
             */

            Vec3 motion =
                    this.getDeltaMovement();

            double nextX =
                    this.getX() + motion.x;

            double nextY =
                    this.getY() + motion.y;

            double nextZ =
                    this.getZ() + motion.z;

            /*
             * Rotate the entity toward its current movement.
             */
            ProjectileUtil.rotateTowardsMovement(
                    this,
                    0.2F
            );

            /*
             * ====================================================
             * TRAIL
             * ====================================================
             *
             * The particles are placed BEHIND the meteor based
             * on its current movement direction.
             *
             * For example:
             *
             *          ↓ direction of travel
             *
             *       ┌────────┐
             *       │ METEOR │
             *       └────────┘
             *           🔥
             *           🔥
             *
             * When the meteor curves, this offset curves with it.
             */

            Vec3 trailMotion = motion;

            if (trailMotion.lengthSqr() > 1.0E-6D) {

                Vec3 trailOffset =
                        trailMotion
                                .normalize()
                                .scale(-TRAIL_OFFSET);

                spawnTrailParticles(
                        nextX + trailOffset.x,
                        nextY + trailOffset.y,
                        nextZ + trailOffset.z
                );

            } else {

                /*
                 * Fallback in the extremely unlikely event that
                 * the meteor has no movement.
                 */
                spawnTrailParticles(
                        nextX,
                        nextY,
                        nextZ
                );
            }

            /*
             * Finally move the actual entity.
             */
            this.setPos(
                    nextX,
                    nextY,
                    nextZ
            );

        } else {
            this.discard();
        }
    }

    /*
     * ============================================================
     * TARGET DISTANCE
     * ============================================================
     */

    private double distanceToTarget() {

        if (targetPosition == null) {
            return Double.MAX_VALUE;
        }

        return this.position()
                .distanceTo(targetPosition);
    }

    /*
     * ============================================================
     * TRAIL PARTICLES
     * ============================================================
     */

    private void spawnTrailParticles(
            double x,
            double y,
            double z
    ) {

        /*
         * Main fiery smoke particle from Irons Spell Books.
         */
        this.level().addParticle(
                ParticleRegistry.FIERY_SMOKE_PARTICLE.get(),
                x,
                y,
                z,
                0.0D,
                0.0D,
                0.0D
        );

        /*
         * Vanilla flame.
         */
        this.level().addParticle(
                ParticleTypes.FLAME,
                x,
                y,
                z,
                0.0D,
                0.0D,
                0.0D
        );

        /*
         * Normal smoke.
         */
        if (this.tickCount % 2 == 0) {

            this.level().addParticle(
                    ParticleTypes.SMOKE,
                    x,
                    y,
                    z,
                    0.0D,
                    0.0D,
                    0.0D
            );
        }

        /*
         * Larger smoke occasionally.
         */
        if (this.tickCount % 5 == 0) {

            this.level().addParticle(
                    ParticleTypes.LARGE_SMOKE,
                    x,
                    y,
                    z,
                    0.0D,
                    0.0D,
                    0.0D
            );
        }

        /*
         * Small amount of random flame around the trail.
         */
        if (this.tickCount % 2 == 0) {

            double offsetX =
                    (this.random.nextDouble() - 0.5D)
                            * 0.3D;

            double offsetY =
                    (this.random.nextDouble() - 0.5D)
                            * 0.3D;

            double offsetZ =
                    (this.random.nextDouble() - 0.5D)
                            * 0.3D;

            this.level().addParticle(
                    ParticleTypes.FLAME,
                    x + offsetX,
                    y + offsetY,
                    z + offsetZ,
                    0.0D,
                    0.0D,
                    0.0D
            );
        }
    }

    /*
     * ============================================================
     * IMPACT
     * ============================================================
     */

    @Override
    protected void onHit(HitResult result) {

        super.onHit(result);

        if (this.hasHit
                || this.level().isClientSide) {
            return;
        }

        this.hasHit = true;

        Entity directHit = null;

        if (result instanceof EntityHitResult entityHit) {
            directHit = entityHit.getEntity();
        }

        /*
         * ========================================================
         * DIRECT DAMAGE
         * ========================================================
         */

        if (directHit instanceof LivingEntity living
                && shouldAffect(living)) {

            living.invulnerableTime = 0;

            living.hurt(
                    this.damageSources().indirectMagic(
                            this,
                            this.getOwner()
                    ),
                    this.damage
            );

            living.setSecondsOnFire(5);
        }

        /*
         * ========================================================
         * AOE DAMAGE
         * ========================================================
         */

        applyAreaDamage();

        /*
         * ========================================================
         * IMPACT PARTICLES
         * ========================================================
         */

        if (this.level() instanceof ServerLevel serverLevel) {

            /*
             * Large fiery smoke burst.
             */
            serverLevel.sendParticles(
                    ParticleRegistry.FIERY_SMOKE_PARTICLE.get(),
                    this.getX(),
                    this.getY() + 0.25D,
                    this.getZ(),
                    40,
                    1.0D,
                    0.5D,
                    1.0D,
                    0.08D
            );

            /*
             * Smoke burst.
             */
            serverLevel.sendParticles(
                    ParticleTypes.LARGE_SMOKE,
                    this.getX(),
                    this.getY() + 0.25D,
                    this.getZ(),
                    15,
                    0.7D,
                    0.3D,
                    0.7D,
                    0.04D
            );

            /*
             * Explosion visual.
             */
            serverLevel.sendParticles(
                    ParticleTypes.EXPLOSION,
                    this.getX(),
                    this.getY(),
                    this.getZ(),
                    1,
                    0.0D,
                    0.0D,
                    0.0D,
                    0.0D
            );

            /*
             * Flames shooting upward.
             */
            serverLevel.sendParticles(
                    ParticleTypes.FLAME,
                    this.getX(),
                    this.getY(),
                    this.getZ(),
                    25,
                    0.5D,
                    1.0D,
                    0.5D,
                    0.05D
            );
        }

        this.discard();
    }

    /*
     * ============================================================
     * AOE DAMAGE
     * ============================================================
     */

    private void applyAreaDamage() {

        AABB area =
                this.getBoundingBox()
                        .inflate(this.aoeRadius);

        List<LivingEntity> entities =
                this.level().getEntitiesOfClass(
                        LivingEntity.class,
                        area
                );

        for (LivingEntity entity : entities) {

            if (!shouldAffect(entity)) {
                continue;
            }

            double distance =
                    this.distanceTo(entity);

            if (distance > this.aoeRadius) {
                continue;
            }

            /*
             * Damage falls off toward the edge of the impact.
             */
            float multiplier =
                    (float) (
                            1.0D
                                    - distance
                                    / this.aoeRadius
                    );

            float finalDamage =
                    this.aoeDamage * multiplier;

            if (finalDamage <= 0.0F) {
                continue;
            }

            entity.invulnerableTime = 0;

            entity.hurt(
                    this.damageSources().indirectMagic(
                            this,
                            this.getOwner()
                    ),
                    finalDamage
            );

            entity.setSecondsOnFire(5);
        }
    }

    /*
     * ============================================================
     * TARGET FILTERING
     * ============================================================
     */

    private boolean shouldAffect(
            LivingEntity entity
    ) {

        /*
         * Never hit the caster.
         */
        if (entity == this.getOwner()) {
            return false;
        }

        LivingEntity owner =
                this.getOwner() instanceof LivingEntity livingOwner
                        ? livingOwner
                        : null;

        /*
         * Don't damage allies.
         */
        return owner == null
                || !owner.isAlliedTo(entity);
    }

    /*
     * ============================================================
     * PROJECTILE SETTINGS
     * ============================================================
     */

    @Override
    protected boolean shouldBurn() {
        return false;
    }

    @Override
    protected float getInertia() {

        /*
         * Movement is manually controlled in tick(), so we don't
         * want AbstractHurtingProjectile applying its own drag.
         */
        return 1.0F;
    }

    @Override
    protected ParticleOptions getTrailParticle() {
        return ParticleTypes.FLAME;
    }

    @Override
    public boolean isPickable() {
        return false;
    }

    @Override
    public boolean ignoreExplosion() {
        return true;
    }

    /*
     * ============================================================
     * GECKOLIB
     * ============================================================
     */

    @Override
    public void registerControllers(
            AnimatableManager.ControllerRegistrar controllers
    ) {
    }

    @Override
    public AnimatableInstanceCache
    getAnimatableInstanceCache() {
        return cache;
    }
}