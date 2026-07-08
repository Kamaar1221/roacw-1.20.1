package net.kamaarion.roacw.entity.spells.impaling_column;

import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.util.GeckoLibUtil;

import java.util.UUID;

public class ImpalingColumnShards extends Entity implements GeoEntity {
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    private static final EntityDataAccessor<Integer> SPAWN_DELAY =
            SynchedEntityData.defineId(ImpalingColumnShards.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Float> SCALE =
            SynchedEntityData.defineId(ImpalingColumnShards.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Boolean> TILTED =
            SynchedEntityData.defineId(ImpalingColumnShards.class, EntityDataSerializers.BOOLEAN);

    // Matches ISB IceSpikeEntity timing
    public static final int RISE_TIME = 8;
    public static final int REST_TIME = 30;
    public static final int LOWER_TIME = 20;

    private boolean sentDamage = false;
    private LivingEntity owner;
    private UUID ownerUUID;
    private float damage;

    public ImpalingColumnShards(EntityType<? extends Entity> type, Level level) {
        super(type, level);
        this.noPhysics = true;
    }

    public void setSpawnDelay(int delay) { this.entityData.set(SPAWN_DELAY, delay); }
    public int getSpawnDelay() { return this.entityData.get(SPAWN_DELAY); }
    public void setScale(float scale) { this.entityData.set(SCALE, scale); }
    public float getScale() { return this.entityData.get(SCALE); }
    public void setTilted(boolean tilted) { this.entityData.set(TILTED, tilted); }
    public boolean isTilted() { return this.entityData.get(TILTED); }
    public void setDamage(float damage) { this.damage = damage; }
    public float getDamage() { return this.damage; }

    public void setOwner(LivingEntity owner) {
        this.owner = owner;
        if (owner != null) this.ownerUUID = owner.getUUID();
    }

    public LivingEntity getOwner() {
        if (this.owner == null && this.ownerUUID != null && this.level() instanceof ServerLevel serverLevel) {
            Entity entity = serverLevel.getEntity(this.ownerUUID);
            if (entity instanceof LivingEntity living) this.owner = living;
        }
        return this.owner;
    }

    /**
     * Returns [-1, 0] — -1 = fully underground, 0 = fully risen.
     * Matches ISB IceSpikeEntity.getPositionOffset exactly.
     */
    public float getPositionOffset(float partialTick) {
        float f = this.tickCount + partialTick;
        int waitTime = this.getSpawnDelay();
        if (f < waitTime) {
            return -1;
        } else if (f < waitTime + RISE_TIME) {
            f = (f - waitTime) / RISE_TIME;
            return (Mth.sin(f * Mth.PI) / Mth.PI) + f - 1f;
        } else if (f < waitTime + RISE_TIME + REST_TIME) {
            return 0f;
        } else {
            f = Mth.clamp((f - (waitTime + RISE_TIME + REST_TIME)) / LOWER_TIME, 0, 1) + 1;
            return -((Mth.sin(f * Mth.PI) / Mth.PI) + f - 1f);
        }
    }

    @Override
    public void tick() {
        super.tick();

        int waitTime = this.getSpawnDelay();

        // Hide while waiting
        this.setInvisible(this.tickCount < waitTime);

        if (this.level().isClientSide) {
            // Particles during rise phase
            if (this.tickCount >= waitTime && this.tickCount < waitTime + RISE_TIME) {
                for (int i = 0; i < 8; i++) {
                    double offsetX = (random.nextDouble() - 0.5D) * 1.5D;
                    double offsetZ = (random.nextDouble() - 0.5D) * 1.5D;

                    this.level().addParticle(ParticleTypes.CLOUD,
                            this.getX() + offsetX, this.getY() + 0.1D, this.getZ() + offsetZ,
                            (random.nextDouble() - 0.5D) * 0.12D, 0.10D,
                            (random.nextDouble() - 0.5D) * 0.12D);

                    this.level().addParticle(
                            new BlockParticleOption(ParticleTypes.BLOCK,
                                    this.level().getBlockState(this.blockPosition().below())),
                            this.getX() + offsetX, this.getY(), this.getZ() + offsetZ,
                            (random.nextDouble() - 0.5D) * 0.08D, 0.08D,
                            (random.nextDouble() - 0.5D) * 0.08D);
                }
            }
            return;
        }

        // Fire damage once at start of rise
        if (!this.sentDamage && this.tickCount == waitTime + RISE_TIME) {
            this.dealDamage();
            this.sentDamage = true;
        }

        // Discard after full lifecycle
        if (this.tickCount > waitTime + RISE_TIME + REST_TIME + LOWER_TIME) {
            this.discard();
        }
    }

    private void dealDamage() {
        LivingEntity caster = this.getOwner();
        var source = net.kamaarion.roacw.registeries.ROACWSpellRegistries.IMPALING_COLUMN
                .get().getDamageSource(this, caster);

        for (LivingEntity target : this.level().getEntitiesOfClass(
                LivingEntity.class, this.getBoundingBox().inflate(0.5D, 1.0D, 0.5D))) {
            if (target == caster || !target.isAlive()) continue;
            if (target.invulnerableTime > 10) continue;

            io.redspace.ironsspellbooks.damage.DamageSources.applyDamage(target, this.damage, source);

            target.addEffect(new net.minecraft.world.effect.MobEffectInstance(
                    net.kamaarion.roacw.registeries.ROACWEffectRegistry.ARMOR_CRUNCH.get(),
                    200, 0, false, false, true));
        }
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {}

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() { return cache; }

    @Override
    protected void defineSynchedData() {
        this.entityData.define(SPAWN_DELAY, 0);
        this.entityData.define(SCALE, 1.0f);
        this.entityData.define(TILTED, true);
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
        if (tag.contains("Damage")) this.damage = tag.getFloat("Damage");
        if (tag.contains("Scale")) this.setScale(tag.getFloat("Scale"));
        if (tag.contains("SpawnDelay")) this.setSpawnDelay(tag.getInt("SpawnDelay"));
        if (tag.contains("Tilted")) this.setTilted(tag.getBoolean("Tilted"));
        if (tag.hasUUID("Owner")) this.ownerUUID = tag.getUUID("Owner");
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
        tag.putFloat("Damage", this.damage);
        tag.putFloat("Scale", this.getScale());
        tag.putInt("SpawnDelay", this.getSpawnDelay());
        tag.putBoolean("Tilted", this.isTilted());
        if (this.ownerUUID != null) tag.putUUID("Owner", this.ownerUUID);
    }
}