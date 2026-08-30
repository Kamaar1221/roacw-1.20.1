package net.kamaarion.roacw.entity.summon.dark_raven;

import io.redspace.ironsspellbooks.capabilities.magic.SummonManager;
import io.redspace.ironsspellbooks.entity.mobs.IMagicSummon;
import net.kamaarion.roacw.ROACW;
import net.kamaarion.roacw.registeries.ROACWSoundRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.AnimationState;
import software.bernie.geckolib.core.animation.RawAnimation;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.core.object.PlayState;
import software.bernie.geckolib.util.GeckoLibUtil;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class DarkRavenEntity
        extends PathfinderMob
        implements GeoEntity, IMagicSummon {

    private static final int CHARGE_HIT_FOLLOWTHROUGH_TICKS = 5;

    private static final EntityDataAccessor<Boolean> DATA_CHARGING =
            SynchedEntityData.defineId(
                    DarkRavenEntity.class,
                    EntityDataSerializers.BOOLEAN
            );

    private static final EntityDataAccessor<Boolean> DATA_SHOW_CHARGE_ANIM =
            SynchedEntityData.defineId(
                    DarkRavenEntity.class,
                    EntityDataSerializers.BOOLEAN
            );

    private static final EntityDataAccessor<Boolean> DATA_PERCHED =
            SynchedEntityData.defineId(
                    DarkRavenEntity.class,
                    EntityDataSerializers.BOOLEAN
            );

    private static final RawAnimation FLAP_ANIM =
            RawAnimation.begin()
                    .thenLoop("Flap");

    private static final RawAnimation PERCH_ANIM =
            RawAnimation.begin()
                    .thenLoop("Perch");

    private static final RawAnimation CHARGE_ANIM =
            RawAnimation.begin()
                    .thenLoop("Charging_(bad)");

    private static final int CHARGE_WINDUP_TICKS = 8;

    /**
     * Vertical amplitude (in blocks) of the small hover bob applied
     * each windup tick so the raven doesn't look completely frozen
     * while winding up to charge - wings are still flapping (see
     * animationPredicate) but without this the hitbox itself was
     * pinned to an exact point for 8 straight ticks, which read as
     * unnaturally stiff.
     */
    private static final double WINDUP_HOVER_BOB_AMPLITUDE = 0.08D;

    /**
     * Radians of bob phase advanced per windup tick. Math.PI / 2.0
     * gives a full sine cycle every 4 ticks, so across the 8-tick
     * windup the raven bobs through 2 full cycles - noticeable
     * without being distracting.
     */
    private static final double WINDUP_HOVER_BOB_SPEED =
            Math.PI / 2.0D;

    private static final double CHARGE_SPEED = 2.5D;

    private static final double CHARGE_OVERSHOOT_DISTANCE = 5.0D;

    private static final int CHARGE_MAX_TICKS = 40;

    private static final float DASH_SPELLPOWER_MULTIPLIER = 1.0F;

    /**
     * Chance (0.0-1.0) that the attack sound actually plays when a
     * charge starts.
     */
    private static final float ATTACK_SOUND_CHANCE = 0.2F;

    /**
     * Minimum ticks between attack-sound plays shared across a single
     * summoner's whole raven group.
     */
    private static final int ATTACK_SOUND_GROUP_COOLDOWN_TICKS = 60;

    /**
     * Last game-time (in ticks) the attack sound played, keyed by
     * summoner UUID.
     */
    private static final Map<UUID, Long> lastAttackSoundGameTime =
            new HashMap<>();

    /*
     * ------------------------------------------------------------------
     * RANDOMIZED / SWARM-AWARE IDLE SOUND SETTINGS
     * ------------------------------------------------------------------
     *
     * Vanilla ambient sound scheduling is disabled for the raven.
     * Idle calls are controlled entirely by the system below.
     */

    private static final int IDLE_SOUND_MIN_DELAY_TICKS = 160;
    private static final int IDLE_SOUND_MAX_DELAY_TICKS = 400;

    private static final int IDLE_SOUND_GROUP_MIN_COOLDOWN_TICKS = 80;
    private static final int IDLE_SOUND_GROUP_MAX_COOLDOWN_TICKS = 200;

    /**
     * If a raven's personal timer expires while the swarm is on cooldown,
     * retry in 1-3 seconds instead of waiting another full personal timer.
     */
    private static final int IDLE_SOUND_RETRY_MIN_TICKS = 20;
    private static final int IDLE_SOUND_RETRY_MAX_TICKS = 60;

    /**
     * Next game-time at which an idle sound is permitted for a given
     * summoner's entire raven group.
     */
    private static final Map<UUID, Long> nextIdleSoundAllowedGameTime =
            new HashMap<>();

    /*
     * ------------------------------------------------------------------
     * AIRBORNE CHARGE SETTINGS
     * ------------------------------------------------------------------
     */

    /**
     * Height above the target's feet that the raven normally aims at.
     *
     * This is now only used as a fallback. The actual charge target
     * prefers the target's bounding-box center, which works much better
     * for small mobs.
     */
    private static final double CHARGE_TARGET_HEIGHT = 1.75D;

    /**
     * Minimum amount of space between the bottom of the raven and terrain.
     */
    private static final double CHARGE_GROUND_CLEARANCE = 1.25D;

    /**
     * How far below the raven's current position we search for terrain.
     */
    private static final double CHARGE_GROUND_SEARCH_DISTANCE = 16.0D;

    /**
     * Extra padding used when checking the raven's bounding box.
     */
    private static final double CHARGE_COLLISION_PADDING = 0.05D;

    /**
     * Extra size added to the raven's dash hitbox.
     *
     * This is deliberately larger than the old value so very small
     * entities such as Endermites are easier to hit.
     */
    private static final double CHARGE_TARGET_PADDING = 0.30D;

    /**
     * Distance used when sampling the dash path.
     *
     * Smaller values make the dash much less likely to skip over
     * small entities between ticks.
     */
    private static final double CHARGE_PATH_STEP = 0.10D;

    /**
     * -------------------------------------------------------------
     * TELEPORT CHARGE
     * -------------------------------------------------------------
     *
     * Occasionally, instead of flying/hopping into charge range
     * normally, a raven blinks to a spot near its target and charges
     * immediately from there, then blinks back once the charge ends.
     * (Same idea as the "attack sound played" throttling problem -
     * this is decided ONCE per attack-cooldown window, not re-rolled
     * every tick, so a low chance stays a low chance in practice.)
     */

    /**
     * Chance (0.0-1.0), rolled once per raven attack-cooldown window
     * in DarkRavenAttackGoal, that the raven teleport-charges instead
     * of approaching normally.
     */
    /**
     * Package-private (not private) so DarkRavenAttackGoal, which
     * decides when to roll for a teleport-charge, can read it
     * directly.
     */
    static final float TELEPORT_CHARGE_CHANCE = 0.2F;

    /**
     * Random horizontal distance range (in blocks) from the target
     * that a teleport-charge can land the raven at.
     */
    private static final double TELEPORT_CHARGE_MIN_DISTANCE = 3.0D;

    private static final double TELEPORT_CHARGE_MAX_DISTANCE = 6.0D;

    /**
     * How many randomized landing spots to try before giving up and
     * falling back to a normal (non-teleport) charge, so the raven
     * never gets stuck attempting to teleport into a wall.
     */
    private static final int TELEPORT_CHARGE_MAX_ATTEMPTS = 8;

    private static final double PERCH_GROUND_CLEARANCE = 0.05D;

    private static final int OWNER_TARGET_FRESHNESS_TICKS = 100;

    private static final int PERCH_RING_COUNT = 3;

    private static final int PERCH_ANGLES_PER_RING = 10;

    private static final double PREFERRED_PERCH_HEIGHT = 2.0D;

    private static final double MAX_PREFERRED_PERCH_HEIGHT = 8.0D;

    private static final double MAX_PERCH_DROP = 2.0D;

    private static final double PERCH_HEADROOM = 1.5D;

    private static final double MAX_TARGET_DISTANCE_FROM_OWNER = 24.0D;

    private static final double MAX_TARGET_DISTANCE_FROM_OWNER_SQR =
            MAX_TARGET_DISTANCE_FROM_OWNER
                    * MAX_TARGET_DISTANCE_FROM_OWNER;

    private final AnimatableInstanceCache cache =
            GeckoLibUtil.createInstanceCache(this);

    private final Set<UUID> chargeVictims =
            new HashSet<>();

    private float spellPower = 2.0F;

    @Nullable
    private Vec3 chargeDestination;

    @Nullable
    private Vec3 chargeStart;

    private int chargeTicks = -1;

    private int chargeAnimReleaseTicks = -1;

    private boolean finishedCharge = false;

    private float chargeYaw = 0.0F;

    /**
     * Individual randomized idle-sound timer for this raven.
     */
    private int idleSoundCooldownTicks;

    /**
     * Highest Y that the ground-clearance safety net (see
     * {@link #getMinimumChargeY}) is allowed to push the raven to
     * during the current charge.
     *
     * Without this cap, CHARGE_GROUND_CLEARANCE (1.25 blocks) always
     * wins over the small-target aiming logic in {@link #startCharge}:
     * an Endermite's hitbox tops out around groundY + 0.3 and a Baby
     * Zombie's around groundY + 0.975, both well below groundY + 1.25.
     * The clearance clamp would force the raven to fly above the
     * target's entire hitbox, so it could aim correctly and still
     * never actually intersect the target.
     *
     * This is set to the target's own hitbox top in startCharge, so
     * tall mobs still get the full 1.25-block clipping buffer, while
     * short mobs let the raven dip down far enough to actually hit
     * them. It's reset to +infinity in stopCharge so it never affects
     * anything outside of an active charge.
     */
    private double chargeCeilingY = Double.POSITIVE_INFINITY;

    public DarkRavenEntity(
            EntityType<? extends PathfinderMob> type,
            Level level
    ) {
        super(type, level);

        this.setPersistenceRequired();
        this.setNoGravity(true);

        this.moveControl =
                new DarkRavenMoveControl(this);

        resetIdleSoundCooldown();
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();

        this.entityData.define(
                DATA_CHARGING,
                false
        );

        this.entityData.define(
                DATA_SHOW_CHARGE_ANIM,
                false
        );

        this.entityData.define(
                DATA_PERCHED,
                false
        );
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
                .add(
                        Attributes.MAX_HEALTH,
                        20.0D
                )
                .add(
                        Attributes.MOVEMENT_SPEED,
                        0.35D
                )
                .add(
                        Attributes.FOLLOW_RANGE,
                        24.0D
                )
                .add(
                        Attributes.ATTACK_DAMAGE,
                        10.0D
                );
    }

    @Override
    protected void registerGoals() {

        this.goalSelector.addGoal(
                1,
                new DarkRavenAttackGoal(this)
        );

        this.goalSelector.addGoal(
                2,
                new DarkRavenPerchGoal(this)
        );

        this.goalSelector.addGoal(
                3,
                new LookAtPlayerGoal(
                        this,
                        Player.class,
                        8.0F
                )
        );

        this.targetSelector.addGoal(
                1,
                new HurtByTargetGoal(this)
                        .setAlertOthers()
        );

        this.targetSelector.addGoal(
                2,
                new NearestAttackableTargetGoal<>(
                        this,
                        Monster.class,
                        10,
                        true,
                        false,
                        target ->
                                target != this.getSummonOwner()
                                        && !this.isFriendlyRaven(target)
                )
        );
    }

    @Override
    public void addAdditionalSaveData(
            CompoundTag tag
    ) {
        super.addAdditionalSaveData(tag);

        tag.putFloat(
                "SpellPower",
                this.spellPower
        );
    }

    @Override
    public void readAdditionalSaveData(
            CompoundTag tag
    ) {
        super.readAdditionalSaveData(tag);

        if (tag.contains("SpellPower")) {
            this.spellPower =
                    tag.getFloat("SpellPower");
        }
    }

    public void setSpellPower(float spellPower) {
        this.spellPower = spellPower;
    }

    public float getSpellPower() {
        return this.spellPower;
    }

    /*
     * ------------------------------------------------------------------
     * SOUND OVERRIDES
     * ------------------------------------------------------------------
     */

    /**
     * Idle sounds are handled manually by tick()/tryPlayIdleSound().
     *
     * Returning null is important: otherwise Mob's normal ambient-sound
     * system can independently play the same sound and bypass the
     * randomized swarm cooldown.
     */
    @Override
    protected net.minecraft.sounds.SoundEvent getAmbientSound() {
        return null;
    }

    @Override
    protected net.minecraft.sounds.SoundEvent getDeathSound() {
        return ROACWSoundRegistry.DARK_RAVEN_HURT.get();
    }

    @Override
    protected float getSoundVolume() {
        return 0.7F;
    }

    @Override
    public float getVoicePitch() {
        return 0.9F
                + this.random.nextFloat() * 0.12F;
    }

    /**
     * Kept for API compatibility. It is no longer used to schedule idle
     * calls.
     */
    @Override
    public int getAmbientSoundInterval() {
        return 60;
    }

    private void resetIdleSoundCooldown() {
        this.idleSoundCooldownTicks =
                IDLE_SOUND_MIN_DELAY_TICKS
                        + this.random.nextInt(
                        IDLE_SOUND_MAX_DELAY_TICKS
                                - IDLE_SOUND_MIN_DELAY_TICKS
                                + 1
                );
    }

    private void resetIdleSoundRetryCooldown() {
        this.idleSoundCooldownTicks =
                IDLE_SOUND_RETRY_MIN_TICKS
                        + this.random.nextInt(
                        IDLE_SOUND_RETRY_MAX_TICKS
                                - IDLE_SOUND_RETRY_MIN_TICKS
                                + 1
                );
    }

    /**
     * Attempts to play an idle call.
     *
     * Each raven has an independent 12-30 second timer, while all ravens
     * belonging to the same summoner share a randomized 6-15 second
     * cooldown. This prevents a swarm from sounding like several copies
     * of the same raven calling simultaneously.
     */
    private void tryPlayIdleSound() {
        if (this.isCharging() || this.isPerched()) {
            return;
        }

        if (this.idleSoundCooldownTicks > 0) {
            this.idleSoundCooldownTicks--;
            return;
        }

        long gameTime = this.level().getGameTime();

        LivingEntity summonOwner =
                this.getSummonOwner();

        UUID ownerId =
                summonOwner != null
                        ? summonOwner.getUUID()
                        : null;

        Long nextAllowed =
                nextIdleSoundAllowedGameTime.get(ownerId);

        if (nextAllowed != null
                && gameTime < nextAllowed) {

            resetIdleSoundRetryCooldown();
            return;
        }

        /*
         * A much wider pitch range than the old 0.900-0.901 range makes
         * repeated calls sound less mechanically identical.
         */
        this.playSound(
                ROACWSoundRegistry.DARK_RAVEN_IDLE.get(),
                0.7F,
                0.85F
                        + this.random.nextFloat() * 0.20F
        );

        resetIdleSoundCooldown();

        int groupCooldown =
                IDLE_SOUND_GROUP_MIN_COOLDOWN_TICKS
                        + this.random.nextInt(
                        IDLE_SOUND_GROUP_MAX_COOLDOWN_TICKS
                                - IDLE_SOUND_GROUP_MIN_COOLDOWN_TICKS
                                + 1
                );

        nextIdleSoundAllowedGameTime.put(
                ownerId,
                gameTime + groupCooldown
        );
    }

    @Override
    public void setTarget(
            @Nullable LivingEntity target
    ) {
        if (this.isCharging()
                && target != this.getTarget()) {
            return;
        }

        super.setTarget(target);
    }

    public boolean isCharging() {
        return this.entityData.get(
                DATA_CHARGING
        );
    }

    private void setCharging(
            boolean charging
    ) {
        this.entityData.set(
                DATA_CHARGING,
                charging
        );
    }

    private boolean isChargeAnimating() {
        return this.entityData.get(
                DATA_SHOW_CHARGE_ANIM
        );
    }

    private void setChargeAnimating(
            boolean animating
    ) {
        this.entityData.set(
                DATA_SHOW_CHARGE_ANIM,
                animating
        );
    }

    public boolean isPerched() {
        return this.entityData.get(
                DATA_PERCHED
        );
    }

    public void setPerched(
            boolean perched
    ) {
        this.entityData.set(
                DATA_PERCHED,
                perched
        );
    }

    public boolean consumeFinishedCharge() {

        if (!finishedCharge) {
            return false;
        }

        finishedCharge = false;

        return true;
    }

    public DarkRavenMoveControl getRavenMoveControl() {
        return (DarkRavenMoveControl) this.moveControl;
    }

    public void moveToward(
            double x,
            double y,
            double z,
            double speed
    ) {
        getRavenMoveControl().moveToward(
                x,
                y,
                z,
                speed
        );
    }

    public void moveToward(
            Entity target,
            double speed
    ) {
        if (target == null) {
            return;
        }

        moveToward(
                target.getX(),
                target.getY(),
                target.getZ(),
                speed
        );
    }

    public void moveToRange(
            LivingEntity target,
            double desiredDistance,
            double speed
    ) {
        if (target == null) {
            return;
        }

        Vec3 toSelf =
                this.position()
                        .subtract(target.position());

        if (toSelf.lengthSqr() < 0.001D) {
            toSelf = this.getLookAngle();
        }

        Vec3 destination =
                target.position()
                        .add(
                                toSelf.normalize()
                                        .scale(desiredDistance)
                        );

        getRavenMoveControl().moveToward(
                destination.x,
                destination.y,
                destination.z,
                speed
        );
    }

    public void stopMoving() {

        this.getNavigation().stop();

        getRavenMoveControl().stop();

        this.setDeltaMovement(
                Vec3.ZERO
        );
    }

    public void faceTarget(
            LivingEntity target,
            float maxTurn
    ) {
        if (target == null) {
            return;
        }

        double dx =
                target.getX()
                        - this.getX();

        double dz =
                target.getZ()
                        - this.getZ();

        float targetYaw =
                (float) (
                        -Math.toDegrees(
                                Math.atan2(dx, dz)
                        )
                );

        float yaw =
                Mth.rotateIfNecessary(
                        this.getYRot(),
                        targetYaw,
                        maxTurn
                );

        this.setYRot(yaw);
        this.yRotO = yaw;

        this.setYBodyRot(yaw);
        this.yBodyRotO = yaw;

        this.setYHeadRot(yaw);
        this.yHeadRotO = yaw;
    }

    public void faceDirection(
            Vec3 targetPos,
            float maxTurn
    ) {

        double dx =
                targetPos.x
                        - this.getX();

        double dz =
                targetPos.z
                        - this.getZ();

        if (dx * dx + dz * dz < 1.0E-4D) {
            return;
        }

        float targetYaw =
                (float) (
                        -Math.toDegrees(
                                Math.atan2(dx, dz)
                        )
                );

        float yaw =
                Mth.rotateIfNecessary(
                        this.getYRot(),
                        targetYaw,
                        maxTurn
                );

        this.setYRot(yaw);
        this.yRotO = yaw;

        this.setYBodyRot(yaw);
        this.yBodyRotO = yaw;
    }

    public Vec3 findPerchSpot(
            LivingEntity owner,
            double radius
    ) {

        Vec3 bestSpot = null;

        double bestScore =
                -Double.MAX_VALUE;

        RandomSource random =
                this.getRandom();

        double ownerGroundY =
                owner.getY();

        for (
                int ring = 1;
                ring <= PERCH_RING_COUNT;
                ring++
        ) {

            double candidateRadius =
                    radius
                            * (
                            (double) ring
                                    / PERCH_RING_COUNT
                    );

            for (
                    int a = 0;
                    a < PERCH_ANGLES_PER_RING;
                    a++
            ) {

                double baseAngle =
                        (
                                Math.PI * 2.0D
                                        / PERCH_ANGLES_PER_RING
                        ) * a;

                double jitter =
                        (
                                random.nextDouble()
                                        - 0.5D
                        )
                                * (
                                Math.PI * 2.0D
                                        / PERCH_ANGLES_PER_RING
                        )
                                * 0.5D;

                double angle =
                        baseAngle + jitter;

                double x =
                        owner.getX()
                                + Math.cos(angle)
                                * candidateRadius;

                double z =
                        owner.getZ()
                                + Math.sin(angle)
                                * candidateRadius;

                double searchTop =
                        owner.getY() + 12.0D;

                double searchBottom =
                        owner.getY()
                                - MAX_PERCH_DROP;

                Vec3 rayStart =
                        new Vec3(
                                x,
                                searchTop,
                                z
                        );

                Vec3 rayEnd =
                        new Vec3(
                                x,
                                searchBottom,
                                z
                        );

                HitResult hit =
                        this.level().clip(
                                new ClipContext(
                                        rayStart,
                                        rayEnd,
                                        ClipContext.Block.COLLIDER,
                                        ClipContext.Fluid.NONE,
                                        this
                                )
                        );

                if (hit.getType()
                        != HitResult.Type.BLOCK) {
                    continue;
                }

                Vec3 hitLocation =
                        hit.getLocation();

                BlockPos groundPos =
                        BlockPos.containing(
                                hitLocation.x,
                                hitLocation.y - 0.01D,
                                hitLocation.z
                        );

                if (!isValidPerchBlock(groundPos)) {
                    continue;
                }

                if (!hasPerchHeadroom(groundPos)) {
                    continue;
                }

                double perchY =
                        hitLocation.y
                                + PERCH_GROUND_CLEARANCE;

                double heightAboveOwner =
                        perchY - ownerGroundY;

                if (heightAboveOwner
                        < -MAX_PERCH_DROP) {
                    continue;
                }

                double score = 0.0D;

                if (heightAboveOwner
                        >= PREFERRED_PERCH_HEIGHT) {

                    double normalizedHeight =
                            Mth.clamp(
                                    (
                                            heightAboveOwner
                                                    - PREFERRED_PERCH_HEIGHT
                                    )
                                            / (
                                            MAX_PREFERRED_PERCH_HEIGHT
                                                    - PREFERRED_PERCH_HEIGHT
                                    ),
                                    0.0D,
                                    1.0D
                            );

                    score +=
                            8.0D
                                    * normalizedHeight;

                } else {

                    score +=
                            heightAboveOwner
                                    * 1.5D;
                }

                double horizontalDistance =
                        Math.sqrt(
                                horizontalDistanceSqr(
                                        new Vec3(
                                                owner.getX(),
                                                0.0D,
                                                owner.getZ()
                                        ),
                                        new Vec3(
                                                hitLocation.x,
                                                0.0D,
                                                hitLocation.z
                                        )
                                )
                        );

                score +=
                        (
                                radius
                                        - horizontalDistance
                        )
                                * 0.75D;

                score +=
                        random.nextDouble()
                                * 1.5D;

                if (score > bestScore) {

                    bestScore = score;

                    bestSpot =
                            new Vec3(
                                    hitLocation.x,
                                    perchY,
                                    hitLocation.z
                            );
                }
            }
        }

        if (bestSpot == null) {

            double angle =
                    random.nextDouble()
                            * Math.PI
                            * 2.0D;

            double x =
                    owner.getX()
                            + Math.cos(angle)
                            * radius;

            double z =
                    owner.getZ()
                            + Math.sin(angle)
                            * radius;

            Vec3 rayStart =
                    new Vec3(
                            x,
                            owner.getY() + 10.0D,
                            z
                    );

            Vec3 rayEnd =
                    new Vec3(
                            x,
                            owner.getY() - 10.0D,
                            z
                    );

            HitResult hit =
                    this.level().clip(
                            new ClipContext(
                                    rayStart,
                                    rayEnd,
                                    ClipContext.Block.COLLIDER,
                                    ClipContext.Fluid.NONE,
                                    this
                            )
                    );

            double groundY =
                    hit.getType()
                            == HitResult.Type.BLOCK
                            ? hit.getLocation().y
                            : owner.getY();

            bestSpot =
                    new Vec3(
                            x,
                            groundY
                                    + PERCH_GROUND_CLEARANCE,
                            z
                    );
        }

        return bestSpot;
    }

    private boolean isValidPerchBlock(
            BlockPos pos
    ) {

        var state =
                this.level().getBlockState(pos);

        if (state.getCollisionShape(
                this.level(),
                pos
        ).isEmpty()) {
            return false;
        }

        return !state.getFluidState().isSource();
    }

    public boolean hasClearanceAt(
            Vec3 pos
    ) {

        AABB box =
                this.getBoundingBox().move(
                        pos.subtract(
                                this.position()
                        )
                );

        return this.level().noCollision(
                this,
                box
        );
    }

    private boolean hasPerchHeadroom(
            BlockPos perchPos
    ) {

        AABB ravenBox =
                this.getBoundingBox();

        double width =
                ravenBox.getXsize();

        double height =
                Math.max(
                        ravenBox.getYsize(),
                        PERCH_HEADROOM
                );

        AABB testBox =
                new AABB(
                        perchPos.getX()
                                + 0.5D
                                - width * 0.5D,

                        perchPos.getY()
                                + 1.0D,

                        perchPos.getZ()
                                + 0.5D
                                - width * 0.5D,

                        perchPos.getX()
                                + 0.5D
                                + width * 0.5D,

                        perchPos.getY()
                                + 1.0D
                                + height,

                        perchPos.getZ()
                                + 0.5D
                                + width * 0.5D
                );

        return this.level().noCollision(
                this,
                testBox
        );
    }

    private static double horizontalDistanceSqr(
            Vec3 a,
            Vec3 b
    ) {

        double dx =
                a.x - b.x;

        double dz =
                a.z - b.z;

        return dx * dx + dz * dz;
    }

    @Nullable
    public LivingEntity getSummonOwner() {

        var owner =
                SummonManager.getOwner(this);

        return owner instanceof LivingEntity living
                ? living
                : null;
    }

    public boolean isFriendlyRaven(
            Entity other
    ) {

        if (!(other instanceof DarkRavenEntity that)) {
            return false;
        }

        LivingEntity myOwner =
                this.getSummonOwner();

        LivingEntity theirOwner =
                that.getSummonOwner();

        return myOwner != null
                && theirOwner != null
                && myOwner.getUUID().equals(
                theirOwner.getUUID()
        );
    }

    /*
     * ------------------------------------------------------------------
     * ENTITY COLLISION
     * ------------------------------------------------------------------
     *
     * Ravens are completely non-collidable with entities.
     *
     * This is important because a flying raven passing over a player
     * can otherwise count as an entity collision and cause Minecraft
     * to put the player into its crouching/swimming collision state.
     *
     * Their charge damage does NOT depend on entity collision. The
     * custom intersectsDashPath() system below handles damage instead.
     */

    @Override
    public boolean isPushable() {
        return false;
    }

    @Override
    public boolean canBeCollidedWith() {
        return false;
    }

    @Override
    public boolean canCollideWith(
            Entity entity
    ) {
        return false;
    }

    @Override
    public void push(
            Entity entity
    ) {
        // Ravens never physically push entities.
    }

    /*
     * ------------------------------------------------------------------
     * MOVEMENT
     * ------------------------------------------------------------------
     */

    @Override
    public void travel(
            Vec3 travelVector
    ) {

        if (this.isCharging()) {

            this.calculateEntityAnimation(true);

            return;
        }

        this.move(
                MoverType.SELF,
                this.getDeltaMovement()
        );

        this.calculateEntityAnimation(true);
    }

    /**
     * Finds the highest solid collision surface directly beneath a
     * position.
     */
    private double getGroundY(
            double x,
            double y,
            double z
    ) {

        double searchTop =
                Math.max(
                        y + 2.0D,
                        this.level().getMaxBuildHeight()
                );

        double searchBottom =
                Math.max(
                        this.level().getMinBuildHeight(),
                        y - CHARGE_GROUND_SEARCH_DISTANCE
                );

        Vec3 start =
                new Vec3(
                        x,
                        searchTop,
                        z
                );

        Vec3 end =
                new Vec3(
                        x,
                        searchBottom,
                        z
                );

        HitResult hit =
                this.level().clip(
                        new ClipContext(
                                start,
                                end,
                                ClipContext.Block.COLLIDER,
                                ClipContext.Fluid.NONE,
                                this
                        )
                );

        if (hit.getType()
                == HitResult.Type.BLOCK) {

            return hit.getLocation().y;
        }

        return Double.NEGATIVE_INFINITY;
    }

    private double getMinimumChargeY(
            double x,
            double y,
            double z
    ) {

        double groundY =
                getGroundY(
                        x,
                        y,
                        z
                );

        if (groundY
                == Double.NEGATIVE_INFINITY) {
            return Double.NEGATIVE_INFINITY;
        }

        double bottomOffset =
                this.getBoundingBox().minY
                        - this.getY();

        double floor =
                groundY
                        + CHARGE_GROUND_CLEARANCE
                        - bottomOffset;

        /*
         * Never let the clipping-safety clearance push the raven
         * above the current target's own hitbox - otherwise short
         * mobs standing on the ground are physically unreachable no
         * matter how well they're aimed at.
         */
        return Math.min(
                floor,
                chargeCeilingY
        );
    }

    private boolean canOccupyChargePosition(
            Vec3 position
    ) {

        AABB box =
                this.getBoundingBox()
                        .move(
                                position.subtract(
                                        this.position()
                                )
                        )
                        .inflate(
                                CHARGE_COLLISION_PADDING
                        );

        return this.level().noCollision(
                this,
                box
        );
    }

    private void performDashMovement(
            Vec3 motion
    ) {

        Vec3 start =
                this.position();

        Vec3 intendedEnd =
                start.add(motion);

        double minimumY =
                getMinimumChargeY(
                        intendedEnd.x,
                        intendedEnd.y,
                        intendedEnd.z
                );

        Vec3 end =
                intendedEnd;

        if (minimumY
                != Double.NEGATIVE_INFINITY
                && end.y < minimumY) {

            end =
                    new Vec3(
                            end.x,
                            minimumY,
                            end.z
                    );
        }

        HitResult obstruction =
                this.level().clip(
                        new ClipContext(
                                start,
                                end,
                                ClipContext.Block.COLLIDER,
                                ClipContext.Fluid.NONE,
                                this
                        )
                );

        if (obstruction.getType()
                != HitResult.Type.MISS) {

            Vec3 raisedEnd =
                    new Vec3(
                            end.x,
                            Math.max(
                                    end.y,
                                    obstruction.getLocation().y
                                            + CHARGE_GROUND_CLEARANCE
                            ),
                            end.z
                    );

            if (canOccupyChargePosition(
                    raisedEnd
            )) {

                end = raisedEnd;

            } else {

                stopCharge(false);

                return;
            }
        }

        if (!canOccupyChargePosition(end)) {

            double raisedY =
                    end.y + 0.5D;

            double raisedMinimumY =
                    getMinimumChargeY(
                            end.x,
                            raisedY,
                            end.z
                    );

            if (raisedMinimumY
                    != Double.NEGATIVE_INFINITY) {

                raisedY =
                        Math.max(
                                raisedY,
                                raisedMinimumY
                        );
            }

            Vec3 raisedEnd =
                    new Vec3(
                            end.x,
                            raisedY,
                            end.z
                    );

            if (canOccupyChargePosition(
                    raisedEnd
            )) {

                end = raisedEnd;

            } else {

                stopCharge(false);

                return;
            }
        }

        /*
         * --------------------------------------------------------------
         * DASH DAMAGE SWEEP
         * --------------------------------------------------------------
         */

        AABB ravenBox =
                this.getBoundingBox();

        double minX =
                Math.min(
                        start.x
                                + ravenBox.minX
                                - this.getX(),

                        end.x
                                + ravenBox.minX
                                - this.getX()
                );

        double minY =
                Math.min(
                        start.y
                                + ravenBox.minY
                                - this.getY(),

                        end.y
                                + ravenBox.minY
                                - this.getY()
                );

        double minZ =
                Math.min(
                        start.z
                                + ravenBox.minZ
                                - this.getZ(),

                        end.z
                                + ravenBox.minZ
                                - this.getZ()
                );

        double maxX =
                Math.max(
                        start.x
                                + ravenBox.maxX
                                - this.getX(),

                        end.x
                                + ravenBox.maxX
                                - this.getX()
                );

        double maxY =
                Math.max(
                        start.y
                                + ravenBox.maxY
                                - this.getY(),

                        end.y
                                + ravenBox.maxY
                                - this.getY()
                );

        double maxZ =
                Math.max(
                        start.z
                                + ravenBox.maxZ
                                - this.getZ(),

                        end.z
                                + ravenBox.maxZ
                                - this.getZ()
                );

        AABB sweepBox =
                new AABB(
                        minX,
                        minY,
                        minZ,
                        maxX,
                        maxY,
                        maxZ
                ).inflate(
                        CHARGE_TARGET_PADDING
                );

        LivingEntity owner =
                this.getSummonOwner();

        for (
                LivingEntity entity :
                this.level().getEntitiesOfClass(
                        LivingEntity.class,
                        sweepBox,
                        e ->
                                e != this
                                        && !(e instanceof DarkRavenEntity)
                                        && e != owner
                                        && e.isAlive()
                )
        ) {

            if (!intersectsDashPath(
                    start,
                    end,
                    entity
            )) {
                continue;
            }

            if (!this.chargeVictims.add(
                    entity.getUUID()
            )) {
                continue;
            }

            Vec3 previousVelocity =
                    entity.getDeltaMovement();

            /*
             * Reset vanilla invulnerability so multiple ravens can
             * independently damage the same target.
             */
            entity.invulnerableTime = 0;

            boolean hit =
                    this.doHurtTarget(entity);

            entity.setDeltaMovement(
                    previousVelocity
            );

            if (hit) {

                this.level().broadcastEntityEvent(
                        this,
                        (byte) 4
                );
            }
        }

        this.setPos(
                end.x,
                end.y,
                end.z
        );
    }

    /**
     * Tests the entire dash path against the target's bounding box.
     *
     * The target box is deliberately expanded because very small mobs
     * can otherwise be skipped between two movement samples.
     */
    private boolean intersectsDashPath(
            Vec3 start,
            Vec3 end,
            LivingEntity target
    ) {

        AABB targetBox =
                target.getBoundingBox()
                        .inflate(
                                CHARGE_TARGET_PADDING
                        );

        Vec3 direction =
                end.subtract(start);

        double length =
                direction.length();

        if (length < 1.0E-6D) {

            AABB ravenBox =
                    this.getBoundingBox()
                            .move(
                                    start.subtract(
                                            this.position()
                                    )
                            );

            return ravenBox
                    .inflate(CHARGE_TARGET_PADDING)
                    .intersects(targetBox);
        }

        Vec3 step =
                direction.normalize();

        for (
                double distance = 0.0D;
                distance <= length;
                distance += CHARGE_PATH_STEP
        ) {

            Vec3 position =
                    start.add(
                            step.scale(distance)
                    );

            AABB ravenBox =
                    this.getBoundingBox()
                            .move(
                                    position.subtract(
                                            this.position()
                                    )
                            )
                            .inflate(
                                    CHARGE_TARGET_PADDING
                            );

            if (ravenBox.intersects(
                    targetBox
            )) {
                return true;
            }
        }

        AABB finalBox =
                this.getBoundingBox()
                        .move(
                                end.subtract(
                                        this.position()
                                )
                        )
                        .inflate(
                                CHARGE_TARGET_PADDING
                        );

        return finalBox.intersects(
                targetBox
        );
    }

    @Override
    public boolean causeFallDamage(
            float fallDistance,
            float multiplier,
            DamageSource source
    ) {
        return false;
    }

    /*
     * ------------------------------------------------------------------
     * TICK
     * ------------------------------------------------------------------
     */

    @Override
    public void tick() {

        super.tick();

        if (this.level().isClientSide) {
            return;
        }

        /*
         * --------------------------------------------------------------
         * RANDOMIZED / SWARM-AWARE IDLE SOUND
         * --------------------------------------------------------------
         */
        tryPlayIdleSound();

        if (this.isCharging()) {

            this.setYRot(chargeYaw);

            this.yBodyRot = chargeYaw;
            this.yHeadRot = chargeYaw;

            this.yBodyRotO = chargeYaw;
            this.yHeadRotO = chargeYaw;

            this.yRotO = chargeYaw;
        }

        LivingEntity owner =
                getSummonOwner();

        if (this.getTarget() != null) {

            LivingEntity currentTarget =
                    this.getTarget();

            boolean invalid =
                    !currentTarget.isAlive()
                            || (
                            owner != null
                                    && owner.distanceToSqr(
                                    currentTarget
                            ) > MAX_TARGET_DISTANCE_FROM_OWNER_SQR
                    );

            if (invalid) {
                this.setTarget(null);
            }
        }

        if (owner != null
                && this.getTarget() == null) {

            LivingEntity ownerTarget =
                    owner.getLastHurtMob();

            boolean recentlyHurt =
                    owner.tickCount
                            - owner.getLastHurtMobTimestamp()
                            <= OWNER_TARGET_FRESHNESS_TICKS;

            if (ownerTarget != null
                    && recentlyHurt
                    && ownerTarget.isAlive()
                    && this.distanceToSqr(
                    ownerTarget
            ) < 900.0D) {

                this.setTarget(ownerTarget);
            }
        }

        if (chargeAnimReleaseTicks >= 0) {

            chargeAnimReleaseTicks++;

            if (chargeAnimReleaseTicks
                    >= CHARGE_HIT_FOLLOWTHROUGH_TICKS) {

                setChargeAnimating(false);

                chargeAnimReleaseTicks = -1;
            }
        }

        if (!this.isCharging()) {
            return;
        }

        chargeTicks++;

        if (chargeTicks
                < CHARGE_WINDUP_TICKS) {

            this.setDeltaMovement(
                    Vec3.ZERO
            );

            applyWindupHoverBob();

            return;
        }

        if (chargeTicks
                == CHARGE_WINDUP_TICKS) {

            setChargeAnimating(true);
        }

        if (this.getDeltaMovement()
                .lengthSqr()
                < 1.0E-4D) {

            Vec3 direction =
                    chargeDestination.subtract(
                            this.position()
                    );

            if (direction.lengthSqr()
                    > 1.0E-4D) {

                this.setDeltaMovement(
                        direction.normalize()
                                .scale(
                                        CHARGE_SPEED
                                )
                );
            }
        }

        Vec3 motion =
                this.getDeltaMovement();

        if (motion.lengthSqr()
                > 1.0E-4D) {

            performDashMovement(motion);

            if (!this.isCharging()) {
                return;
            }
        }

        Vec3 chargeLine =
                chargeDestination.subtract(
                        chargeStart
                );

        Vec3 current =
                this.position().subtract(
                        chargeStart
                );

        if (current.dot(chargeLine)
                >= chargeLine.lengthSqr()) {

            stopCharge(false);

            return;
        }

        if (chargeTicks
                >= CHARGE_MAX_TICKS) {

            stopCharge(false);
        }
    }

    /**
     * Teleports the raven to a randomized spot near {@code target}
     * and immediately begins a charge from there ("teleport-charge").
     *
     * DarkRavenAttackGoal rolls TELEPORT_CHARGE_CHANCE once per
     * attack-cooldown window and calls this instead of the normal
     * approach-then-{@link #startCharge} flow when it hits.
     *
     * The actual teleport happens inside startCharge() itself, right
     * after the raven enters its charging state - so it's already
     * charging the instant it moves, and only teleports once (into
     * position; there's no return trip).
     *
     * If no safe landing spot can be found nearby (e.g. the raven is
     * boxed in), this falls back to a normal in-place charge rather
     * than teleporting into a wall.
     */
    public void teleportChargeTo(
            LivingEntity target
    ) {

        if (target == null
                || !target.isAlive()
                || this.isCharging()) {
            return;
        }

        Vec3 landingSpot =
                findTeleportLandingSpot(target);

        startCharge(
                target,
                landingSpot
        );
    }

    /**
     * Finds a randomized point near the target that the raven can
     * safely occupy (no block collision), trying a handful of random
     * angles/heights before giving up.
     */
    @Nullable
    private Vec3 findTeleportLandingSpot(
            LivingEntity target
    ) {

        for (int attempt = 0;
             attempt < TELEPORT_CHARGE_MAX_ATTEMPTS;
             attempt++) {

            double angle =
                    this.random.nextDouble()
                            * Math.PI
                            * 2.0D;

            double distance =
                    TELEPORT_CHARGE_MIN_DISTANCE
                            + this.random.nextDouble()
                            * (
                            TELEPORT_CHARGE_MAX_DISTANCE
                                    - TELEPORT_CHARGE_MIN_DISTANCE
                    );

            double x =
                    target.getX()
                            + Math.cos(angle) * distance;

            double z =
                    target.getZ()
                            + Math.sin(angle) * distance;

            double y =
                    target.getY()
                            + (
                            this.random.nextDouble()
                                    * 1.5D
                    );

            AABB testBox =
                    this.getBoundingBox()
                            .move(
                                    x - this.getX(),
                                    y - this.getY(),
                                    z - this.getZ()
                            );

            if (this.level()
                    .noCollision(
                            this,
                            testBox
                    )) {

                return new Vec3(x, y, z);
            }
        }

        return null;
    }

    /**
     * Enderman-style visual/audio cue for a teleport, played at both
     * the departure and arrival points.
     */
    private void playTeleportEffects(
            Vec3 position
    ) {

        if (!(this.level()
                instanceof ServerLevel serverLevel)) {
            return;
        }

        serverLevel.playSound(
                null,
                position.x,
                position.y,
                position.z,
                SoundEvents.ENDERMAN_TELEPORT,
                SoundSource.NEUTRAL,
                0.6F,
                1.0F
                        + (
                        this.random.nextFloat()
                                * 0.2F
                )
        );

        serverLevel.sendParticles(
                ParticleTypes.REVERSE_PORTAL,
                position.x,
                position.y + 0.3D,
                position.z,
                12,
                0.3D,
                0.3D,
                0.3D,
                0.02D
        );
    }

    /**
     * Small vertical hover bob applied each windup tick so the raven
     * doesn't look completely frozen while winding up to charge.
     *
     * Recomputed fresh from chargeStart every tick (a bounded
     * oscillation, not an accumulated one), so no matter how long the
     * windup runs the raven can never drift away from the position
     * the charge trajectory was actually calculated from - it just
     * settles back to chargeStart.y at the peak/trough of each cycle.
     */
    private void applyWindupHoverBob() {

        if (chargeStart == null) {
            return;
        }

        double bobOffset =
                Math.sin(
                        chargeTicks
                                * WINDUP_HOVER_BOB_SPEED
                )
                        * WINDUP_HOVER_BOB_AMPLITUDE;

        this.setPos(
                chargeStart.x,
                chargeStart.y + bobOffset,
                chargeStart.z
        );
    }

    /**
     * Starts an airborne charge toward a target.
     */
    public void startCharge(
            LivingEntity target
    ) {

        startCharge(
                target,
                null
        );
    }

    /**
     * Starts a charge toward a target, optionally teleporting the
     * raven to {@code teleportTo} first (the teleport-charge case).
     *
     * The teleport happens right after setCharging(true) - so the
     * raven is already in its charging state the instant it moves -
     * and BEFORE chargeStart/chargeDestination are computed, so the
     * dash trajectory is correctly calculated from the new (post-
     * teleport) position rather than the old one.
     */
    private void startCharge(
            LivingEntity target,
            @Nullable Vec3 teleportTo
    ) {

        if (target == null
                || !target.isAlive()
                || this.isCharging()) {
            return;
        }

        getRavenMoveControl().stop();

        setPerched(false);

        chargeVictims.clear();

        chargeTicks = 0;

        setCharging(true);

        if (teleportTo != null) {

            playTeleportEffects(
                    this.position()
            );

            this.teleportTo(
                    teleportTo.x,
                    teleportTo.y,
                    teleportTo.z
            );

            this.setDeltaMovement(
                    Vec3.ZERO
            );

            playTeleportEffects(
                    teleportTo
            );
        }

        /*
         * --------------------------------------------------------------
         * ATTACK SOUND
         * --------------------------------------------------------------
         *
         * This happens when THIS raven actually begins its charge.
         * Since DarkRavenAttackGoal gives every raven its own randomized
         * cooldown, the attack sounds are naturally staggered too.
         *
         * Gated behind ATTACK_SOUND_CHANCE so it doesn't bark on every
         * single charge, AND behind a shared group cooldown
         * (ATTACK_SOUND_GROUP_COOLDOWN_TICKS) so a big swarm of ravens
         * can't collectively spam the sound just because each one
         * individually has a decent chance to play it.
         *
         * Runs AFTER the teleport above so the sound plays from
         * wherever the raven actually ends up.
         */
        long gameTime =
                this.level().getGameTime();

        LivingEntity summonOwner =
                getSummonOwner();

        UUID ownerId =
                summonOwner != null
                        ? summonOwner.getUUID()
                        : null;

        Long lastPlayed =
                lastAttackSoundGameTime.get(ownerId);

        boolean groupOnCooldown =
                lastPlayed != null
                        && (gameTime - lastPlayed)
                        < ATTACK_SOUND_GROUP_COOLDOWN_TICKS;

        if (!groupOnCooldown
                && this.random.nextFloat() < ATTACK_SOUND_CHANCE) {

            this.playSound(
                    ROACWSoundRegistry.DARK_RAVEN_ATTACK.get(),
                    0.7F,
                    0.85F
                            + (
                            this.random.nextFloat()
                                    * 0.20F
                    )
            );

            lastAttackSoundGameTime.put(
                    ownerId,
                    gameTime
            );
        }

        chargeStart =
                this.position();

        /*
         * --------------------------------------------------------------
         * SMALL-TARGET AIMING
         * --------------------------------------------------------------
         *
         * Instead of always aiming at target.getY() + 1.75, use the
         * actual center of the target's bounding box.
         *
         * This is much more reliable for:
         * - Baby Zombies
         * - Endermites
         * - Silverfish
         * - Small slimes
         * - Other short mobs
         */
        AABB targetBox =
                target.getBoundingBox();

        /*
         * Must be set before any of the getMinimumChargeY(...) calls
         * below run, so the ground-clearance clamp knows not to push
         * the aim point/destination above this target's hitbox.
         */
        chargeCeilingY =
                targetBox.maxY;

        Vec3 targetPoint =
                new Vec3(
                        target.getX(),
                        targetBox.getCenter().y,
                        target.getZ()
                );

        /*
         * If the target's bounding box is unusually small or malformed,
         * fall back to the old height-based value.
         */
        if (targetBox.getYsize() < 0.1D) {

            targetPoint =
                    new Vec3(
                            target.getX(),
                            target.getY()
                                    + CHARGE_TARGET_HEIGHT,
                            target.getZ()
                    );
        }

        Vec3 toTarget =
                targetPoint.subtract(
                        this.position()
                );

        Vec3 direction;

        if (toTarget.lengthSqr()
                > 1.0E-4D) {

            direction =
                    toTarget.normalize();

        } else {

            direction =
                    getLookAngle().normalize();
        }

        Vec3 pointAtTarget =
                chargeStart.add(
                        direction.scale(
                                Math.max(
                                        toTarget.length(),
                                        1.0D
                                )
                        )
                );

        double minimumTargetY =
                getMinimumChargeY(
                        pointAtTarget.x,
                        pointAtTarget.y,
                        pointAtTarget.z
                );

        if (minimumTargetY
                != Double.NEGATIVE_INFINITY) {

            pointAtTarget =
                    new Vec3(
                            pointAtTarget.x,
                            Math.max(
                                    pointAtTarget.y,
                                    minimumTargetY
                            ),
                            pointAtTarget.z
                    );
        }

        Vec3 horizontalDirection =
                new Vec3(
                        direction.x,
                        0.0D,
                        direction.z
                );

        if (horizontalDirection.lengthSqr()
                < 1.0E-4D) {

            Vec3 lookHorizontal =
                    new Vec3(
                            getLookAngle().x,
                            0.0D,
                            getLookAngle().z
                    );

            horizontalDirection =
                    lookHorizontal.lengthSqr()
                            > 1.0E-4D
                            ? lookHorizontal.normalize()
                            : new Vec3(
                            1.0D,
                            0.0D,
                            0.0D
                    );

        } else {

            horizontalDirection =
                    horizontalDirection.normalize();
        }

        chargeDestination =
                pointAtTarget.add(
                        horizontalDirection.scale(
                                CHARGE_OVERSHOOT_DISTANCE
                        )
                );

        double minimumDestinationY =
                getMinimumChargeY(
                        chargeDestination.x,
                        chargeDestination.y,
                        chargeDestination.z
                );

        if (minimumDestinationY
                != Double.NEGATIVE_INFINITY) {

            chargeDestination =
                    new Vec3(
                            chargeDestination.x,
                            Math.max(
                                    chargeDestination.y,
                                    minimumDestinationY
                            ),
                            chargeDestination.z
                    );
        }

        float yaw =
                (float) (
                        -Math.toDegrees(
                                Math.atan2(
                                        direction.x,
                                        direction.z
                                )
                        )
                );

        chargeYaw = yaw;

        this.setYRot(yaw);
        this.setYBodyRot(yaw);
        this.setYHeadRot(yaw);

        this.yRotO = yaw;

        this.setDeltaMovement(
                Vec3.ZERO
        );

        this.hurtMarked = true;
    }

    public void stopCharge(
            boolean hit
    ) {

        finishedCharge = true;

        setCharging(false);

        chargeDestination = null;

        chargeStart = null;

        chargeTicks = -1;

        chargeCeilingY = Double.POSITIVE_INFINITY;

        this.getNavigation().stop();

        this.setDeltaMovement(
                Vec3.ZERO
        );

        getRavenMoveControl().stop();

        if (hit) {

            chargeAnimReleaseTicks = 0;

        } else {

            setChargeAnimating(false);

            chargeAnimReleaseTicks = -1;
        }
    }


    @Override
    public boolean doHurtTarget(
            Entity target
    ) {

        float baseDamage =
                (float) this.getAttributeValue(
                        Attributes.ATTACK_DAMAGE
                );

        float damage =
                baseDamage
                        + (
                        this.spellPower
                                * DASH_SPELLPOWER_MULTIPLIER
                );

        DamageSource damageSource =
                this.damageSources()
                        .mobAttack(this);

        boolean success =
                target.hurt(
                        damageSource,
                        damage
                );

        if (success) {
            this.setLastHurtMob(target);
        }

        return success;
    }

    @Override
    public void setLastHurtByMob(
            @Nullable LivingEntity entity
    ) {

        if (entity != null
                && entity == this.getSummonOwner()) {
            return;
        }

        if (entity != null
                && this.isFriendlyRaven(entity)) {
            return;
        }

        super.setLastHurtByMob(entity);
    }

    @Override
    public void onUnSummon() {

        if (this.level()
                instanceof ServerLevel serverLevel) {

            serverLevel.playSound(
                    null,
                    this.blockPosition(),
                    ROACWSoundRegistry.DARK_RAVEN_SUMMON.get(),
                    SoundSource.NEUTRAL,
                    0.7F,
                    1.3F
            );
        }

        this.discard();
    }

    @Override
    public void onRemovedFromWorld() {

        super.onRemovedFromWorld();

        onRemovedHelper(this);
    }

    @Override
    public void die(
            DamageSource damageSource
    ) {

        onDeathHelper();

        super.die(damageSource);
    }

    @Override
    public boolean hurt(
            DamageSource source,
            float amount
    ) {

        if (shouldIgnoreDamage(source)) {
            return false;
        }

        return super.hurt(
                source,
                amount
        );
    }

    @Override
    protected ResourceLocation getDefaultLootTable() {

        return ResourceLocation.fromNamespaceAndPath(
                ROACW.MODID,
                "entities/dark_raven"
        );
    }

    @Override
    public boolean removeWhenFarAway(
            double distanceSq
    ) {
        return false;
    }

    /*
     * ------------------------------------------------------------------
     * GECKOLIB ANIMATION
     * ------------------------------------------------------------------
     */

    @Override
    public void registerControllers(
            AnimatableManager.ControllerRegistrar controllers
    ) {

        controllers.add(
                new AnimationController<>(
                        this,
                        "controller",
                        3,
                        this::animationPredicate
                )
        );
    }

    private PlayState animationPredicate(
            AnimationState<DarkRavenEntity> state
    ) {

        if (this.isChargeAnimating()) {

            state.getController().setAnimation(
                    CHARGE_ANIM
            );

            return PlayState.CONTINUE;
        }

        if (this.isPerched()) {

            state.getController().setAnimation(
                    PERCH_ANIM
            );

            return PlayState.CONTINUE;
        }

        state.getController().setAnimation(
                FLAP_ANIM
        );

        return PlayState.CONTINUE;
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return cache;
    }
}