package net.kamaarion.roacw.entity.projectile.plague_nuke;

import io.redspace.ironsspellbooks.api.util.CameraShakeData;
import io.redspace.ironsspellbooks.api.util.CameraShakeManager;
import io.redspace.ironsspellbooks.registries.ParticleRegistry;
import net.kamaarion.roacw.Utils;
import net.kamaarion.roacw.entity.projectile.plague_cloud.PlagueCloudEntity;
import net.kamaarion.roacw.registeries.ROACWEffectRegistry;

import net.kamaarion.roacw.registeries.ROACWEntityRegistry;
import net.kamaarion.roacw.registeries.ROACWSoundRegistry;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.AbstractHurtingProjectile;
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

import java.util.List;

public class PlagueNukeEntity extends AbstractHurtingProjectile implements GeoEntity {

    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    private static final float EXPLOSION_POWER = 10.0F;
    private static final float DIRECT_HIT_DAMAGE = 30.0F;
    private static final float SPLASH_DAMAGE = 30.0F;

    private static final double KNOCKBACK_STRENGTH = 1.25D;
    private static final double KNOCKBACK_VERTICAL = 0.6D;
    private static final double KNOCKBACK_LIFT = 0.2D;

    private static final int PLAGUE_DURATION_TICKS = 200;
    private static final int PLAGUE_AMPLIFIER = 0;

    private boolean detonated = false;

    public PlagueNukeEntity(EntityType<? extends AbstractHurtingProjectile> type, Level level) {
        super(type, level);
    }

    public PlagueNukeEntity(EntityType<? extends AbstractHurtingProjectile> type, Level level, Player shooter) {
        super(type, shooter.getX(), shooter.getEyeY() - 0.1D, shooter.getZ(), 0, 0, 0, level);
        this.setOwner(shooter);
    }

    @Override
    public void tick() {

        if (!this.level().isClientSide && this.level() instanceof ServerLevel serverLevel) {

            serverLevel.sendParticles(
                    ParticleRegistry.ACID_PARTICLE.get(),
                    this.getX(), this.getY(), this.getZ(),
                    4, 0.08D, 0.08D, 0.08D, 0.0D
            );

            serverLevel.sendParticles(
                    ParticleRegistry.ACID_BUBBLE_PARTICLE.get(),
                    this.getX(), this.getY(), this.getZ(),
                    2, 0.05D, 0.05D, 0.05D, 0.0D
            );
        }

        super.tick();
    }

    @Override
    protected boolean shouldBurn() {
        return false;
    }

    @Override
    protected void onHit(HitResult result) {
        if (!this.level().isClientSide) {

            LivingEntity directHit = null;

            if (result instanceof EntityHitResult entityHit &&
                    entityHit.getEntity() instanceof LivingEntity living) {
                directHit = living;
            }

            detonate(directHit);
        }
    }

    private void detonate(LivingEntity directHit) {

        if (detonated) {
            return;
        }

        detonated = true;

        playExplosionEffects();

        // Read once per detonation and reuse across direct-hit + splash so
        // both damage instances use the same snapshot of the owner's
        // attribute value.
        float damageMultiplier = (this.getOwner() instanceof LivingEntity owner)
                ? Utils.getMagicProjectileDamageMultiplier(owner)
                : 1.0F;

        if (directHit != null &&
                directHit != this.getOwner() &&
                !(this.getOwner() != null && this.getOwner().isAlliedTo(directHit))) {

            directHit.invulnerableTime = 0;

            directHit.hurt(
                    this.damageSources().thrown(this, this.getOwner()),
                    DIRECT_HIT_DAMAGE * damageMultiplier
            );
        }

        playExplosionEffects();

        doScreenShake();

        spawnBurstCloud();

        applySplashDamage(directHit, damageMultiplier);

        applyPlagueEffect();

        spawnPlagueCloud();

        applyExplosionKnockback();

        discard();
    }

    private List<LivingEntity> getAffectedEntities() {
        return this.level().getEntitiesOfClass(
                LivingEntity.class,
                new AABB(this.position(), this.position()).inflate(EXPLOSION_POWER)
        );
    }

    private boolean shouldAffect(LivingEntity entity) {

        if (entity == this.getOwner())
            return false;

        return this.getOwner() == null
                || !this.getOwner().isAlliedTo(entity);
    }

    private void applySplashDamage(LivingEntity excluded, float damageMultiplier) {

        List<LivingEntity> affected = getAffectedEntities();

        for (LivingEntity entity : affected) {

            if (entity == excluded)
                continue;

            if (!shouldAffect(entity))
                continue;

            entity.invulnerableTime = 0;

            entity.hurt(
                    this.damageSources().thrown(this, this.getOwner()),
                    SPLASH_DAMAGE * damageMultiplier
            );
        }
    }

    private void playExplosionEffects() {

        if (level() instanceof ServerLevel serverLevel) {

            serverLevel.sendParticles(
                    ParticleTypes.EXPLOSION,
                    getX(), getY(), getZ(),
                    1, 0, 0, 0, 0
            );

            serverLevel.sendParticles(
                    ParticleTypes.SMOKE,
                    getX(), getY(), getZ(),
                    15, 0.3, 0.3, 0.3, 0.02
            );
        }

        level().playSound(
                null,
                getX(), getY(), getZ(),
                ROACWSoundRegistry.PLAGUE_NUKE_EXPLOSION.get(),
                SoundSource.HOSTILE,
                4.0F,
                1.0F
        );
    }

    private void doScreenShake() {
        CameraShakeManager.addCameraShake(
                new CameraShakeData(
                        level(),
                        20,
                        position(),
                        40.0F
                )
        );
    }

    private void spawnBurstCloud() {

        if (level().isClientSide) {
            return;
        }

        PlagueCloudEntity burst = new PlagueCloudEntity(
                ROACWEntityRegistry.PLAGUE_CLOUD.get(),
                level()
        );

        burst.setPos(getX(), getY(), getZ());

        burst.setBurstOnly(true);

        if (getOwner() instanceof LivingEntity owner) {
            burst.setOwner(owner);
        }

        level().addFreshEntity(burst);
    }

    private void applyExplosionKnockback() {

        List<LivingEntity> affected = getAffectedEntities();

        for (LivingEntity entity : affected) {

            if (!shouldAffect(entity))
                continue;

            Vec3 offset = entity.position().subtract(this.position());

            double distance = offset.length();

            if (distance <= 0.001D)
                continue;

            double strength = 1.0D - (distance / EXPLOSION_POWER);

            if (strength <= 0)
                continue;

            Vec3 motion = offset.normalize().scale(strength * KNOCKBACK_STRENGTH);

            entity.push(
                    motion.x,
                    motion.y * KNOCKBACK_VERTICAL + KNOCKBACK_LIFT,
                    motion.z
            );

            entity.hurtMarked = true;
        }
    }

    private void applyPlagueEffect() {
        List<LivingEntity> affected = getAffectedEntities();

        int amplifier = (this.getOwner() instanceof LivingEntity owner)
                ? Utils.getNatureScaledPlagueAmplifier(owner)
                : PLAGUE_AMPLIFIER;

        for (LivingEntity entity : affected) {
            if (!shouldAffect(entity))
                continue;

            entity.addEffect(new MobEffectInstance(
                    ROACWEffectRegistry.PLAGUE.get(),
                    PLAGUE_DURATION_TICKS,
                    amplifier,
                    false, false, true
            ));
        }
    }

    private void spawnPlagueCloud() {
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
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {}

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return cache;
    }
}