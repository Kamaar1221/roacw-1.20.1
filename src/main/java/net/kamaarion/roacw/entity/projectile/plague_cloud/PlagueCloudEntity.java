package net.kamaarion.roacw.entity.projectile.plague_cloud;

import net.kamaarion.roacw.Utils;
import net.kamaarion.roacw.particle.PlagueParticleHelper;
import net.kamaarion.roacw.registeries.ROACWEffectRegistry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;

import java.util.List;

public class PlagueCloudEntity extends Entity {

    private static final EntityDataAccessor<Float> DATA_RADIUS =
            SynchedEntityData.defineId(PlagueCloudEntity.class, EntityDataSerializers.FLOAT);

    private static final int DAMAGE_INTERVAL = 20;    // Once per second

    // Damage is now an instance field (default matches the old constant),
    // so rocket/nuke bursts that never call setDamage(...) behave
    // identically to before. PestilenceCloakSpell overrides this to a
    // separate, lower value.
    private float damage = 6.0F;

    // Lifetime stays a plain field - only read server-side (see tick()),
    // so it never needs to be networked to clients. Radius drives
    // client-side particle spawning, so it's synched via DATA_RADIUS above.
    private int lifetime = 100; // 5 seconds

    private int age;

    private LivingEntity owner;

    private boolean burstOnly = false;

    private float burstRadius = 6.0F * 2.0F;
    private double burstHeight = 4.5D;

    private int burstCloudParticles = 80;
    private int burstGreenParticles = 120;
    private int burstRedParticles = 35;

    public PlagueCloudEntity(EntityType<? extends PlagueCloudEntity> type, Level level) {
        super(type, level);
    }

    @Override
    protected void defineSynchedData() {
        this.entityData.define(DATA_RADIUS, 6.0F);
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
        age = tag.getInt("Age");
        burstOnly = tag.getBoolean("BurstOnly");
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
        tag.putInt("Age", age);
        tag.putBoolean("BurstOnly", burstOnly);
    }

    @Override
    public void tick() {
        super.tick();

        age++;

        if (level().isClientSide) {

            if (burstOnly) {

                PlagueParticleHelper.spawnCloud(
                        level(),
                        getX(),
                        getY(),
                        getZ(),
                        burstRadius,
                        burstHeight,
                        burstCloudParticles,
                        burstGreenParticles,
                        burstRedParticles
                );

                discard();
                return;
            }

            // Normal lingering plague cloud - reads the synched radius,
            // so client-spawned particles actually reflect setRadius(...)
            // called server-side.
            PlagueParticleHelper.spawnCloud(
                    level(),
                    getX(),
                    getY(),
                    getZ(),
                    getRadius(),
                    3.5D,
                    40,
                    20,
                    5
            );

            return;
        }

        if (age % DAMAGE_INTERVAL == 0) {
            damageEntities();
        }

        if (age >= lifetime) {
            discard();
        }
    }

    protected void damageEntities() {

        AABB area = getBoundingBox().inflate(getRadius());
        List<LivingEntity> entities = level().getEntitiesOfClass(LivingEntity.class, area);
        LivingEntity owner = getOwner();

        int amplifier = (owner != null) ? Utils.getNatureScaledPlagueAmplifier(owner) : 0;

        for (LivingEntity entity : entities) {

            if (entity == owner)
                continue;

            if (owner != null && owner.isAlliedTo(entity))
                continue;

            entity.hurt(getDamageSource(), damage);

            entity.addEffect(
                    new MobEffectInstance(
                            ROACWEffectRegistry.PLAGUE.get(),
                            200, amplifier, false, false, true
                    )
            );
        }
    }

    /** Damage source used by the cloud's DOT. Override to change per-subclass. */
    protected DamageSource getDamageSource() {
        return damageSources().generic();
    }

    public void setOwner(LivingEntity owner) {
        this.owner = owner;
    }

    public void setBurstOnly(boolean burstOnly) {
        this.burstOnly = burstOnly;
    }

    /** Overrides the default cloud radius (affects damage area, particle spread). Synched to clients. */
    public void setRadius(float radius) {
        this.entityData.set(DATA_RADIUS, radius);
    }

    public float getRadius() {
        return this.entityData.get(DATA_RADIUS);
    }

    /** Overrides the default cloud lifetime in ticks. Server-only, no client dependency, so not synched. */
    public void setLifetime(int lifetime) {
        this.lifetime = lifetime;
    }

    protected int getLifetime() {
        return lifetime;
    }

    protected int getAge() {
        return age;
    }

    /** Overrides the default per-tick cloud damage. Server-only, no client dependency, so not synched. */
    public void setDamage(float damage) {
        this.damage = damage;
    }

    protected float getDamage() {
        return damage;
    }

    public void setBurstSettings(float radius,
                                 double height,
                                 int cloudParticles,
                                 int greenParticles,
                                 int redParticles) {

        this.burstRadius = radius;
        this.burstHeight = height;

        this.burstCloudParticles = cloudParticles;
        this.burstGreenParticles = greenParticles;
        this.burstRedParticles = redParticles;
    }

    public LivingEntity getOwner() {
        return owner;
    }

    @Override
    public boolean isPickable() {
        return false;
    }

    @Override
    public boolean isNoGravity() {
        return true;
    }
}