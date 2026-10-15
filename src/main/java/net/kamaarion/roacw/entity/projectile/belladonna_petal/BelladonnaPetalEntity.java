package net.kamaarion.roacw.entity.projectile.belladonna_petal;

import io.redspace.ironsspellbooks.api.spells.AbstractSpell;
import io.redspace.ironsspellbooks.damage.DamageSources;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.AbstractHurtingProjectile;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.util.GeckoLibUtil;

import javax.annotation.Nullable;

public class BelladonnaPetalEntity extends AbstractHurtingProjectile implements GeoEntity {

    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    private static final DustParticleOptions BELLADONNA_DUST =
            new DustParticleOptions(new Vector3f(0.58F, 0.29F, 0.72F), 1.0F);

    private static final float BASE_DAMAGE = 5.0F;

    private float damage = BASE_DAMAGE;
    private boolean hasHit;

    @Nullable
    private LivingEntity summonOwner;

    @Nullable
    private AbstractSpell damageSpell;

    public BelladonnaPetalEntity(
            EntityType<? extends AbstractHurtingProjectile> type,
            Level level
    ) {
        super(type, level);
    }

    public BelladonnaPetalEntity(
            EntityType<? extends AbstractHurtingProjectile> type,
            Level level,
            LivingEntity spawner,
            @Nullable LivingEntity owner,
            Vec3 spawnPos,
            Vec3 direction,
            AbstractSpell damageSpell
    ) {
        super(
                type,
                spawnPos.x,
                spawnPos.y,
                spawnPos.z,
                direction.x,
                direction.y,
                direction.z,
                level
        );

        this.setOwner(spawner);
        this.summonOwner = owner;
        this.damageSpell = damageSpell;
        this.setDeltaMovement(direction);
    }

    public void setDamage(float damage) {
        this.damage = damage;
    }

    @Nullable
    public AbstractSpell getDamageSpell() {
        return this.damageSpell;
    }

    @Override
    public void tick() {
        Entity owner = this.getOwner();

        if (this.level().isClientSide
                || ((owner == null || !owner.isRemoved())
                && this.level().hasChunkAt(this.blockPosition()))) {

            super.baseTick();

            if (this.shouldBurn()) {
                this.setSecondsOnFire(1);
            }

            HitResult hitResult = ProjectileUtil.getHitResultOnMoveVector(
                    this,
                    this::canHitEntity
            );

            if (hitResult.getType() != HitResult.Type.MISS
                    && !net.minecraftforge.event.ForgeEventFactory.onProjectileImpact(this, hitResult)) {
                this.onHit(hitResult);
            }

            this.checkInsideBlocks();

            Vec3 motion = this.getDeltaMovement();

            double nextX = this.getX() + motion.x;
            double nextY = this.getY() + motion.y;
            double nextZ = this.getZ() + motion.z;

            ProjectileUtil.rotateTowardsMovement(this, 0.2F);

            float inertia = this.getInertia();

            if (this.isInWater()) {
                for (int i = 0; i < 4; ++i) {
                    this.level().addParticle(
                            ParticleTypes.BUBBLE,
                            nextX - motion.x * 0.25D,
                            nextY - motion.y * 0.25D,
                            nextZ - motion.z * 0.25D,
                            motion.x,
                            motion.y,
                            motion.z
                    );
                }

                inertia = 0.8F;
            }

            this.setDeltaMovement(
                    motion.add(this.xPower, this.yPower, this.zPower)
                            .scale(inertia)
            );

            this.level().addParticle(
                    this.getTrailParticle(),
                    nextX,
                    nextY,
                    nextZ,
                    0.0D,
                    0.0D,
                    0.0D
            );

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

            if (this.damageSpell == null) {
                this.discard();
                return;
            }

            LivingEntity spellOwner = this.summonOwner;

            if (spellOwner == null && this.getOwner() instanceof LivingEntity livingOwner) {
                spellOwner = livingOwner;
            }

            if (spellOwner == null) {
                this.discard();
                return;
            }

            var source = this.damageSpell.getDamageSource(this, spellOwner);

            boolean damaged = DamageSources.applyDamage(
                    living,
                    this.damage,
                    source
            );

            if (damaged) {
                living.addEffect(new MobEffectInstance(MobEffects.POISON, 100, 1));
            }
        }

        if (this.level() instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(
                    ParticleTypes.SPORE_BLOSSOM_AIR,
                    this.getX(),
                    this.getY(0.5D),
                    this.getZ(),
                    10,
                    0.2D,
                    0.2D,
                    0.2D,
                    0.01D
            );
        }

        this.discard();
    }

    private boolean shouldAffect(LivingEntity entity) {
        if (entity == this.getOwner() || entity == this.summonOwner) {
            return false;
        }

        LivingEntity allianceCheckEntity = this.summonOwner != null
                ? this.summonOwner
                : (this.getOwner() instanceof LivingEntity livingOwner
                   ? livingOwner
                   : null);

        return allianceCheckEntity == null || !allianceCheckEntity.isAlliedTo(entity);
    }

    @Override
    protected float getInertia() {
        return 0.92F;
    }

    @Override
    protected ParticleOptions getTrailParticle() {
        return ParticleTypes.GLOW;
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