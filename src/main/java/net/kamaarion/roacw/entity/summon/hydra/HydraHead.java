package net.kamaarion.roacw.entity.summon.hydra;

import io.redspace.ironsspellbooks.capabilities.magic.MagicManager;
import io.redspace.ironsspellbooks.util.ParticleHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.control.MoveControl;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.target.TargetGoal;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.AnimationState;
import software.bernie.geckolib.core.animation.RawAnimation;
import software.bernie.geckolib.core.object.PlayState;
import software.bernie.geckolib.util.GeckoLibUtil;

import javax.annotation.Nullable;
import java.util.EnumSet;
import java.util.UUID;

public class HydraHead extends Monster implements GeoEntity {

    private static final EntityDataAccessor<Integer> DATA_BODY_ID =
            SynchedEntityData.defineId(HydraHead.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Boolean> DATA_CHARGING =
            SynchedEntityData.defineId(HydraHead.class, EntityDataSerializers.BOOLEAN);

    private static final RawAnimation IDLE = RawAnimation.begin().thenLoop("idle");

    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    @Nullable
    private UUID bodyUUID;
    @Nullable
    private HydraBodyEntity cachedBody;
    private int headIndex;

    public HydraHead(EntityType<? extends Monster> type, Level level) {
        super(type, level);
        this.moveControl = new HydraHeadMoveControl(this);
        this.setNoGravity(true);
        this.setPersistenceRequired();
        this.xpReward = 3;
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 16.0)
                .add(Attributes.ATTACK_DAMAGE, 5.0)
                .add(Attributes.MOVEMENT_SPEED, 0.4)
                .add(Attributes.FOLLOW_RANGE, 24.0);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(8, new HydraHeadOrbitGoal());
        this.goalSelector.addGoal(9, new LookAtPlayerGoal(this, Player.class, 8.0f));

        this.targetSelector.addGoal(2, new CopyBodyTargetGoal(this));
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(DATA_BODY_ID, -1);
        this.entityData.define(DATA_CHARGING, false);
    }

    public void setBody(@Nullable HydraBodyEntity body) {
        this.cachedBody = body;
        this.bodyUUID = body == null ? null : body.getUUID();
        this.entityData.set(DATA_BODY_ID, body == null ? -1 : body.getId());
    }

    public int getBodyId() {
        return this.entityData.get(DATA_BODY_ID);
    }

    @Nullable
    public HydraBodyEntity getBody() {
        if (cachedBody != null && !cachedBody.isRemoved()) {
            return cachedBody;
        }
        if (bodyUUID != null && level() instanceof ServerLevel serverLevel) {
            if (serverLevel.getEntity(bodyUUID) instanceof HydraBodyEntity body) {
                cachedBody = body;
                return body;
            }
        }
        if (level().isClientSide) {
            int id = getBodyId();
            if (id != -1 && level().getEntity(id) instanceof HydraBodyEntity body) {
                cachedBody = body;
                return body;
            }
        }
        return null;
    }

    public void setHeadIndex(int index) {
        this.headIndex = index;
    }

    public int getHeadIndex() {
        return headIndex;
    }

    @Override
    public void tick() {
        this.noPhysics = true;
        super.tick();
        this.noPhysics = false;
        this.setNoGravity(true);

        if (!level().isClientSide) {
            HydraBodyEntity body = getBody();
            if (body == null || !body.isAlive()) {
                if (this.tickCount > 20) {
                    onChainSevered();
                }
                return;
            }
            this.entityData.set(DATA_BODY_ID, body.getId());
            tetherTo(body);
        }
    }

    private void tetherTo(HydraBodyEntity body) {
        Vec3 toBody = body.getChainAnchor(1f).subtract(position());
        double distance = toBody.length();
        if (distance > HydraBodyEntity.CHAIN_LENGTH) {
            double excess = distance - HydraBodyEntity.CHAIN_LENGTH;
            Vec3 pull = toBody.normalize().scale(Math.min(excess * 0.1, 0.5));
            setDeltaMovement(getDeltaMovement().add(pull));
            this.hurtMarked = true;
        }
    }

    public void onChainSevered() {
        if (!level().isClientSide) {
            MagicManager.spawnParticles(level(), ParticleHelper.ENDER_SPARKS,
                    getX(), getY() + getBbHeight() * 0.5, getZ(), 12, 0.1, 0.1, 0.1, 0.15, false);
            playSound(SoundEvents.CHAIN_BREAK, 1f, 0.8f);
            this.discard();
        }
    }

    @Override
    public boolean canAttack(LivingEntity target) {
        if (target instanceof HydraHead other && other.getBodyId() == this.getBodyId()) {
            return false;
        }
        if (target instanceof HydraBodyEntity body && body.getId() == this.getBodyId()) {
            return false;
        }
        return super.canAttack(target);
    }

    @Override
    public boolean isAlliedTo(Entity entity) {
        if (super.isAlliedTo(entity)) {
            return true;
        }
        int bodyId = getBodyId();
        return (entity instanceof HydraBodyEntity body && body.getId() == bodyId)
                || (entity instanceof HydraHead head && head.getBodyId() == bodyId);
    }

    @Override
    public boolean isPushable() {
        return false;
    }

    @Override
    protected void doPush(Entity entity) {
    }

    @Override
    protected void pushEntities() {
    }

    @Override
    public boolean causeFallDamage(float distance, float multiplier, net.minecraft.world.damagesource.DamageSource source) {
        return false;
    }

    @Override
    protected void checkFallDamage(double y, boolean onGround, net.minecraft.world.level.block.state.BlockState state, net.minecraft.core.BlockPos pos) {
    }

    @Override
    public MobType getMobType() {
        return MobType.UNDEFINED;
    }

    @Override
    public boolean removeWhenFarAway(double distance) {
        return false;
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        if (bodyUUID != null) {
            tag.putUUID("Body", bodyUUID);
        }
        tag.putInt("HeadIndex", headIndex);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        if (tag.hasUUID("Body")) {
            bodyUUID = tag.getUUID("Body");
            cachedBody = null;
        }
        headIndex = tag.getInt("HeadIndex");
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "head_controller", 2, this::headPredicate));
    }

    private PlayState headPredicate(AnimationState<HydraHead> state) {
        return state.setAndContinue(IDLE);
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return cache;
    }

    static class HydraHeadMoveControl extends MoveControl {
        public HydraHeadMoveControl(HydraHead head) {
            super(head);
        }

        @Override
        public void tick() {
            if (this.operation != Operation.MOVE_TO) {
                return;
            }
            Vec3 delta = new Vec3(this.wantedX - mob.getX(), this.wantedY - mob.getY(), this.wantedZ - mob.getZ());
            double distance = delta.length();
            if (distance < mob.getBoundingBox().getSize()) {
                this.operation = Operation.WAIT;
                mob.setDeltaMovement(mob.getDeltaMovement().scale(0.5));
            } else {
                mob.setDeltaMovement(mob.getDeltaMovement().add(delta.scale(this.speedModifier * 0.05 / distance)));
                LivingEntity target = mob.getTarget();
                if (target == null) {
                    Vec3 movement = mob.getDeltaMovement();
                    mob.setYRot(-((float) Mth.atan2(movement.x, movement.z)) * Mth.RAD_TO_DEG);
                } else {
                    double dx = target.getX() - mob.getX();
                    double dz = target.getZ() - mob.getZ();
                    mob.setYRot(-((float) Mth.atan2(dx, dz)) * Mth.RAD_TO_DEG);
                }
                mob.yBodyRot = mob.getYRot();
            }
        }
    }

    class HydraHeadOrbitGoal extends Goal {
        HydraHeadOrbitGoal() {
            this.setFlags(EnumSet.of(Goal.Flag.MOVE));
        }

        @Override
        public boolean canUse() {
            return !getMoveControl().hasWanted() && random.nextInt(reducedTickDelay(7)) == 0;
        }

        @Override
        public boolean canContinueToUse() {
            return false;
        }

        @Override
        public void tick() {
            HydraBodyEntity body = getBody();
            Vec3 center = body != null ? body.position() : position();
            int siblings = body != null ? Math.max(1, body.getLivingHeadCount()) : 1;

            double phase = (headIndex / (double) siblings) * Math.PI * 2.0;
            double drift = (tickCount * 0.015) + random.nextGaussian() * 0.35;
            double angle = phase + drift;
            double radius = 2.0 + random.nextDouble() * 2.5;
            double height = (body != null ? body.getBbHeight() : 1.5) * 0.7 + random.nextDouble() * 1.8;

            Vec3 wanted = center.add(Math.cos(angle) * radius, height, Math.sin(angle) * radius);
            getMoveControl().setWantedPosition(wanted.x, wanted.y, wanted.z, 0.6);
        }
    }

    static class CopyBodyTargetGoal extends TargetGoal {
        private final HydraHead head;
        private final TargetingConditions conditions = TargetingConditions.forCombat().ignoreLineOfSight();

        CopyBodyTargetGoal(HydraHead head) {
            super(head, false);
            this.head = head;
        }

        @Override
        public boolean canUse() {
            HydraBodyEntity body = head.getBody();
            return body != null && body.getTarget() != null && canAttack(body.getTarget(), this.conditions);
        }

        @Override
        public void start() {
            HydraBodyEntity body = head.getBody();
            if (body != null) {
                head.setTarget(body.getTarget());
            }
            super.start();
        }
    }
}
