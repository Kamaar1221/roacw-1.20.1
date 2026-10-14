package net.kamaarion.roacw.entity.summon.endo_hydra;

import io.redspace.ironsspellbooks.capabilities.magic.SummonManager;
import io.redspace.ironsspellbooks.entity.mobs.IMagicSummon;
import net.kamaarion.roacw.entity.projectile.endo_hydra_beam.EndoHydraBeamEntity;
import net.kamaarion.roacw.registeries.ROACWEntityRegistry;
import net.kamaarion.roacw.registeries.ROACWSoundRegistry;
import net.kamaarion.roacw.registeries.ROACWSpellRegistry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.AnimationState;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.core.animation.RawAnimation;
import software.bernie.geckolib.core.object.PlayState;
import software.bernie.geckolib.util.GeckoLibUtil;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class EndoHydraEntity extends PathfinderMob implements GeoEntity, IMagicSummon {

    private static final EntityDataAccessor<Integer> DATA_SPELL_LEVEL =
            SynchedEntityData.defineId(EndoHydraEntity.class, EntityDataSerializers.INT);

    private static final EntityDataAccessor<Float> DATA_SPELL_POWER =
            SynchedEntityData.defineId(EndoHydraEntity.class, EntityDataSerializers.FLOAT);

    private static final EntityDataAccessor<Integer> DATA_ATTACK_TARGET_ID =
            SynchedEntityData.defineId(EndoHydraEntity.class, EntityDataSerializers.INT);

    private static final RawAnimation BODY_IDLE =
            RawAnimation.begin().thenLoop("BodyIdle");

    private static final RawAnimation HEAD_1_ATTACK =
            RawAnimation.begin().thenPlay("Head1Attack");

    private static final RawAnimation HEAD_2_ATTACK =
            RawAnimation.begin().thenPlay("Head2Attack");

    private static final RawAnimation HEAD_3_ATTACK =
            RawAnimation.begin().thenPlay("Head3Attack");

    private static final RawAnimation HEAD_4_ATTACK =
            RawAnimation.begin().thenPlay("Head4Attack");

    private static final RawAnimation HEAD_5_ATTACK =
            RawAnimation.begin().thenPlay("Head5Attack");

    private static final float HEAD_FIRE_ANGLE_TOLERANCE_DEGREES = 3.0F;
    private static final int HEAD_FIRE_MAX_WAIT_TICKS = 10;
    private static final int ATTACK_COOLDOWN = 60;
    private static final int HEAD_ATTACK_ANIMATION_LOCK_TICKS = 30;
    private static final double ATTACK_RANGE = 24.0D;
    private static final float ATTACK_HEAD_MAX_YAW = 180.0F;
    private static final float ATTACK_HEAD_MAX_PITCH = 45.0F;
    private static final float ATTACK_HEAD_TURN_SPEED = 45.0F;
    private static final float ALL_HEADS_ATTACK_CHANCE = 0.5F;
    private static final int INDIVIDUAL_ATTACK_DELAY_MIN = 10;
    private static final int INDIVIDUAL_ATTACK_DELAY_MAX = 20;
    private static final double BODY_BACK_DISTANCE = 1.65D;
    private static final double BODY_VERTICAL_OFFSET = 0.9D;
    private static final double FOLLOW_LERP = 0.35D;
    private static final float HEAD_MAX_YAW = 35.0F;
    private static final float HEAD_MAX_PITCH = 18.0F;
    private static final float HEAD_MAX_TURN_SPEED = 2.0F;
    private static final int HEAD_LOOK_MIN_DELAY = 45;
    private static final int HEAD_LOOK_MAX_DELAY = 120;
    private static final float HEAD_RETURN_TO_IDLE_SPEED = 2.0F;

    public static final String HEAD_1_BONE = "Head1";
    public static final String HEAD_2_BONE = "Head2";
    public static final String HEAD_3_BONE = "Head3";
    public static final String HEAD_4_BONE = "Head4";
    public static final String HEAD_5_BONE = "Head5";

    public static final String HEAD_1_ROOT_BONE = "Neck1-1";
    public static final String HEAD_2_ROOT_BONE = "Neck2-1";
    public static final String HEAD_3_ROOT_BONE = "Neck3-1";
    public static final String HEAD_4_ROOT_BONE = "Neck4-1";
    public static final String HEAD_5_ROOT_BONE = "Neck5-1";

    public static final String MOUTH_1_BONE = "Mouth1";
    public static final String MOUTH_2_BONE = "Mouth2";
    public static final String MOUTH_3_BONE = "Mouth3";
    public static final String MOUTH_4_BONE = "Mouth4";
    public static final String MOUTH_5_BONE = "Mouth5";

    public static final List<String> ALL_HEAD_ROOTS_FOR_RENDERING =
            List.of(
                    HEAD_1_ROOT_BONE,
                    HEAD_2_ROOT_BONE,
                    HEAD_3_ROOT_BONE,
                    HEAD_4_ROOT_BONE,
                    HEAD_5_ROOT_BONE
            );

    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    private final float[] headCurrentYaw = new float[5];
    private final float[] headCurrentPitch = new float[5];
    private final float[] headPreviousYaw = new float[5];
    private final float[] headPreviousPitch = new float[5];
    private final float[] headTargetYaw = new float[5];
    private final float[] headTargetPitch = new float[5];
    private final int[] headLookTimers = new int[5];
    private final int[] individualHeadFireTimers = new int[5];
    private final boolean[] headPendingBeamFire = new boolean[5];
    private final int[] headPendingBeamFireWaitTicks = new int[5];
    private final int[] headAnimationLockTimers = new int[5];

    private final Vec3[] clientMouthPositions = {
            Vec3.ZERO,
            Vec3.ZERO,
            Vec3.ZERO,
            Vec3.ZERO,
            Vec3.ZERO
    };

    private final Vec3[] clientPreviousMouthPositions = {
            Vec3.ZERO,
            Vec3.ZERO,
            Vec3.ZERO,
            Vec3.ZERO,
            Vec3.ZERO
    };

    private boolean fullHeadAttackActive;
    private boolean attackSequenceActive;

    @Nullable
    private LivingEntity attackTarget;

    private int attackCooldownTimer;

    public EndoHydraEntity(EntityType<? extends PathfinderMob> type, Level level) {
        super(type, level);

        this.setPersistenceRequired();
        this.setNoGravity(true);
        this.getNavigation().stop();
        this.noCulling = true;

        for (int i = 0; i < 5; i++) {
            this.headLookTimers[i] = randomLookDelay(i);
            this.individualHeadFireTimers[i] = -1;
            this.headAnimationLockTimers[i] = 0;
            this.headCurrentYaw[i] = 0.0F;
            this.headCurrentPitch[i] = 0.0F;
            this.headPreviousYaw[i] = 0.0F;
            this.headPreviousPitch[i] = 0.0F;
            this.headTargetYaw[i] = 0.0F;
            this.headTargetPitch[i] = 0.0F;
        }
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(DATA_SPELL_LEVEL, 1);
        this.entityData.define(DATA_SPELL_POWER, 15.0F);
        this.entityData.define(DATA_ATTACK_TARGET_ID, -1);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 20.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.0D)
                .add(Attributes.FOLLOW_RANGE, 0.0D);
    }

    @Override
    protected void registerGoals() {
    }

    @Nullable
    public LivingEntity getSummonOwner() {
        var owner = SummonManager.getOwner(this);
        return owner instanceof LivingEntity living ? living : null;
    }

    @Override
    public void onUnSummon() {
        this.discard();
    }

    public void setSpellLevel(int spellLevel) {
        this.entityData.set(DATA_SPELL_LEVEL, Mth.clamp(spellLevel, 1, 3));
    }

    public int getSpellLevel() {
        return this.entityData.get(DATA_SPELL_LEVEL);
    }

    public float getBeamDamage() {
        return getSpellPower();
    }

    public void setSpellPower(float spellPower) {
        this.entityData.set(DATA_SPELL_POWER, Math.max(0.0F, spellPower));
    }

    public float getSpellPower() {
        return this.entityData.get(DATA_SPELL_POWER);
    }

    public List<String> getVisibleHeadRootBones() {
        int level = getSpellLevel();

        if (level <= 1) {
            return List.of(HEAD_1_ROOT_BONE);
        }

        if (level == 2) {
            return List.of(
                    HEAD_1_ROOT_BONE,
                    HEAD_3_ROOT_BONE,
                    HEAD_5_ROOT_BONE
            );
        }

        return ALL_HEAD_ROOTS_FOR_RENDERING;
    }

    public List<String> getHiddenHeadRootBones() {
        List<String> hidden = new ArrayList<>(ALL_HEAD_ROOTS_FOR_RENDERING);
        hidden.removeAll(getVisibleHeadRootBones());
        return hidden;
    }

    public boolean isHeadActive(int headIndex) {
        if (headIndex < 0 || headIndex >= 5) {
            return false;
        }

        String root = switch (headIndex) {
            case 0 -> HEAD_1_ROOT_BONE;
            case 1 -> HEAD_2_ROOT_BONE;
            case 2 -> HEAD_3_ROOT_BONE;
            case 3 -> HEAD_4_ROOT_BONE;
            case 4 -> HEAD_5_ROOT_BONE;
            default -> "";
        };

        return getVisibleHeadRootBones().contains(root);
    }

    private int randomLookDelay(int headIndex) {
        int base = HEAD_LOOK_MIN_DELAY
                + this.random.nextInt(HEAD_LOOK_MAX_DELAY - HEAD_LOOK_MIN_DELAY + 1);

        return base + headIndex * 7;
    }

    private int randomLookDelay() {
        return HEAD_LOOK_MIN_DELAY
                + this.random.nextInt(HEAD_LOOK_MAX_DELAY - HEAD_LOOK_MIN_DELAY + 1);
    }

    private float randomFloat(float min, float max) {
        return min + this.random.nextFloat() * (max - min);
    }

    private void chooseNewHeadLookTarget(int headIndex) {
        headTargetYaw[headIndex] = randomFloat(-HEAD_MAX_YAW, HEAD_MAX_YAW);
        headTargetPitch[headIndex] = randomFloat(-HEAD_MAX_PITCH, HEAD_MAX_PITCH);
        headLookTimers[headIndex] = randomLookDelay();
    }

    private float[] calculateTargetRotation(int headIndex, LivingEntity target) {
        Vec3 headPosition = getApproximateHeadWorldPosition(headIndex);
        Vec3 direction = target.getEyePosition().subtract(headPosition);

        if (direction.lengthSqr() < 0.000001D) {
            return new float[]{0.0F, 0.0F};
        }

        double horizontal = Math.sqrt(direction.x * direction.x + direction.z * direction.z);

        float worldYaw = (float) Math.toDegrees(Math.atan2(-direction.x, direction.z));
        float worldPitch = (float) Math.toDegrees(Math.atan2(direction.y, horizontal));
        float localYaw = wrapDegrees(worldYaw - this.getYRot());

        return new float[]{localYaw, worldPitch};
    }

    private Vec3 getApproximateHeadWorldPosition(int headIndex) {
        double x = 0.0D;
        double y = headIndex == 0 ? 1.95D : 3.45D;
        double z = headIndex == 0 ? 0.15D : -0.09D;

        float yaw = this.getYRot() * Mth.DEG_TO_RAD;
        double cos = Math.cos(yaw);
        double sin = Math.sin(yaw);

        return this.position().add(
                x * cos - z * sin,
                y,
                x * sin + z * cos
        );
    }

    private void updateHeadLooking() {
        int targetId = this.entityData.get(DATA_ATTACK_TARGET_ID);
        LivingEntity target = null;

        if (targetId >= 0) {
            Entity resolved = this.level().getEntity(targetId);

            if (resolved instanceof LivingEntity living && living.isAlive()) {
                target = living;
            }
        }

        for (int i = 0; i < 5; i++) {
            headPreviousYaw[i] = headCurrentYaw[i];
            headPreviousPitch[i] = headCurrentPitch[i];

            if (!isHeadActive(i)) {
                headTargetYaw[i] = 0.0F;
                headTargetPitch[i] = 0.0F;

                headCurrentYaw[i] = approachAngle(
                        headCurrentYaw[i],
                        0.0F,
                        HEAD_RETURN_TO_IDLE_SPEED
                );

                headCurrentPitch[i] = approachAngle(
                        headCurrentPitch[i],
                        0.0F,
                        HEAD_RETURN_TO_IDLE_SPEED
                );

                continue;
            }

            if (target != null) {
                float[] rotation = calculateTargetRotation(i, target);

                headTargetYaw[i] = Mth.clamp(
                        rotation[0],
                        -ATTACK_HEAD_MAX_YAW,
                        ATTACK_HEAD_MAX_YAW
                );

                headTargetPitch[i] = Mth.clamp(
                        rotation[1],
                        -ATTACK_HEAD_MAX_PITCH,
                        ATTACK_HEAD_MAX_PITCH
                );

                headCurrentYaw[i] = approachAngle(
                        headCurrentYaw[i],
                        headTargetYaw[i],
                        ATTACK_HEAD_TURN_SPEED
                );

                headCurrentPitch[i] = approachAngle(
                        headCurrentPitch[i],
                        headTargetPitch[i],
                        ATTACK_HEAD_TURN_SPEED
                );

                continue;
            }

            if (--headLookTimers[i] <= 0) {
                chooseNewHeadLookTarget(i);
            }

            headCurrentYaw[i] = approachAngle(
                    headCurrentYaw[i],
                    headTargetYaw[i],
                    HEAD_MAX_TURN_SPEED
            );

            headCurrentPitch[i] = approachAngle(
                    headCurrentPitch[i],
                    headTargetPitch[i],
                    HEAD_MAX_TURN_SPEED * 0.65F
            );
        }
    }

    private float approachAngle(float current, float target, float maxStep) {
        float difference = wrapDegrees(target - current);

        if (Math.abs(difference) <= maxStep) {
            return target;
        }

        return current + Math.copySign(maxStep, difference);
    }

    private float wrapDegrees(float angle) {
        angle %= 360.0F;

        if (angle >= 180.0F) {
            angle -= 360.0F;
        }

        if (angle < -180.0F) {
            angle += 360.0F;
        }

        return angle;
    }

    public float getHeadLookYaw(int headIndex) {
        if (headIndex < 0 || headIndex >= 5) {
            return 0.0F;
        }

        return headCurrentYaw[headIndex];
    }

    public float getHeadPreviousYaw(int headIndex) {
        if (headIndex < 0 || headIndex >= 5) {
            return 0.0F;
        }

        return headPreviousYaw[headIndex];
    }

    public float getHeadLookPitch(int headIndex) {
        if (headIndex < 0 || headIndex >= 5) {
            return 0.0F;
        }

        return headCurrentPitch[headIndex];
    }

    public float getHeadPreviousPitch(int headIndex) {
        if (headIndex < 0 || headIndex >= 5) {
            return 0.0F;
        }

        return headPreviousPitch[headIndex];
    }

    private boolean isHeadAnimationReady(int headIndex) {
        if (headIndex < 0 || headIndex >= 5) {
            return false;
        }

        return headAnimationLockTimers[headIndex] <= 0;
    }

    private void lockHeadAnimation(int headIndex) {
        if (headIndex < 0 || headIndex >= 5) {
            return;
        }

        headAnimationLockTimers[headIndex] = HEAD_ATTACK_ANIMATION_LOCK_TICKS;
    }

    private void updateHeadAnimationLocks() {
        for (int i = 0; i < 5; i++) {
            if (headAnimationLockTimers[i] > 0) {
                headAnimationLockTimers[i]--;
            }
        }
    }

    private boolean allActiveHeadsAnimationReady() {
        for (int i = 0; i < 5; i++) {
            if (!isHeadActive(i)) {
                continue;
            }

            if (!isHeadAnimationReady(i)) {
                return false;
            }
        }

        return true;
    }

    public void setClientMouthPosition(int headIndex, Vec3 position) {
        if (headIndex < 0 || headIndex >= 5 || position == null) {
            return;
        }

        clientPreviousMouthPositions[headIndex] = clientMouthPositions[headIndex];
        clientMouthPositions[headIndex] = position;
    }

    public Vec3 getClientMouthPosition(int headIndex) {
        if (headIndex < 0 || headIndex >= 5) {
            return Vec3.ZERO;
        }

        return clientMouthPositions[headIndex];
    }

    public Vec3 getPreviousClientMouthPosition(int headIndex) {
        if (headIndex < 0 || headIndex >= 5) {
            return Vec3.ZERO;
        }

        return clientPreviousMouthPositions[headIndex];
    }

    public Vec3 getInterpolatedClientMouthPosition(int headIndex, float partialTick) {
        if (headIndex < 0 || headIndex >= 5) {
            return Vec3.ZERO;
        }

        return clientPreviousMouthPositions[headIndex].lerp(
                clientMouthPositions[headIndex],
                partialTick
        );
    }

    public Vec3 getMouthWorldPosition(int headIndex) {
        return getClientMouthPosition(headIndex);
    }

    private void setAttackTarget(LivingEntity target) {
        this.attackTarget = target;
        this.entityData.set(DATA_ATTACK_TARGET_ID, target.getId());
    }

    private void clearAttackTarget() {
        this.attackTarget = null;
        this.entityData.set(DATA_ATTACK_TARGET_ID, -1);
        this.attackSequenceActive = false;
        this.fullHeadAttackActive = false;

        for (int i = 0; i < 5; i++) {
            individualHeadFireTimers[i] = -1;
            headPendingBeamFire[i] = false;
            headPendingBeamFireWaitTicks[i] = 0;
        }
    }

    private void startFireCycle() {
        if (attackTarget == null || !attackTarget.isAlive() || attackSequenceActive) {
            return;
        }

        if (!allActiveHeadsAnimationReady()) {
            return;
        }

        attackSequenceActive = true;
        fullHeadAttackActive = false;

        for (int i = 0; i < 5; i++) {
            individualHeadFireTimers[i] = -1;
        }

        if (this.random.nextFloat() < ALL_HEADS_ATTACK_CHANCE) {
            fullHeadAttackActive = true;

            for (int i = 0; i < 5; i++) {
                if (isHeadActive(i)) {
                    individualHeadFireTimers[i] = 0;
                }
            }
        } else {
            int delay = 0;

            for (int i = 0; i < 5; i++) {
                if (!isHeadActive(i)) {
                    continue;
                }

                individualHeadFireTimers[i] = delay;
                delay += INDIVIDUAL_ATTACK_DELAY_MIN
                        + this.random.nextInt(
                        INDIVIDUAL_ATTACK_DELAY_MAX - INDIVIDUAL_ATTACK_DELAY_MIN + 1
                );
            }
        }

        attackCooldownTimer = ATTACK_COOLDOWN;
    }

    public void beginHeadAttack(LivingEntity target) {
        if (target == null || !target.isAlive()) {
            return;
        }

        if (target.distanceToSqr(this) > ATTACK_RANGE * ATTACK_RANGE) {
            return;
        }

        setAttackTarget(target);
        startFireCycle();
    }

    private boolean canHeadReachTarget(int headIndex, LivingEntity target) {
        float[] rotation = calculateTargetRotation(headIndex, target);

        return Math.abs(rotation[0]) <= ATTACK_HEAD_MAX_YAW
                && Math.abs(rotation[1]) <= ATTACK_HEAD_MAX_PITCH;
    }

    private void updateAutoAttack(LivingEntity owner) {
        if (attackTarget != null) {
            if (!attackTarget.isAlive()
                    || attackTarget.distanceToSqr(this) > ATTACK_RANGE * ATTACK_RANGE) {
                if (!attackSequenceActive) {
                    clearAttackTarget();
                }
            }
        }

        if (attackTarget == null) {
            LivingEntity lastHurt = owner.getLastHurtMob();

            if (lastHurt != null
                    && lastHurt.isAlive()
                    && lastHurt.distanceToSqr(this) <= ATTACK_RANGE * ATTACK_RANGE) {
                setAttackTarget(lastHurt);
            }
        }

        if (attackTarget == null) {
            return;
        }

        if (attackCooldownTimer > 0) {
            attackCooldownTimer--;
        }

        if (attackSequenceActive || attackCooldownTimer > 0) {
            return;
        }

        if (!allActiveHeadsAnimationReady()) {
            return;
        }

        boolean canReach = false;

        for (int i = 0; i < 5; i++) {
            if (isHeadActive(i) && canHeadReachTarget(i, attackTarget)) {
                canReach = true;
                break;
            }
        }

        if (canReach) {
            startFireCycle();
        }
    }

    private void updateIndividualHeadAttacks() {
        if (!attackSequenceActive) {
            return;
        }

        List<Integer> readyThisTick = new ArrayList<>();

        for (int i = 0; i < 5; i++) {
            int timer = individualHeadFireTimers[i];

            if (timer < 0) {
                continue;
            }

            if (timer > 0) {
                individualHeadFireTimers[i] = timer - 1;
                continue;
            }

            readyThisTick.add(i);
        }

        Collections.shuffle(readyThisTick, new java.util.Random(this.random.nextLong()));

        for (int headIndex : readyThisTick) {
            if (!isHeadAnimationReady(headIndex)) {
                individualHeadFireTimers[headIndex] = -1;
                continue;
            }

            triggerHeadAttack(headIndex);
            lockHeadAnimation(headIndex);
            headPendingBeamFire[headIndex] = true;
            headPendingBeamFireWaitTicks[headIndex] = 0;
            individualHeadFireTimers[headIndex] = -1;
        }

        for (int i = 0; i < 5; i++) {
            if (!headPendingBeamFire[i]) {
                continue;
            }

            headPendingBeamFireWaitTicks[i]++;

            float yawDifference = Math.abs(
                    wrapDegrees(headTargetYaw[i] - headCurrentYaw[i])
            );

            float pitchDifference = Math.abs(
                    headTargetPitch[i] - headCurrentPitch[i]
            );

            boolean converged = yawDifference <= HEAD_FIRE_ANGLE_TOLERANCE_DEGREES
                    && pitchDifference <= HEAD_FIRE_ANGLE_TOLERANCE_DEGREES;

            boolean timedOut = headPendingBeamFireWaitTicks[i] >= HEAD_FIRE_MAX_WAIT_TICKS;

            if (converged || timedOut) {
                headPendingBeamFire[i] = false;
                fireHeadBeam(i);
            }
        }

        boolean scheduled = false;

        for (int i = 0; i < 5; i++) {
            if (individualHeadFireTimers[i] >= 0 || headPendingBeamFire[i]) {
                scheduled = true;
                break;
            }
        }

        if (!scheduled) {
            attackSequenceActive = false;
            fullHeadAttackActive = false;
        }
    }

    private void fireHeadBeam(int headIndex) {
        if (this.level().isClientSide || !isHeadActive(headIndex)) {
            return;
        }

        if (attackTarget == null || !attackTarget.isAlive()) {
            return;
        }

        if (attackTarget.distanceToSqr(this) > ATTACK_RANGE * ATTACK_RANGE) {
            return;
        }

        if (!canHeadReachTarget(headIndex, attackTarget)) {
            return;
        }

        float[] rotation = calculateTargetRotation(headIndex, attackTarget);

        headTargetYaw[headIndex] = Mth.clamp(
                rotation[0],
                -ATTACK_HEAD_MAX_YAW,
                ATTACK_HEAD_MAX_YAW
        );

        headTargetPitch[headIndex] = Mth.clamp(
                rotation[1],
                -ATTACK_HEAD_MAX_PITCH,
                ATTACK_HEAD_MAX_PITCH
        );

        headCurrentYaw[headIndex] = headTargetYaw[headIndex];
        headCurrentPitch[headIndex] = headTargetPitch[headIndex];

        Vec3 headPosition = getApproximateHeadWorldPosition(headIndex);
        Vec3 direction = attackTarget.getEyePosition().subtract(headPosition);

        if (direction.lengthSqr() < 0.0001D) {
            return;
        }

        direction = direction.normalize();

        this.level().playSound(
                null,
                headPosition.x,
                headPosition.y,
                headPosition.z,
                ROACWSoundRegistry.ENDO_HYDRA_FIRING.get(),
                SoundSource.HOSTILE,
                1.0F,
                1.0F
        );

        EndoHydraBeamEntity beam = new EndoHydraBeamEntity(
                ROACWEntityRegistry.ENDO_HYDRA_BEAM.get(),
                this.level()
        );

        beam.setPos(headPosition.x, headPosition.y, headPosition.z);

        beam.setupBeam(
                this,
                headPosition,
                direction,
                headIndex,
                attackTarget.getEyePosition(),
                getBeamDamage(),
                fullHeadAttackActive,
                ROACWSpellRegistry.SUMMON_ENDO_HYDRA.get()
        );

        this.level().addFreshEntity(beam);
    }

    private Vec3 getDesiredBodyPosition(LivingEntity owner) {
        float yaw = owner.yBodyRot * Mth.DEG_TO_RAD;

        double forwardX = -Math.sin(yaw);
        double forwardZ = Math.cos(yaw);

        double x = owner.getX() - forwardX * BODY_BACK_DISTANCE;
        double z = owner.getZ() - forwardZ * BODY_BACK_DISTANCE;
        double y = owner.getY() + BODY_VERTICAL_OFFSET;

        return new Vec3(x, y, z);
    }

    private void updateBodyPosition(LivingEntity owner) {
        Vec3 desired = getDesiredBodyPosition(owner);
        Vec3 current = this.position();
        Vec3 next = current.lerp(desired, FOLLOW_LERP);

        this.setPos(next.x, next.y, next.z);

        float ownerYaw = owner.yBodyRot;

        this.setYRot(ownerYaw);
        this.setYBodyRot(ownerYaw);
        this.setYHeadRot(ownerYaw);
        this.setDeltaMovement(Vec3.ZERO);
    }

    @Override
    public void tick() {
        super.tick();

        updateHeadLooking();

        if (!this.level().isClientSide) {
            updateHeadAnimationLocks();
        }

        if (this.level().isClientSide) {
            return;
        }

        LivingEntity owner = getSummonOwner();

        if (owner == null || !owner.isAlive()) {
            this.discard();
            return;
        }

        updateBodyPosition(owner);
        updateIndividualHeadAttacks();
        updateAutoAttack(owner);
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        return false;
    }

    @Override
    public boolean isInvulnerableTo(DamageSource source) {
        return true;
    }

    @Override
    public boolean canBeSeenAsEnemy() {
        return false;
    }

    @Override
    public boolean isPushable() {
        return false;
    }

    @Override
    public boolean canBeCollidedWith() {
        return false;
    }

    @Override
    public boolean canCollideWith(Entity entity) {
        return false;
    }

    @Override
    public void push(Entity entity) {
    }

    @Override
    public void pushEntities() {
    }

    @Override
    public boolean isAffectedByFluids() {
        return false;
    }

    @Override
    public boolean causeFallDamage(float fallDistance, float multiplier, DamageSource source) {
        return false;
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putInt("SpellLevel", getSpellLevel());
        tag.putFloat("SpellPower", getSpellPower());
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);

        if (tag.contains("SpellLevel")) {
            setSpellLevel(tag.getInt("SpellLevel"));
        }

        if (tag.contains("SpellPower")) {
            setSpellPower(tag.getFloat("SpellPower"));
        }
    }

    public void triggerHeadAttack(int headIndex) {
        switch (headIndex) {
            case 0 -> this.triggerAnim("head_1_attack_controller", "head_1_attack");
            case 1 -> this.triggerAnim("head_2_attack_controller", "head_2_attack");
            case 2 -> this.triggerAnim("head_3_attack_controller", "head_3_attack");
            case 3 -> this.triggerAnim("head_4_attack_controller", "head_4_attack");
            case 4 -> this.triggerAnim("head_5_attack_controller", "head_5_attack");
        }
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(
                this,
                "body_controller",
                3,
                this::animationPredicate
        ));

        controllers.add(new AnimationController<>(
                this,
                "head_1_attack_controller",
                0,
                state -> PlayState.STOP
        ).triggerableAnim("head_1_attack", HEAD_1_ATTACK));

        controllers.add(new AnimationController<>(
                this,
                "head_2_attack_controller",
                0,
                state -> PlayState.STOP
        ).triggerableAnim("head_2_attack", HEAD_2_ATTACK));

        controllers.add(new AnimationController<>(
                this,
                "head_3_attack_controller",
                0,
                state -> PlayState.STOP
        ).triggerableAnim("head_3_attack", HEAD_3_ATTACK));

        controllers.add(new AnimationController<>(
                this,
                "head_4_attack_controller",
                0,
                state -> PlayState.STOP
        ).triggerableAnim("head_4_attack", HEAD_4_ATTACK));

        controllers.add(new AnimationController<>(
                this,
                "head_5_attack_controller",
                0,
                state -> PlayState.STOP
        ).triggerableAnim("head_5_attack", HEAD_5_ATTACK));
    }

    private PlayState animationPredicate(AnimationState<EndoHydraEntity> state) {
        state.getController().setAnimation(BODY_IDLE);
        return PlayState.CONTINUE;
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return cache;
    }
}