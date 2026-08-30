package net.kamaarion.roacw.entity.projectile.plague_charger_stinger;

import net.kamaarion.roacw.entity.summon.plague_charger.PlagueChargerEntity;
import net.kamaarion.roacw.registeries.ROACWEffectRegistry;
import net.kamaarion.roacw.registeries.ROACWParticleRegistry;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.AbstractHurtingProjectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.util.GeckoLibUtil;

import javax.annotation.Nullable;

public class PlagueChargerStingerEntity extends AbstractHurtingProjectile implements GeoEntity {

    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    private static final DustParticleOptions STINGER_DUST =
            new DustParticleOptions(new org.joml.Vector3f(0.42F, 0.62F, 0.35F), 1.0F); // TODO match spirit's palette if desired

    private static final float BASE_DAMAGE = 3.0F;

    // Matches PlagueChargerEntity's dash-hit values so the summon applies
    // Plague consistently whether it's charging or stinging.
    private static final int PLAGUE_DURATION_TICKS = 200; // 10s
    private static final int PLAGUE_AMPLIFIER = 0;

    private float damage = BASE_DAMAGE;
    private boolean hasHit = false;

    // Vanilla's Entity#setOwner is used for the actual firer (the charger)
    // so shouldAffect() can correctly exclude it via getOwner() regardless
    // of whether this is a wild or summoned charger. summonOwner is tracked
    // separately, purely so a summoned charger's stinger also excludes its
    // player - previously these were conflated into one field, which meant
    // setOwner() got overwritten with the player for summoned chargers,
    // leaving the charger itself with no self-exclusion at all.
    @Nullable
    private LivingEntity summonOwner;

    public PlagueChargerStingerEntity(EntityType<? extends AbstractHurtingProjectile> type, Level level) {
        super(type, level);
    }

    public PlagueChargerStingerEntity(EntityType<? extends AbstractHurtingProjectile> type, Level level,
                                      LivingEntity spawner, @Nullable LivingEntity owner, Vec3 spawnPos, Vec3 direction) {
        super(type, spawnPos.x, spawnPos.y, spawnPos.z,
                direction.x, direction.y, direction.z, level);
        this.setOwner(spawner);
        this.summonOwner = owner;
    }

    public void setDamage(float damage) {
        this.damage = damage;
    }

    @Override
    public void tick() {
        Entity owner = this.getOwner();

        this.yRotO = this.getYRot();
        this.xRotO = this.getXRot();

        if (this.level().isClientSide || (owner == null || !owner.isRemoved()) && this.level().hasChunkAt(this.blockPosition())) {

            super.baseTick();

            if (this.shouldBurn()) {
                this.setSecondsOnFire(1);
            }

            HitResult hitResult = net.minecraft.world.entity.projectile.ProjectileUtil
                    .getHitResultOnMoveVector(this, this::canHitEntity);

            if (hitResult.getType() != HitResult.Type.MISS
                    && !net.minecraftforge.event.ForgeEventFactory.onProjectileImpact(this, hitResult)) {
                this.onHit(hitResult);
            }

            this.checkInsideBlocks();

            Vec3 motion = this.getDeltaMovement();

            double nextX = this.getX() + motion.x;
            double nextY = this.getY() + motion.y;
            double nextZ = this.getZ() + motion.z;

            net.minecraft.world.entity.projectile.ProjectileUtil.rotateTowardsMovement(this, 0.2F);

            float inertia = this.getInertia();

            this.setDeltaMovement(
                    motion.add(this.xPower, this.yPower, this.zPower)
                            .scale(inertia)
            );

            // Trail particle - spawned ourselves rather than relying on
            // AbstractHurtingProjectile's built-in per-tick trail (which
            // NPEs on a null getTrailParticle() in this version).
            Vec3 velocity = this.getDeltaMovement();

            if (velocity.lengthSqr() > 1.0E-6D) {
                Vec3 forward = velocity.normalize();

                Vec3 worldUp = new Vec3(0, 1, 0);

                Vec3 right = forward.cross(worldUp);
                if (right.lengthSqr() < 1.0E-6D) {
                    right = new Vec3(1, 0, 0);
                } else {
                    right = right.normalize();
                }

                Vec3 up = right.cross(forward).normalize();

                Vec3 particlePos = this.position()
                        .subtract(forward.scale(0.25D))
                        .add(up.scale(0.12D));

                this.level().addParticle(
                        this.random.nextBoolean()
                                ? ROACWParticleRegistry.PLAGUE_NANO_GREEN.get()
                                : ROACWParticleRegistry.PLAGUE_NANO_RED.get(),
                        particlePos.x,
                        particlePos.y,
                        particlePos.z,
                        0.0D,
                        0.0D,
                        0.0D
                );
            }

            this.setPos(nextX, nextY, nextZ);
        } else {
            this.discard();
        }
    }

    @Override
    protected boolean shouldBurn() {
        return false;
    }

    @Override
    protected float getInertia() {
        return 0.85F; // matches BelladonnaPetalEntity's fix - avoids unbounded acceleration
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

        if (directHit instanceof LivingEntity living && shouldAffect(living)) {
            living.invulnerableTime = 0;
            living.hurt(this.damageSources().thrown(this, this.getOwner()), this.damage);

            living.addEffect(new MobEffectInstance(
                    ROACWEffectRegistry.PLAGUE.get(),
                    PLAGUE_DURATION_TICKS,
                    PLAGUE_AMPLIFIER,
                    false,
                    true));
        }

        if (this.level() instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(STINGER_DUST,
                    this.getX(), this.getY(0.5D), this.getZ(),
                    10, 0.2D, 0.2D, 0.2D, 0.01D);
        }

        this.discard();
    }

    private boolean shouldAffect(LivingEntity entity) {
        if (entity == this.getOwner() || entity == summonOwner) {
            return false;
        }
        // Same-side Chargers only - a summoned Charger's stinger should
        // still land on a wild Charger (and vice versa). getOwner() here is
        // the firer itself (see the constructor comment), so this asks the
        // firing Charger directly whether the hit target is on its side,
        // via the same isFriendlyCharger() rule used everywhere else on
        // PlagueChargerEntity. Previously this excluded PlagueChargerEntity
        // outright, which is what blocked the stinger even after the dash
        // and targeting were already fixed to allow summon-vs-wild combat.
        if (entity instanceof PlagueChargerEntity
                && this.getOwner() instanceof PlagueChargerEntity firer
                && firer.isFriendlyCharger(entity)) {
            return false;
        }
        // Alliance check needs the player, not the firer - see
        // BelladonnaPetalEntity for why this matters even though the
        // species check above already covers charger-vs-charger.
        // getOwner() returns Entity, not LivingEntity - narrow it safely
        // rather than casting blindly.
        LivingEntity allianceCheckEntity = summonOwner != null
                ? summonOwner
                : (this.getOwner() instanceof LivingEntity livingOwner ? livingOwner : null);
        return allianceCheckEntity == null || !allianceCheckEntity.isAlliedTo(entity);
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