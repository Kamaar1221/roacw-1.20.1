package net.kamaarion.roacw.entity.spells.earthly_virtue;

import io.redspace.ironsspellbooks.registries.ParticleRegistry;
import net.minecraft.core.particles.DustParticleOptions;
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
import org.joml.Vector3f;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.util.GeckoLibUtil;

import java.util.UUID;

public class EarthlyVirtueShards extends Entity implements GeoEntity {

    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    // Synced wait time so renderer knows when to start rising
    private static final EntityDataAccessor<Integer> WAIT_TIME =
            SynchedEntityData.defineId(EarthlyVirtueShards.class, EntityDataSerializers.INT);

    // ISB-style timing — smooth sine curve rise and retract
    public static final int RISE_TIME = 10;
    public static final int REST_TIME = 370; // long hold for the AoE duration
    public static final int LOWER_TIME = 20;

    private LivingEntity owner;
    private UUID ownerUUID;

    public EarthlyVirtueShards(EntityType<? extends Entity> type, Level level) {
        super(type, level);
        this.noPhysics = true;
    }

    public int getWaitTime() { return this.entityData.get(WAIT_TIME); }
    public void setWaitTime(int i) { this.entityData.set(WAIT_TIME, i); }

    /**
     * Returns [-1, 0] — -1 = fully underground, 0 = fully risen.
     * Same sine curve math as ISB IceSpikeEntity.
     */
    public float getPositionOffset(float partialTick) {
        float f = this.tickCount + partialTick;
        int waitTime = this.getWaitTime();
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

    public void setOwner(LivingEntity owner) {
        this.owner = owner;

        if (owner != null) {
            this.ownerUUID = owner.getUUID();
        }
    }

    public LivingEntity getOwner() {
        if (this.owner == null
                && this.ownerUUID != null
                && this.level() instanceof ServerLevel serverLevel) {

            Entity entity = serverLevel.getEntity(this.ownerUUID);

            if (entity instanceof LivingEntity living) {
                this.owner = living;
            }
        }

        return this.owner;
    }

    @Override
    public void tick() {
        super.tick();

        int waitTime = this.getWaitTime();

        if (this.level().isClientSide) {
            // Earth burst while emerging
            if (this.tickCount >= waitTime && this.tickCount < waitTime + RISE_TIME) {
                for (int i = 0; i < 8; i++) {
                    double offsetX = (random.nextDouble() - 0.5D) * 1.8D;
                    double offsetZ = (random.nextDouble() - 0.5D) * 1.8D;
                    this.level().addParticle(ParticleTypes.CLOUD,
                            this.getX() + offsetX, this.getY() + 0.1D, this.getZ() + offsetZ,
                            (random.nextDouble() - 0.5D) * 0.08D,
                            0.08D + random.nextDouble() * 0.05D,
                            (random.nextDouble() - 0.5D) * 0.08D);
                }
            }

            // Holy aura after emerging — stop during retract
            int restEnd = waitTime + RISE_TIME + REST_TIME;
            if (this.tickCount >= waitTime + RISE_TIME && this.tickCount < restEnd) {
                if (random.nextInt(4) == 0) {
                    this.level().addParticle(ParticleRegistry.CLEANSE_PARTICLE.get(),
                            getX() + (random.nextDouble() - 0.5D) * 1.5D,
                            getY() + 0.5D + random.nextDouble(),
                            getZ() + (random.nextDouble() - 0.5D) * 1.5D,
                            0.0D, 0.03D, 0.0D);
                }
                if (random.nextInt(10) == 0) {
                    this.level().addParticle(new DustParticleOptions(new Vector3f(1.0F, 0.9F, 0.4F), 1.0F),
                            getX() + (random.nextDouble() - 0.5D) * 1.5D,
                            getY() + 1.0D,
                            getZ() + (random.nextDouble() - 0.5D) * 1.5D,
                            0.0D, 0.03D, 0.0D);
                }
            }
            return;
        }

        // Discard after full lifecycle
        if (this.tickCount > waitTime + RISE_TIME + REST_TIME + LOWER_TIME) {
            this.discard();
        }
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return cache;
    }

    @Override
    protected void defineSynchedData() {
        this.entityData.define(WAIT_TIME, 0);
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
        if (tag.hasUUID("Owner")) this.ownerUUID = tag.getUUID("Owner");
        if (tag.contains("WaitTime")) this.setWaitTime(tag.getInt("WaitTime"));
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
        if (this.ownerUUID != null) tag.putUUID("Owner", this.ownerUUID);
        tag.putInt("WaitTime", this.getWaitTime());
    }
}