package net.kamaarion.roacw.entity.projectile.plague_rocket;

import net.kamaarion.roacw.Utils;
import net.kamaarion.roacw.particle.PlagueParticleHelper;
import net.kamaarion.roacw.registeries.ROACWEffectRegistry;
import net.kamaarion.roacw.registeries.ROACWSoundRegistry;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractHurtingProjectile;
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
import java.util.List;

public class PlagueRocketEntity extends AbstractHurtingProjectile implements GeoEntity {

    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    private static final float EXPLOSION_POWER = 3.0F;
    private static final float DIRECT_HIT_DAMAGE = 8.0F;
    private static final float SPLASH_DAMAGE = 4.0F;
    private static final int MAX_LIFETIME_TICKS = 200;

    private static final double SEARCH_RADIUS = 24.0D;
    private static final double TURN_RATE = 0.50D;
    private static final int RETARGET_INTERVAL = 3;
    private static final int TARGETING_DELAY_TICKS = 15;
    private static final double PROXIMITY_DETONATE_RADIUS = 1.25D;

    private static final float BANK_SCALE = 4.0F;
    private static final float MAX_BANK_DEGREES = 40.0F;
    private static final float BANK_DECAY = 0.85F;

    private static final int PLAGUE_DURATION_TICKS = 200;
    private static final int PLAGUE_AMPLIFIER = 0;

    private static final double KNOCKBACK_STRENGTH = 1.25D;
    private static final double KNOCKBACK_VERTICAL = 0.6D;
    private static final double KNOCKBACK_LIFT = 0.2D;

    private static final int CLOUD_CLUSTER_COUNT = 4;
    private static final double CLOUD_SCATTER_RADIUS = 0.6D;
    private static final float MINI_CLOUD_RADIUS = 1.0F;
    private static final double MINI_CLOUD_HEIGHT = 0.8D;
    private static final int MINI_CLOUD_PARTICLES = 5;
    private static final int MINI_CLOUD_GREEN_PARTICLES = 6;
    private static final int MINI_CLOUD_RED_PARTICLES = 2;

    private static final String DUMMY_MOD_NAMESPACE = "dummmmmmy";
    private static final double MIN_ROTATION_SPEED_SQR = 1.0E-6D;

    private float bankAngle;
    private float previousBankAngle;
    private boolean detonated;
    private boolean hasValidRotation;

    private float lastValidYaw;
    private float lastValidPitch;

    @Nullable
    private LivingEntity target;

    private int retargetCooldown;

    public PlagueRocketEntity(EntityType<? extends AbstractHurtingProjectile> type, Level level) {
        super(type, level);
    }

    public PlagueRocketEntity(
            EntityType<? extends AbstractHurtingProjectile> type,
            Level level,
            Player shooter
    ) {
        super(
                type,
                shooter.getX(),
                shooter.getEyeY() - 0.1D,
                shooter.getZ(),
                0,
                0,
                0,
                level
        );

        this.setOwner(shooter);
        this.lastValidYaw = shooter.getYRot();
        this.lastValidPitch = shooter.getXRot();
        this.hasValidRotation = true;
        this.setYRot(this.lastValidYaw);
        this.setXRot(this.lastValidPitch);
        this.yRotO = this.lastValidYaw;
        this.xRotO = this.lastValidPitch;
    }

    public float getBankAngle() {
        return this.bankAngle;
    }

    public float getPreviousBankAngle() {
        return this.previousBankAngle;
    }

    @Override
    public void tick() {
        this.yRotO = this.getYRot();
        this.xRotO = this.getXRot();

        if (!this.level().isClientSide) {
            if (this.tickCount >= MAX_LIFETIME_TICKS) {
                this.detonate(null);
                return;
            }

            this.updateHoming();
        }

        Vec3 movementBeforeTick = this.getDeltaMovement();
        boolean stationaryBeforeTick =
                movementBeforeTick.lengthSqr() < MIN_ROTATION_SPEED_SQR;

        if (!stationaryBeforeTick) {
            this.saveValidRotation();
        }

        super.tick();

        if (this.isRemoved()) {
            return;
        }

        Vec3 movementAfterTick = this.getDeltaMovement();

        if (stationaryBeforeTick
                || movementAfterTick.lengthSqr() < MIN_ROTATION_SPEED_SQR) {
            this.restoreValidRotation();
        } else {
            this.saveValidRotation();
        }
    }

    private void saveValidRotation() {
        this.lastValidYaw = this.getYRot();
        this.lastValidPitch = this.getXRot();
        this.hasValidRotation = true;
    }

    private void restoreValidRotation() {
        if (!this.hasValidRotation) {
            this.saveValidRotation();
        }

        this.setYRot(this.lastValidYaw);
        this.setXRot(this.lastValidPitch);
        this.yRotO = this.lastValidYaw;
        this.xRotO = this.lastValidPitch;
    }

    @Override
    protected boolean shouldBurn() {
        return false;
    }

    @Override
    protected boolean canHitEntity(Entity entity) {
        return !(entity instanceof Player) && super.canHitEntity(entity);
    }

    private void updateHoming() {
        this.previousBankAngle = this.bankAngle;
        this.bankAngle *= BANK_DECAY;

        if (this.tickCount < TARGETING_DELAY_TICKS) {
            this.target = null;
            return;
        }

        if (this.target == null || !this.isValidTarget(this.target)) {
            this.target = this.findTarget();
            this.retargetCooldown = RETARGET_INTERVAL;
        } else if (--this.retargetCooldown <= 0) {
            LivingEntity newTarget = this.findTarget();

            if (newTarget != null
                    && newTarget.distanceToSqr(this) < this.target.distanceToSqr(this)) {
                this.target = newTarget;
            }

            this.retargetCooldown = RETARGET_INTERVAL;
        }

        if (this.target == null) {
            return;
        }

        if (this.target.distanceToSqr(this)
                <= PROXIMITY_DETONATE_RADIUS * PROXIMITY_DETONATE_RADIUS) {
            this.detonate(this.target);
            return;
        }

        Vec3 toTarget = this.target.getEyePosition().subtract(this.position()).normalize();
        Vec3 current = this.getDeltaMovement();
        double speed = current.length();

        if (speed <= 1.0E-4D) {
            return;
        }

        Vec3 currentDir = current.normalize();

        Vec3 blendedDir = currentDir
                .scale(1.0D - TURN_RATE)
                .add(toTarget.scale(TURN_RATE))
                .normalize();

        this.setDeltaMovement(blendedDir.scale(speed));

        float oldYaw = this.getYRot();
        float newYaw = (float) (
                Mth.atan2(blendedDir.x, blendedDir.z) * Mth.RAD_TO_DEG
        );

        float newPitch = (float) (
                Mth.atan2(
                        blendedDir.y,
                        Math.sqrt(
                                blendedDir.x * blendedDir.x
                                        + blendedDir.z * blendedDir.z
                        )
                ) * Mth.RAD_TO_DEG
        );

        float yawDelta = Mth.wrapDegrees(newYaw - oldYaw);

        this.bankAngle = Mth.clamp(
                -yawDelta * BANK_SCALE,
                -MAX_BANK_DEGREES,
                MAX_BANK_DEGREES
        );

        this.setYRot(newYaw);
        this.setXRot(newPitch);
    }

    @Nullable
    private LivingEntity findTarget() {
        AABB searchBox = this.getBoundingBox().inflate(SEARCH_RADIUS);

        List<LivingEntity> candidates = this.level().getEntitiesOfClass(
                LivingEntity.class,
                searchBox,
                this::isValidTarget
        );

        LivingEntity closest = null;
        double closestDistSq = Double.MAX_VALUE;

        for (LivingEntity candidate : candidates) {
            double distSq = candidate.distanceToSqr(this);

            if (distSq < closestDistSq) {
                closestDistSq = distSq;
                closest = candidate;
            }
        }

        return closest;
    }

    private boolean isValidTarget(LivingEntity candidate) {
        if (candidate instanceof Player
                || candidate == this.getOwner()
                || !candidate.isAlive()
                || candidate.isSpectator()) {
            return false;
        }

        if (this.getOwner() != null && this.getOwner().isAlliedTo(candidate)) {
            return false;
        }

        if (DUMMY_MOD_NAMESPACE.equals(
                BuiltInRegistries.ENTITY_TYPE.getKey(candidate.getType()).getNamespace()
        )) {
            return true;
        }

        return candidate instanceof Enemy;
    }

    @Override
    protected void onHit(HitResult result) {
        if (result instanceof EntityHitResult entityHit
                && entityHit.getEntity() instanceof Player) {
            return;
        }

        super.onHit(result);

        if (!this.level().isClientSide) {
            Entity directHit = result instanceof EntityHitResult entityHit
                    ? entityHit.getEntity()
                    : null;

            this.detonate(directHit);
        }
    }

    private void detonate(@Nullable Entity directHitEntity) {
        if (this.detonated) {
            return;
        }

        this.detonated = true;
        this.playExplosionEffects();

        float damageMultiplier = this.getOwner() instanceof LivingEntity owner
                ? Utils.getMagicProjectileDamageMultiplier(owner)
                : 1.0F;

        if (directHitEntity instanceof LivingEntity living
                && this.shouldAffect(living)) {
            living.invulnerableTime = 0;

            living.hurt(
                    this.damageSources().thrown(this, this.getOwner()),
                    DIRECT_HIT_DAMAGE * damageMultiplier
            );
        }

        this.spawnExplosionBurst();
        this.applySplashDamage(directHitEntity, damageMultiplier);
        this.applyPlagueEffect();
        this.applyExplosionKnockback();
        this.discard();
    }

    private void playExplosionEffects() {
        if (this.level() instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(
                    ParticleTypes.EXPLOSION,
                    this.getX(),
                    this.getY(),
                    this.getZ(),
                    1,
                    0,
                    0,
                    0,
                    0
            );

            serverLevel.sendParticles(
                    ParticleTypes.SMOKE,
                    this.getX(),
                    this.getY(),
                    this.getZ(),
                    8,
                    0.15D,
                    0.15D,
                    0.15D,
                    0.01D
            );
        }

        this.level().playSound(
                null,
                this.getX(),
                this.getY(),
                this.getZ(),
                ROACWSoundRegistry.PLAGUE_ROCKET_EXPLOSION.get(),
                SoundSource.HOSTILE,
                4.0F,
                1.0F
        );
    }

    private void spawnExplosionBurst() {
        if (!(this.level() instanceof ServerLevel serverLevel)) {
            return;
        }

        for (int i = 0; i < CLOUD_CLUSTER_COUNT; i++) {
            double angle = this.random.nextDouble() * Math.PI * 2.0D;
            double r = this.random.nextDouble() * CLOUD_SCATTER_RADIUS;

            double x = this.getX() + Math.cos(angle) * r;
            double z = this.getZ() + Math.sin(angle) * r;

            PlagueParticleHelper.spawnCloudBurstServer(
                    serverLevel,
                    x,
                    this.getY(),
                    z,
                    MINI_CLOUD_RADIUS,
                    MINI_CLOUD_HEIGHT,
                    MINI_CLOUD_PARTICLES,
                    MINI_CLOUD_GREEN_PARTICLES,
                    MINI_CLOUD_RED_PARTICLES
            );
        }
    }

    private void applyExplosionKnockback() {
        for (LivingEntity entity : this.getAffectedEntities()) {
            if (!this.shouldAffect(entity)) {
                continue;
            }

            Vec3 offset = entity.position().subtract(this.position());
            double distance = offset.length();

            if (distance <= 0.001D) {
                continue;
            }

            double strength = 1.0D - distance / EXPLOSION_POWER;

            if (strength <= 0.0D) {
                continue;
            }

            Vec3 motion = offset.normalize().scale(
                    strength * KNOCKBACK_STRENGTH
            );

            entity.push(
                    motion.x,
                    motion.y * KNOCKBACK_VERTICAL + KNOCKBACK_LIFT,
                    motion.z
            );

            entity.hurtMarked = true;
        }
    }

    private List<LivingEntity> getAffectedEntities() {
        return this.level().getEntitiesOfClass(
                LivingEntity.class,
                new AABB(this.position(), this.position()).inflate(EXPLOSION_POWER)
        );
    }

    private boolean shouldAffect(LivingEntity entity) {
        if (entity instanceof Player || entity == this.getOwner()) {
            return false;
        }

        return this.getOwner() == null || !this.getOwner().isAlliedTo(entity);
    }

    private void applySplashDamage(
            @Nullable Entity excluded,
            float damageMultiplier
    ) {
        for (LivingEntity entity : this.getAffectedEntities()) {
            if (entity == excluded || !this.shouldAffect(entity)) {
                continue;
            }

            entity.invulnerableTime = 0;

            entity.hurt(
                    this.damageSources().thrown(this, this.getOwner()),
                    SPLASH_DAMAGE * damageMultiplier
            );
        }
    }

    private void applyPlagueEffect() {
        int amplifier = this.getOwner() instanceof LivingEntity owner
                ? Utils.getNatureScaledPlagueAmplifier(owner)
                : PLAGUE_AMPLIFIER;

        for (LivingEntity entity : this.getAffectedEntities()) {
            if (!this.shouldAffect(entity)) {
                continue;
            }

            entity.addEffect(
                    new MobEffectInstance(
                            ROACWEffectRegistry.PLAGUE.get(),
                            PLAGUE_DURATION_TICKS,
                            amplifier,
                            false,
                            false,
                            true
                    )
            );
        }
    }

    @Override
    protected float getInertia() {
        return 1.0F;
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
    public void registerControllers(
            AnimatableManager.ControllerRegistrar controllers
    ) {
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.cache;
    }
}