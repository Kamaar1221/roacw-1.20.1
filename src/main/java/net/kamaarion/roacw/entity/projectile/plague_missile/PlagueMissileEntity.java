package net.kamaarion.roacw.entity.projectile.plague_missile;

import io.redspace.ironsspellbooks.api.spells.AbstractSpell;
import io.redspace.ironsspellbooks.damage.DamageSources;
import net.kamaarion.roacw.entity.summon.mini_plaguebringer.MiniPlaguebringerEntity;
import net.kamaarion.roacw.particle.PlagueParticleHelper;
import net.kamaarion.roacw.registeries.ROACWEffectRegistry;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
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
import java.util.List;

public class PlagueMissileEntity
        extends AbstractHurtingProjectile
        implements GeoEntity {

    private static final float BASE_DAMAGE = 6.0F;
    private static final float BASE_SPLASH_DAMAGE = 3.0F;

    private static final double LAUNCH_SPEED = 0.4D;
    private static final double EXPLOSION_RADIUS = 2.25D;

    private static final int PLAGUE_DURATION_TICKS = 200;
    private static final int PLAGUE_AMPLIFIER = 0;

    private static final double KNOCKBACK_STRENGTH = 0.65D;
    private static final double KNOCKBACK_VERTICAL = 0.5D;
    private static final double KNOCKBACK_LIFT = 0.15D;

    private static final int CLOUD_CLUSTER_COUNT = 3;
    private static final double CLOUD_SCATTER_RADIUS = 0.5D;

    private static final float MINI_CLOUD_RADIUS = 0.8F;
    private static final double MINI_CLOUD_HEIGHT = 0.7D;

    private static final int MINI_CLOUD_PARTICLES = 4;
    private static final int MINI_CLOUD_GREEN_PARTICLES = 5;
    private static final int MINI_CLOUD_RED_PARTICLES = 2;

    private static final double SMOKE_MIN_BACK_DISTANCE = 0.40D;
    private static final double SMOKE_MAX_BACK_DISTANCE = 0.65D;
    private static final double SMOKE_SPREAD = 0.18D;

    private final AnimatableInstanceCache cache =
            GeckoLibUtil.createInstanceCache(this);

    private float damage = BASE_DAMAGE;
    private float splashDamage = BASE_SPLASH_DAMAGE;

    @Nullable
    private AbstractSpell damageSpell;

    private boolean hasHit = false;

    @Nullable
    private LivingEntity summonOwner;

    public PlagueMissileEntity(
            EntityType<? extends AbstractHurtingProjectile> type,
            Level level
    ) {
        super(type, level);
    }

    public PlagueMissileEntity(
            EntityType<? extends AbstractHurtingProjectile> type,
            Level level,
            MiniPlaguebringerEntity spawner,
            @Nullable LivingEntity summonOwner,
            Vec3 spawnPos,
            Vec3 direction,
            AbstractSpell damageSpell
    ) {
        super(
                type,
                spawnPos.x,
                spawnPos.y,
                spawnPos.z,
                0.0D,
                0.0D,
                0.0D,
                level
        );

        this.setOwner(spawner);
        this.summonOwner = summonOwner;
        this.damageSpell = damageSpell;

        Vec3 normalizedDirection;

        if (direction.lengthSqr() > 1.0E-6D) {
            normalizedDirection = direction.normalize();
        } else {
            normalizedDirection = new Vec3(0.0D, 0.0D, 1.0D);
        }

        this.setDeltaMovement(
                normalizedDirection.scale(LAUNCH_SPEED)
        );

        double horizontalDistance =
                normalizedDirection.horizontalDistance();

        float yaw = (float) (
                -Mth.atan2(
                        normalizedDirection.z,
                        normalizedDirection.x
                ) * (180D / Math.PI)
        ) + 90.0F;

        float pitch = (float) (
                -Mth.atan2(
                        normalizedDirection.y,
                        horizontalDistance
                ) * (180D / Math.PI)
        );

        this.setYRot(yaw);
        this.setXRot(pitch);

        this.yRotO = yaw;
        this.xRotO = pitch;
    }

    public void setDamage(float damage) {
        this.damage = damage;
    }

    public float getDamage() {
        return this.damage;
    }

    public void setSplashDamage(float splashDamage) {
        this.splashDamage = splashDamage;
    }

    public float getSplashDamage() {
        return this.splashDamage;
    }

    public void setSummonOwner(
            @Nullable LivingEntity summonOwner
    ) {
        this.summonOwner = summonOwner;
    }

    public void setDamageSpell(
            @Nullable AbstractSpell damageSpell
    ) {
        this.damageSpell = damageSpell;
    }

    @Nullable
    public AbstractSpell getDamageSpell() {
        return this.damageSpell;
    }

    @Nullable
    private net.minecraft.world.damagesource.DamageSource getSpellDamageSource() {
        if (this.damageSpell == null || this.summonOwner == null) {
            return null;
        }

        return this.damageSpell.getDamageSource(
                this,
                this.summonOwner
        );
    }

    @Override
    public void tick() {
        if (!this.shouldBurn()) {
            this.clearFire();
        }

        super.baseTick();

        Vec3 movement = this.getDeltaMovement();

        HitResult hitResult = ProjectileUtil.getHitResultOnMoveVector(
                this,
                this::canHitEntity
        );

        if (hitResult.getType() != HitResult.Type.MISS) {
            this.onHit(hitResult);

            if (this.isRemoved()) {
                return;
            }
        }

        this.setPos(
                this.getX() + movement.x,
                this.getY() + movement.y,
                this.getZ() + movement.z
        );

        if (movement.lengthSqr() > 1.0E-6D) {
            this.yRotO = this.getYRot();
            this.xRotO = this.getXRot();

            double horizontalDistance = movement.horizontalDistance();

            float yaw = (float) (
                    -Mth.atan2(
                            movement.z,
                            movement.x
                    ) * (180D / Math.PI)
            ) + 90.0F;

            float pitch = (float) (
                    -Mth.atan2(
                            movement.y,
                            horizontalDistance
                    ) * (180D / Math.PI)
            );

            this.setYRot(yaw);
            this.setXRot(pitch);
        }

        this.setDeltaMovement(
                movement.scale(this.getInertia())
        );

        if (this.level() instanceof ServerLevel serverLevel) {
            this.spawnTrailParticles(serverLevel);
        }
    }

    private void spawnTrailParticles(ServerLevel serverLevel) {
        Vec3 velocity = this.getDeltaMovement();

        if (velocity.lengthSqr() <= 1.0E-6D) {
            return;
        }

        Vec3 forward = velocity.normalize();

        Vec3 worldUp = new Vec3(0.0D, 1.0D, 0.0D);
        Vec3 right = forward.cross(worldUp);

        if (right.lengthSqr() < 1.0E-6D) {
            right = new Vec3(1.0D, 0.0D, 0.0D);
        } else {
            right = right.normalize();
        }

        double smokeBackDistance =
                SMOKE_MIN_BACK_DISTANCE
                        + this.random.nextDouble()
                        * (SMOKE_MAX_BACK_DISTANCE - SMOKE_MIN_BACK_DISTANCE);

        double smokeSideOffset =
                (this.random.nextDouble() - 0.5D) * SMOKE_SPREAD;

        Vec3 smokePosition = this.position()
                .subtract(forward.scale(smokeBackDistance))
                .add(right.scale(smokeSideOffset))
                .add(0.0D, 0.15D, 0.0D);

        serverLevel.sendParticles(
                ParticleTypes.SMOKE,
                smokePosition.x,
                smokePosition.y,
                smokePosition.z,
                1,
                0.02D,
                0.02D,
                0.02D,
                0.015D
        );
    }

    private void updateRotationFromVelocity() {
        Vec3 motion = this.getDeltaMovement();

        if (motion.lengthSqr() <= 1.0E-6D) {
            return;
        }

        this.yRotO = this.getYRot();
        this.xRotO = this.getXRot();

        double horizontalDistance = motion.horizontalDistance();

        float yaw = (float) (
                -Mth.atan2(
                        motion.z,
                        motion.x
                ) * (180D / Math.PI)
        ) + 90.0F;

        float pitch = (float) (
                -Mth.atan2(
                        motion.y,
                        horizontalDistance
                ) * (180D / Math.PI)
        );

        this.setYRot(yaw);
        this.setXRot(pitch);
    }

    @Override
    protected boolean shouldBurn() {
        return false;
    }

    @Override
    protected float getInertia() {
        return 1.0F;
    }

    @Override
    protected boolean canHitEntity(Entity entity) {
        if (!super.canHitEntity(entity)) {
            return false;
        }

        if (entity == this.getOwner() || entity == this.summonOwner) {
            return false;
        }

        if (entity instanceof MiniPlaguebringerEntity mini
                && this.getOwner() instanceof MiniPlaguebringerEntity plaguebringer
                && plaguebringer.isFriendlySummon(mini)) {
            return false;
        }

        if (this.getOwner() instanceof MiniPlaguebringerEntity plaguebringer) {
            LivingEntity owner = plaguebringer.getSummonOwner();

            if (owner != null && owner.isAlliedTo(entity)) {
                return false;
            }
        }

        return true;
    }

    @Override
    protected void onHit(HitResult result) {
        super.onHit(result);

        if (this.hasHit || this.level().isClientSide) {
            return;
        }

        this.hasHit = true;

        Entity directHit = null;

        if (result instanceof EntityHitResult entityHit) {
            directHit = entityHit.getEntity();
        }

        var source = this.getSpellDamageSource();

        if (source == null) {
            this.discard();
            return;
        }

        if (directHit instanceof LivingEntity living
                && this.shouldAffect(living)) {
            living.invulnerableTime = 0;

            boolean damaged = DamageSources.applyDamage(
                    living,
                    this.damage,
                    source
            );

            if (damaged) {
                this.applyPlague(living);
            }
        }

        this.applySplashDamage(directHit, source);
        this.spawnImpactParticles();
        this.discard();
    }

    private void applySplashDamage(
            @Nullable Entity directHit,
            net.minecraft.world.damagesource.DamageSource source
    ) {
        AABB area = this.getBoundingBox().inflate(EXPLOSION_RADIUS);

        List<LivingEntity> entities = this.level().getEntitiesOfClass(
                LivingEntity.class,
                area,
                this::shouldAffect
        );

        for (LivingEntity entity : entities) {
            if (entity == directHit) {
                continue;
            }

            entity.invulnerableTime = 0;

            boolean damaged = DamageSources.applyDamage(
                    entity,
                    this.splashDamage,
                    source
            );

            if (damaged) {
                this.applyPlague(entity);
                this.applyKnockback(entity);
            }
        }
    }

    private void applyPlague(LivingEntity entity) {
        LivingEntity effectOwner = this.summonOwner;

        MobEffectInstance plague = new MobEffectInstance(
                ROACWEffectRegistry.PLAGUE.get(),
                PLAGUE_DURATION_TICKS,
                PLAGUE_AMPLIFIER,
                false,
                false,
                true
        );

        if (effectOwner != null) {
            entity.addEffect(plague, effectOwner);
        } else {
            entity.addEffect(plague);
        }
    }

    private void applyKnockback(LivingEntity entity) {
        Vec3 offset = entity.position().subtract(this.position());
        double distance = offset.length();

        if (distance <= 0.001D) {
            return;
        }

        double strength = 1.0D - distance / EXPLOSION_RADIUS;

        if (strength <= 0.0D) {
            return;
        }

        Vec3 direction = offset.normalize();
        double horizontalStrength = strength * KNOCKBACK_STRENGTH;

        entity.push(
                direction.x * horizontalStrength,
                direction.y * horizontalStrength * KNOCKBACK_VERTICAL
                        + KNOCKBACK_LIFT,
                direction.z * horizontalStrength
        );

        entity.hurtMarked = true;
    }

    private void spawnImpactParticles() {
        if (!(this.level() instanceof ServerLevel serverLevel)) {
            return;
        }

        for (int i = 0; i < CLOUD_CLUSTER_COUNT; i++) {
            double angle = this.random.nextDouble() * Math.PI * 2.0D;
            double radius = this.random.nextDouble() * CLOUD_SCATTER_RADIUS;

            double x = this.getX() + Math.cos(angle) * radius;
            double z = this.getZ() + Math.sin(angle) * radius;

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

    private boolean shouldAffect(LivingEntity entity) {
        Entity projectileOwner = this.getOwner();

        if (entity == projectileOwner || entity == this.summonOwner) {
            return false;
        }

        if (projectileOwner instanceof MiniPlaguebringerEntity plaguebringer) {
            if (plaguebringer.isFriendlySummon(entity)) {
                return false;
            }

            LivingEntity owner = plaguebringer.getSummonOwner();

            if (owner != null && owner.isAlliedTo(entity)) {
                return false;
            }

            return true;
        }

        if (projectileOwner instanceof LivingEntity livingOwner) {
            return !livingOwner.isAlliedTo(entity);
        }

        return true;
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