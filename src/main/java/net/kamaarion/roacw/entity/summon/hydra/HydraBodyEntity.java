package net.kamaarion.roacw.entity.summon.hydra;

import io.redspace.ironsspellbooks.entity.mobs.IMagicSummon;
import net.kamaarion.roacw.registeries.ROACWEntityRegistry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.phys.Vec3;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.AnimationState;
import software.bernie.geckolib.core.animation.RawAnimation;
import software.bernie.geckolib.core.object.PlayState;
import software.bernie.geckolib.util.GeckoLibUtil;

import java.util.ArrayList;
import java.util.List;

public class HydraBodyEntity extends Monster implements GeoEntity, IMagicSummon {

    public static final float CHAIN_LENGTH = 7.5f;
    public static final int MAX_HEADS = 12;

    private static final EntityDataAccessor<Integer> DATA_LIVING_HEADS =
            SynchedEntityData.defineId(HydraBodyEntity.class, EntityDataSerializers.INT);

    private static final RawAnimation IDLE = RawAnimation.begin().thenLoop("idle");
    private static final RawAnimation WALK = RawAnimation.begin().thenLoop("walk");

    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    private int initialHeadCount = 1;
    private boolean hasSpawnedHeads = false;

    private List<HydraHead> headCache = new ArrayList<>();
    private int headCacheTick = -1;

    public HydraBodyEntity(EntityType<? extends Monster> type, Level level) {
        super(type, level);
        this.noCulling = true;
        this.xpReward = 30;
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 100.0)
                .add(Attributes.ATTACK_DAMAGE, 8.0)
                .add(Attributes.MOVEMENT_SPEED, 0.22)
                .add(Attributes.ARMOR, 6.0)
                .add(Attributes.KNOCKBACK_RESISTANCE, 0.8)
                .add(Attributes.FOLLOW_RANGE, 32.0);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(DATA_LIVING_HEADS, 0);
    }

    public void setInitialHeadCount(int count) {
        this.initialHeadCount = Math.max(0, Math.min(MAX_HEADS, count));
    }

    public int getInitialHeadCount() {
        return initialHeadCount;
    }

    public int getLivingHeadCount() {
        return this.entityData.get(DATA_LIVING_HEADS);
    }

    public List<HydraHead> getHeads() {
        if (headCacheTick != this.tickCount) {
            headCacheTick = this.tickCount;
            headCache = level().getEntitiesOfClass(
                    HydraHead.class,
                    getBoundingBox().inflate(CHAIN_LENGTH * 2.0 + 4.0),
                    head -> head.isAlive() && head.getBodyId() == this.getId());
        }
        return headCache;
    }

    private void spawnHeads() {
        if (!(level() instanceof ServerLevelAccessor serverLevel)) {
            return;
        }
        for (int i = 0; i < initialHeadCount; i++) {
            HydraHead head = ROACWEntityRegistry.HYDRA_HEAD.get().create(level());
            if (head == null) {
                continue;
            }
            double angle = (i / (double) Math.max(1, initialHeadCount)) * Math.PI * 2.0;
            head.moveTo(
                    getX() + Math.cos(angle) * 2.5,
                    getY() + getBbHeight() * 0.9,
                    getZ() + Math.sin(angle) * 2.5,
                    (float) (angle * Mth.RAD_TO_DEG), 0f);
            head.setBody(this);
            head.setHeadIndex(i);
            head.finalizeSpawn(serverLevel, level().getCurrentDifficultyAt(blockPosition()),
                    MobSpawnType.MOB_SUMMONED, null, null);
            level().addFreshEntity(head);
        }
        this.entityData.set(DATA_LIVING_HEADS, initialHeadCount);
    }

    public Vec3 getChainAnchor(float partialTick) {
        return getPosition(partialTick).add(0, getBbHeight() * 0.85, 0);
    }

    @Override
    public void tick() {
        super.tick();
        if (!level().isClientSide) {
            if (!hasSpawnedHeads) {
                hasSpawnedHeads = true;
                spawnHeads();
            } else if (this.tickCount % 10 == 0) {
                this.entityData.set(DATA_LIVING_HEADS, getHeads().size());
            }
        }
    }

    @Override
    public void die(DamageSource source) {
        if (!level().isClientSide) {
            for (HydraHead head : getHeads()) {
                head.onChainSevered();
            }
        }
        super.die(source);
    }

    @Override
    public boolean canAttack(LivingEntity target) {
        return !(target instanceof HydraHead head && head.getBodyId() == this.getId())
                && super.canAttack(target);
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putInt("InitialHeadCount", initialHeadCount);
        tag.putBoolean("SpawnedHeads", hasSpawnedHeads);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        if (tag.contains("InitialHeadCount")) {
            initialHeadCount = tag.getInt("InitialHeadCount");
        }
        hasSpawnedHeads = tag.getBoolean("SpawnedHeads");
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "body_controller", 4, this::bodyPredicate));
    }

    private PlayState bodyPredicate(AnimationState<HydraBodyEntity> state) {
        if (state.isMoving()) {
            return state.setAndContinue(WALK);
        }
        return state.setAndContinue(IDLE);
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return cache;
    }

    @Override
    public void onUnSummon() {
        if(!(this.level().isClientSide)){
            for (HydraHead head : getHeads()) {
                head.onChainSevered();
            }
            this.discard();
        }
    }
}