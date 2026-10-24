package net.kamaarion.roacw.entity.projectile.plague_cloud;

import net.kamaarion.roacw.Utils;
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
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;

import java.util.List;

public class PlagueCloudEntity extends Entity {

    private static final EntityDataAccessor<Float> DATA_RADIUS =
            SynchedEntityData.defineId(
                    PlagueCloudEntity.class,
                    EntityDataSerializers.FLOAT
            );

    private static final int DAMAGE_INTERVAL = 20;

    private float damage = 6.0F;
    private int lifetime = 100;
    private int age;

    private LivingEntity owner;

    private boolean burstOnly;

    private float burstRadius = 6.0F * 2.0F;
    private double burstHeight = 4.5D;

    private int burstCloudParticles = 80;
    private int burstGreenParticles = 120;
    private int burstRedParticles = 35;

    public PlagueCloudEntity(
            EntityType<? extends PlagueCloudEntity> type,
            Level level
    ) {
        super(type, level);
    }

    @Override
    protected void defineSynchedData() {
        this.entityData.define(DATA_RADIUS, 6.0F);
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
        this.age = tag.getInt("Age");
        this.burstOnly = tag.getBoolean("BurstOnly");
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
        tag.putInt("Age", this.age);
        tag.putBoolean("BurstOnly", this.burstOnly);
    }

    @Override
    public void tick() {
        super.tick();

        this.age++;

        if (this.burstOnly) {
            this.discard();
            return;
        }

        if (this.level().isClientSide) {
            return;
        }

        if (this.age % DAMAGE_INTERVAL == 0) {
            this.damageEntities();
        }

        if (this.age >= this.lifetime) {
            this.discard();
        }
    }

    protected void damageEntities() {
        AABB area = this.getBoundingBox().inflate(this.getRadius());

        List<LivingEntity> entities = this.level().getEntitiesOfClass(
                LivingEntity.class,
                area
        );

        LivingEntity owner = this.getOwner();

        int amplifier = owner != null
                ? Utils.getNatureScaledPlagueAmplifier(owner)
                : 0;

        for (LivingEntity entity : entities) {
            if (entity instanceof Player || entity == owner) {
                continue;
            }

            if (!entity.isAlive()) {
                continue;
            }

            if (owner != null && owner.isAlliedTo(entity)) {
                continue;
            }

            entity.hurt(this.getDamageSource(), this.damage);

            entity.addEffect(
                    new MobEffectInstance(
                            ROACWEffectRegistry.PLAGUE.get(),
                            200,
                            amplifier,
                            false,
                            false,
                            true
                    )
            );
        }
    }

    protected DamageSource getDamageSource() {
        return this.damageSources().generic();
    }

    public void setOwner(LivingEntity owner) {
        this.owner = owner;
    }

    public void setBurstOnly(boolean burstOnly) {
        this.burstOnly = burstOnly;
    }

    public void setRadius(float radius) {
        this.entityData.set(DATA_RADIUS, radius);
    }

    public float getRadius() {
        return this.entityData.get(DATA_RADIUS);
    }

    public void setLifetime(int lifetime) {
        this.lifetime = lifetime;
    }

    protected int getLifetime() {
        return this.lifetime;
    }

    protected int getAge() {
        return this.age;
    }

    public void setDamage(float damage) {
        this.damage = damage;
    }

    protected float getDamage() {
        return this.damage;
    }

    public void setBurstSettings(
            float radius,
            double height,
            int cloudParticles,
            int greenParticles,
            int redParticles
    ) {
        this.burstRadius = radius;
        this.burstHeight = height;
        this.burstCloudParticles = cloudParticles;
        this.burstGreenParticles = greenParticles;
        this.burstRedParticles = redParticles;
    }

    public LivingEntity getOwner() {
        return this.owner;
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