package net.kamaarion.roacw.entity.projectile.plague_nuke;

import io.redspace.ironsspellbooks.api.util.CameraShakeData;
import io.redspace.ironsspellbooks.api.util.CameraShakeManager;
import net.kamaarion.roacw.Utils;
import net.kamaarion.roacw.entity.projectile.plague_cloud.PlagueCloudEntity;
import net.kamaarion.roacw.registeries.ROACWEffectRegistry;
import net.kamaarion.roacw.registeries.ROACWEntityRegistry;
import net.kamaarion.roacw.registeries.ROACWSoundRegistry;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
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

import java.util.List;

public class PlagueNukeEntity extends AbstractHurtingProjectile implements GeoEntity {

    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    private static final float EXPLOSION_POWER = 10.0F;
    private static final float DIRECT_HIT_DAMAGE = 30.0F;
    private static final float SPLASH_DAMAGE = 30.0F;
    private static final int MAX_LIFETIME_TICKS = 200;

    private static final double KNOCKBACK_STRENGTH = 1.25D;
    private static final double KNOCKBACK_VERTICAL = 0.6D;
    private static final double KNOCKBACK_LIFT = 0.2D;

    private static final int PLAGUE_DURATION_TICKS = 200;
    private static final int PLAGUE_AMPLIFIER = 0;
    private static final double MIN_ROTATION_SPEED_SQR = 1.0E-6D;

    private boolean detonated;
    private boolean hasValidRotation;

    private float lastValidYaw;
    private float lastValidPitch;

    public PlagueNukeEntity(
            EntityType<? extends AbstractHurtingProjectile> type,
            Level level
    ) {
        super(type, level);
    }

    public PlagueNukeEntity(
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

    @Override
    public void tick() {
        if (!this.level().isClientSide && this.tickCount >= MAX_LIFETIME_TICKS) {
            this.detonate(null);
            return;
        }

        Vec3 movementBeforeTick = this.getDeltaMovement();
        boolean stationaryBeforeTick = movementBeforeTick.lengthSqr() < MIN_ROTATION_SPEED_SQR;

        if (!stationaryBeforeTick) {
            this.saveValidRotation();
        }

        super.tick();

        if (this.isRemoved()) {
            return;
        }

        Vec3 movementAfterTick = this.getDeltaMovement();

        if (stationaryBeforeTick || movementAfterTick.lengthSqr() < MIN_ROTATION_SPEED_SQR) {
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

    @Override
    protected void onHit(HitResult result) {
        if (result instanceof EntityHitResult entityHit
                && entityHit.getEntity() instanceof Player) {
            return;
        }

        super.onHit(result);

        if (!this.level().isClientSide) {
            LivingEntity directHit = null;

            if (result instanceof EntityHitResult entityHit
                    && entityHit.getEntity() instanceof LivingEntity living) {
                directHit = living;
            }

            this.detonate(directHit);
        }
    }

    private void detonate(LivingEntity directHit) {
        if (this.detonated) {
            return;
        }

        this.detonated = true;

        this.playExplosionEffects();
        this.doScreenShake();

        float damageMultiplier = this.getOwner() instanceof LivingEntity owner
                ? Utils.getMagicProjectileDamageMultiplier(owner)
                : 1.0F;

        if (directHit != null && this.shouldAffect(directHit)) {
            directHit.invulnerableTime = 0;

            directHit.hurt(
                    this.damageSources().thrown(this, this.getOwner()),
                    DIRECT_HIT_DAMAGE * damageMultiplier
            );
        }

        this.spawnBurstCloud();
        this.applySplashDamage(directHit, damageMultiplier);
        this.applyPlagueEffect();
        this.spawnPlagueCloud();
        this.applyExplosionKnockback();
        this.discard();
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
            LivingEntity excluded,
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

    private void playExplosionEffects() {
        this.level().playSound(
                null,
                this.getX(),
                this.getY(),
                this.getZ(),
                ROACWSoundRegistry.PLAGUE_NUKE_EXPLOSION.get(),
                SoundSource.HOSTILE,
                4.0F,
                1.0F
        );
    }

    private void doScreenShake() {
        CameraShakeManager.addCameraShake(
                new CameraShakeData(
                        this.level(),
                        20,
                        this.position(),
                        40.0F
                )
        );
    }

    private void spawnBurstCloud() {
        if (this.level().isClientSide) {
            return;
        }

        PlagueCloudEntity burst = new PlagueCloudEntity(
                ROACWEntityRegistry.PLAGUE_CLOUD.get(),
                this.level()
        );

        burst.setPos(this.getX(), this.getY(), this.getZ());
        burst.setBurstOnly(true);

        if (this.getOwner() instanceof LivingEntity owner) {
            burst.setOwner(owner);
        }

        this.level().addFreshEntity(burst);
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

    private void spawnPlagueCloud() {
        if (this.level().isClientSide) {
            return;
        }

        PlagueCloudEntity cloud = new PlagueCloudEntity(
                ROACWEntityRegistry.PLAGUE_CLOUD.get(),
                this.level()
        );

        cloud.setPos(this.getX(), this.getY(), this.getZ());

        if (this.getOwner() instanceof LivingEntity living) {
            cloud.setOwner(living);
        }

        this.level().addFreshEntity(cloud);
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