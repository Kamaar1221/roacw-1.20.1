package net.kamaarion.roacw.entity.summon.plague_charger;

import io.redspace.ironsspellbooks.capabilities.magic.SummonManager;
import io.redspace.ironsspellbooks.entity.mobs.IMagicSummon;
import net.kamaarion.roacw.ROACW;
import net.kamaarion.roacw.entity.projectile.plague_charger_stinger.PlagueChargerStingerEntity;
import net.kamaarion.roacw.registeries.ROACWEffectRegistry;
import net.kamaarion.roacw.registeries.ROACWEntityRegistry;
import net.kamaarion.roacw.registeries.ROACWParticleRegistry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.RangedAttackMob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.AnimationState;
import software.bernie.geckolib.core.animation.RawAnimation;
import software.bernie.geckolib.core.object.PlayState;
import software.bernie.geckolib.util.GeckoLibUtil;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import javax.annotation.Nullable;

public class PlagueChargerEntity extends PathfinderMob implements RangedAttackMob, GeoEntity, IMagicSummon {

    private static final int CHARGE_HIT_FOLLOWTHROUGH_TICKS = 5;

    // isCharging needs to be client-visible - animationPredicate() runs on
    // the client and its triggered-loop fallback reads this. It used to be
    // a plain field, which meant the client's copy never left its default
    // (false), and GeckoLib's triggered thenLoop("charge") can drop out of
    // isPlayingTriggeredAnimation() after its first cycle - when that
    // happened, the predicate fell through past the fallback (client
    // isCharging always false) straight to idle/walking, snapping the
    // model out of the charge pose mid-dash.
    private static final EntityDataAccessor<Boolean> DATA_CHARGING =
            SynchedEntityData.defineId(PlagueChargerEntity.class, EntityDataSerializers.BOOLEAN);

    // Separate from DATA_CHARGING: this drives the charge animation only.
    // GeckoLib's triggerAnim + thenLoop("charge") combo turned out to be
    // unreliable - it plays for a split second then drops (the loop stage
    // stops reporting isPlayingTriggeredAnimation() almost immediately), so
    // the charge pose is now driven directly off this flag via setAnimation()
    // every predicate tick instead of going through the trigger system at
    // all. Stays true a few extra ticks after a successful hit via
    // chargeAnimReleaseTicks so the pose doesn't cut off instantly.
    private static final EntityDataAccessor<Boolean> DATA_SHOW_CHARGE_ANIM =
            SynchedEntityData.defineId(PlagueChargerEntity.class, EntityDataSerializers.BOOLEAN);

    // The "charge" clip is authored as a 1-second/20-tick animation, but the
    // actual dash duration varies with distance to target and is often much
    // shorter (e.g. ~7 ticks at CHARGE_MIN_RANGE), which was cutting the
    // animation off mid-cycle. This scales playback speed at charge-start
    // time so the full windup-to-impact animation always finishes right as
    // the dash does. Adjust CHARGE_ANIM_LENGTH_TICKS if the authored clip
    // length isn't actually 20 ticks / 1 second.
    private static final float CHARGE_ANIM_LENGTH_TICKS = 20.0F;
    private static final EntityDataAccessor<Float> DATA_CHARGE_ANIM_SPEED =
            SynchedEntityData.defineId(PlagueChargerEntity.class, EntityDataSerializers.FLOAT);

    // Distinguishes the windup phase (played at natural 1.0 speed, entity
    // rooted) from the dash phase (played at DATA_CHARGE_ANIM_SPEED, scaled
    // to travel time) - the predicate reads this each tick to pick which
    // speed to apply. Set true server-side the instant the dash actually
    // launches.
    private static final EntityDataAccessor<Boolean> DATA_CHARGE_LAUNCHED =
            SynchedEntityData.defineId(PlagueChargerEntity.class, EntityDataSerializers.BOOLEAN);

    // setAnimation()'s "safe to call every frame" guarantee relies on
    // passing back the SAME RawAnimation instance each time - it checks
    // !rawAnimation.equals(currentRawAnimation), and building a fresh
    // RawAnimation.begin()... inline in the predicate every tick meant that
    // check was true every tick, forcing a transition reset every tick and
    // never letting "charge" advance past its own transition. Caching these
    // once and reusing the reference was the actual fix for the "plays a
    // split second and resets" bug - the speed-scaling below is a real
    // secondary improvement but wasn't the root cause.
    private static final RawAnimation IDLE_ANIM = RawAnimation.begin().thenLoop("idle");
    private static final RawAnimation WALKING_ANIM = RawAnimation.begin().thenLoop("walking");
    private static final RawAnimation CHARGE_ANIM = RawAnimation.begin().thenPlay("charge");

    // Fraction into the "charge" clip where the launch/leap keyframe sits -
    // taken from the "all" bone's position track peaking at t=0.2917 in the
    // animation json. Movement is held at zero until this point in the
    // (speed-scaled) charge so the physical dash launches exactly when that
    // keyframe hits, instead of departing immediately under the crouch pose
    // while the animation is still mid-windup. Re-check this fraction
    // against your authored keyframes if the animation gets re-timed.
    private static final float CHARGE_LAUNCH_KEYFRAME_FRACTION = 0.2917F;

    private static final double CHARGE_SPEED = 1.6D;
    private static final double CHARGE_OVERSHOOT_DISTANCE = 4.0D; // TODO tune - how far past the target it aims
    private static final int CHARGE_MAX_TICKS = 40; // safety cap in case the target is unreachable
    private static final double CHARGE_HIT_RADIUS = 0.35D; // matches the old ChargeGoal's bounding box inflate
    // X formation around the owner: front-right, front-left, back-left,
    // back-right. formationIndex assigns each charger to a slot (see
    // setFormationIndex) and cycles via modulo, so with exactly 4 chargers
    // each one gets its own fixed corner.
    private static final Vec3[] FORMATION_OFFSETS = {

            new Vec3(3.0D, 2.0D, 3.0D),   // front-right
            new Vec3(-3.0D, 2.0D, 3.0D),  // front-left
            new Vec3(-3.0D, 2.0D, -3.0D), // back-left
            new Vec3(3.0D, 2.0D, -3.0D)   // back-right

    };

    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    private final Set<UUID> chargeVictims = new HashSet<>();

    private float spellPower = 2.0F;

    @Nullable
    private LivingEntity chargeTarget;

    @Nullable
    private Vec3 chargeDestination;

    @Nullable
    private Vec3 chargeStart;

    private int chargeTicks = -1;
    private int chargeWindupTicks = 0;
    private int chargeAnimReleaseTicks = -1;
    private boolean finishedCharge = false;
    private int formationIndex = 0;
    private int ticksSinceCombat = 0;
    private float chargeYaw = 0.0F;

    public PlagueChargerEntity(EntityType<? extends PathfinderMob> type, Level level) {
        super(type, level);

        this.setPersistenceRequired();
        this.setNoGravity(true);

        this.moveControl = new PlagueChargerMoveControl(this);
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(DATA_CHARGING, false);
        this.entityData.define(DATA_SHOW_CHARGE_ANIM, false);
        this.entityData.define(DATA_CHARGE_ANIM_SPEED, 1.0F);
        this.entityData.define(DATA_CHARGE_LAUNCHED, false);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 20.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.3D)
                .add(Attributes.FOLLOW_RANGE, 24.0D)
                .add(Attributes.ATTACK_DAMAGE, 4.0D);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(1, new PlagueChargerAttackGoal(this));
        this.goalSelector.addGoal(2, new PlagueChargerWanderGoal(this));
        this.goalSelector.addGoal(3, new LookAtPlayerGoal(this, Player.class, 8.0F));

        this.targetSelector.addGoal(1, new HurtByTargetGoal(this).setAlertOthers());
        // LivingEntity, not Player - attacks any living entity it can see,
        // except the summon owner (if any) and other Plague Chargers on the
        // same side (see isFriendlyCharger()).
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(
                this, LivingEntity.class, 10, true, false,
                target -> target != this.getSummonOwner()
                        && !this.isFriendlyCharger(target)));
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);

        // combatRole and formationIndex are plain fields with no NBT
        // persistence otherwise - on world reload (or SummonManager's
        // offline-player save/restore round trip) they'd silently reset to
        // their class defaults (RANGED, 0), which is why every charger came
        // back as a stinger clumped on the same formation slot.
        tag.putString("CombatRole", this.combatRole.name());
        tag.putInt("FormationIndex", this.formationIndex);
        tag.putFloat("SpellPower", this.spellPower);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);

        if (tag.contains("CombatRole")) {
            try {
                this.combatRole = CombatRole.valueOf(tag.getString("CombatRole"));
            } catch (IllegalArgumentException ignored) {
                // Unknown/corrupt value - fall back to the field default.
            }
        }

        if (tag.contains("FormationIndex")) {
            this.formationIndex = tag.getInt("FormationIndex");
        }

        if (tag.contains("SpellPower")) {
            this.spellPower = tag.getFloat("SpellPower");
        }
    }

    public void setSpellPower(float spellPower) {
        this.spellPower = spellPower;
    }

    public float getSpellPower() {
        return this.spellPower;
    }

    public enum CombatRole {
        CHARGER,
        RANGED
    }

    private CombatRole combatRole = CombatRole.RANGED;

    public CombatRole getCombatRole() {
        return combatRole;
    }

    public void setCombatRole(CombatRole combatRole) {
        this.combatRole = combatRole;
    }

    @Override
    public void setTarget(@Nullable LivingEntity target) {

        // Goals could still periodically re-validate (and re-pick) the
        // current target - including mid-charge. The charge dash itself
        // always flies at the cached chargeTarget field, so an in-flight
        // dash is unaffected, but if getTarget() has been swapped to a
        // different entity by the time the NEXT charge starts, it aims at
        // that new (possibly very differently positioned) target instead of
        // continuing to press the one actually being fought - which can
        // look like the charger reversing direction between dashes. Lock
        // the target entirely while charging: block both nulling it out and
        // swapping it to something else.
        if (this.isCharging() && target != this.getTarget()) {
            return;
        }

        super.setTarget(target);
    }

    public boolean isCharging() {
        return this.entityData.get(DATA_CHARGING);
    }

    private void setCharging(boolean charging) {
        this.entityData.set(DATA_CHARGING, charging);
    }

    private boolean isChargeAnimating() {
        return this.entityData.get(DATA_SHOW_CHARGE_ANIM);
    }

    private void setChargeAnimating(boolean animating) {
        this.entityData.set(DATA_SHOW_CHARGE_ANIM, animating);
    }

    private float getChargeAnimSpeed() {
        return this.entityData.get(DATA_CHARGE_ANIM_SPEED);
    }

    private void setChargeAnimSpeed(float speed) {
        this.entityData.set(DATA_CHARGE_ANIM_SPEED, speed);
    }

    private boolean isChargeLaunched() {
        return this.entityData.get(DATA_CHARGE_LAUNCHED);
    }

    private void setChargeLaunched(boolean launched) {
        this.entityData.set(DATA_CHARGE_LAUNCHED, launched);
    }

    public boolean consumeFinishedCharge() {

        if (!finishedCharge) {
            return false;
        }

        finishedCharge = false;
        return true;
    }

    public int getFormationIndex() {
        return formationIndex;
    }

    public void setFormationIndex(int formationIndex) {
        this.formationIndex = formationIndex;
    }

    public PlagueChargerMoveControl getPlagueMoveControl() {
        return (PlagueChargerMoveControl) this.moveControl;
    }

    public void moveToward(LivingEntity target, double speed) {
        if (target == null) {
            return;
        }

        ((PlagueChargerMoveControl) this.moveControl).moveToward(
                target.getX(),
                target.getY(),
                target.getZ(),
                speed
        );
    }

    public void moveToRange(LivingEntity target, double desiredDistance, double speed) {
        if (target == null) return;

        Vec3 toSelf = this.position().subtract(target.position());

        if (toSelf.lengthSqr() < 0.001D) {
            toSelf = this.getLookAngle();
        }

        Vec3 destination = target.position().add(
                toSelf.normalize().scale(desiredDistance));

        ((PlagueChargerMoveControl) this.moveControl)
                .moveToward(destination.x, destination.y, destination.z, speed);
    }

    public void stopMoving() {
        this.getNavigation().stop();
        this.setDeltaMovement(Vec3.ZERO);
    }

    public void moveAwayFrom(LivingEntity target, double speed) {
        if (target == null) return;

        Vec3 direction = this.position()
                .subtract(target.position())
                .normalize();

        Vec3 destination = this.position().add(direction.scale(6.0D));

        getPlagueMoveControl().moveToward(
                destination.x,
                destination.y,
                destination.z,
                speed
        );
    }

    public void faceTarget(LivingEntity target, float maxTurn) {

        if (target == null) {
            return;
        }

        double dx = target.getX() - this.getX();
        double dz = target.getZ() - this.getZ();

        float targetYaw = (float)(-Math.toDegrees(Math.atan2(dx, dz)));

        float yaw = net.minecraft.util.Mth.rotateIfNecessary(
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

    @Nullable
    public LivingEntity getSummonOwner() {
        var owner = SummonManager.getOwner(this);
        return owner instanceof LivingEntity living ? living : null;
    }

    // Single source of truth for "is this other entity a Plague Charger on
    // my side" - same-owner-status Chargers (both wild, or both summoned
    // regardless of whose summon) don't fight each other; different-owner-
    // status Chargers (one summoned, one wild) do. This same rule used to
    // be hand-copied into the target selector, the dash hit-detection
    // sweep, setLastHurtByMob(), and the stinger projectile's shouldAffect()
    // separately - four independent copies meant a fix to one didn't
    // reach the others, which is exactly how the stinger ended up still
    // excluding every Charger outright after the other three were already
    // fixed. Everything that needs this check should call this method
    // instead of re-deriving it.
    public boolean isFriendlyCharger(Entity other) {
        return other instanceof PlagueChargerEntity that
                && (that.getSummonOwner() != null) == (this.getSummonOwner() != null);
    }

    @Override
    public boolean isPushable() {
        return false;
    }

    @Override
    public void travel(Vec3 travelVector) {
        // PlagueChargerMoveControl fully owns velocity computation. Vanilla's
        // default travel() applies an airborne friction multiplier AFTER
        // movement resolves, which fights our own lerp-toward-target logic -
        // bypass it and just resolve movement/collision with our velocity.
        this.move(MoverType.SELF, this.getDeltaMovement());
        this.calculateEntityAnimation(true);
    }

    @Override
    public boolean causeFallDamage(float fallDistance, float multiplier, DamageSource source) {
        return false;
    }

    @Override
    public void tick() {

        super.tick();

        if (this.level().isClientSide) {
            return;
        }

        if (this.isCharging()) {
            // Forcibly re-apply the locked-in dash heading every tick.
            // Previously this block only synced yBodyRot/yHeadRot to
            // whatever getYRot() currently was, which meant anything that
            // nudged yRot mid-dash (collision with the entity being hit,
            // etc.) would stick and the model would appear to face a
            // different direction than it was actually flying. Pinning to
            // chargeYaw every tick makes the heading immune to that.
            this.setYRot(chargeYaw);
            this.yBodyRot = chargeYaw;
            this.yHeadRot = chargeYaw;
            this.yBodyRotO = chargeYaw;
            this.yHeadRotO = chargeYaw;
            this.yRotO = chargeYaw;
        }

        // NearestAttackableTargetGoal used to be the thing that periodically
        // cleared a dead/invalid target back to null. Now that it's gone,
        // nothing else does - getTarget() would otherwise keep pointing at
        // a dead entity forever, which blocks WanderGoal (canUse() requires
        // getTarget() == null) and everything else gated on being "out of
        // combat" from ever kicking in.
        if (this.getTarget() != null && !this.getTarget().isAlive()) {
            this.setTarget(null);
        }

        // Follow the summoner's current target - but only if it was actually
        // hurt recently. getLastHurtMob() has no time limit; without this
        // check, any still-alive thing the owner ever hit (possibly minutes
        // ago) gets re-adopted the instant getTarget() goes null, which
        // means PlagueChargerAttackGoal keeps preempting
        // PlagueChargerWanderGoal (same MOVE flag, higher priority) and the
        // charger never gets a real chance to fly back to formation.
        LivingEntity owner = getSummonOwner();
        if (owner != null && this.getTarget() == null) {

            LivingEntity ownerTarget = owner.getLastHurtMob();

            boolean recentlyHurt = owner.tickCount - owner.getLastHurtMobTimestamp()
                    <= OWNER_TARGET_FRESHNESS_TICKS;

            if (ownerTarget != null
                    && recentlyHurt
                    && ownerTarget.isAlive()
                    && this.distanceToSqr(ownerTarget) < 900.0D) {

                this.setTarget(ownerTarget);
            }
        }

        // Track time since we were last actually in combat (had a target).
        // Regen below waits until OUT_OF_COMBAT_GRACE_TICKS have passed since
        // this hit zero, rather than firing the instant getTarget() happens
        // to go null.
        if (this.getTarget() != null) {
            ticksSinceCombat = 0;
        } else if (ticksSinceCombat < OUT_OF_COMBAT_GRACE_TICKS) {
            ticksSinceCombat++;
        }

        // Regenerate health while out of combat (no current target, not
        // mid-charge, and past the post-combat grace period).
        if (!this.isCharging()
                && this.getTarget() == null
                && ticksSinceCombat >= OUT_OF_COMBAT_GRACE_TICKS
                && this.getHealth() < this.getMaxHealth()
                && this.tickCount % OUT_OF_COMBAT_REGEN_INTERVAL_TICKS == 0) {

            this.heal(OUT_OF_COMBAT_REGEN_AMOUNT);
        }

        // Keeps the charge pose alive briefly after a successful hit
        // (stopCharge(true) sets chargeAnimReleaseTicks = 0 to start this
        // countdown). Runs unconditionally, independent of isCharging, since by
        // the time this needs to fire, isCharging is already false.
        if (chargeAnimReleaseTicks >= 0) {

            chargeAnimReleaseTicks++;

            if (chargeAnimReleaseTicks >= CHARGE_HIT_FOLLOWTHROUGH_TICKS) {

                setChargeAnimating(false);

                chargeAnimReleaseTicks = -1;
            }
        }

        if (!this.isCharging()) {
            return;
        }

        chargeTicks++;

        if (chargeTicks < chargeWindupTicks) {
            // Still in the windup portion, played at natural speed - stay
            // rooted so the dash departs exactly on the launch keyframe
            // rather than under the crouch pose.
            this.setDeltaMovement(Vec3.ZERO);
            return;
        }

        if (this.getDeltaMovement().lengthSqr() < 1.0E-4D) {

            Vec3 direction = chargeDestination.subtract(this.position());

            if (direction.lengthSqr() > 1.0E-4D) {
                this.setDeltaMovement(direction.normalize().scale(CHARGE_SPEED));
            }
        }

        // Windup just ended and the dash is now actually moving - switch
        // the predicate over to the travel-scaled speed from here.
        setChargeLaunched(true);

        Vec3 motion = this.getDeltaMovement();

        // 1.6 blocks/tick is well above normal mob speed, and Entity#move's
        // per-tick AABB sweep isn't true continuous collision - at this
        // speed it can visibly tunnel through thin obstacles (fences, thin
        // walls) in a single step even though collision is technically on.
        // Raycasting one tick ahead and stopping cleanly on a hit avoids
        // that, on top of the line-of-sight check that already prevents
        // charges from starting through a wall in the first place.
        HitResult obstruction = this.level().clip(new ClipContext(
                this.position(),
                this.position().add(motion),
                ClipContext.Block.COLLIDER,
                ClipContext.Fluid.NONE,
                this));

        if (obstruction.getType() != HitResult.Type.MISS) {
            stopCharge(false);
            return;
        }

        AABB dashBox = this.getBoundingBox()
                .expandTowards(motion)
                .inflate(0.5D);

        for (LivingEntity entity : this.level().getEntitiesOfClass(
                LivingEntity.class,
                dashBox,
                e -> e != this
                        && e != getSummonOwner()
                        && !this.isFriendlyCharger(e)
                        && e.isAlive())) {

            if (!chargeVictims.add(entity.getUUID())) {
                continue;
            }

            this.doHurtTarget(entity);
        }

// Stop once we've flown through the destination.
        Vec3 chargeLine = chargeDestination.subtract(chargeStart);
        Vec3 current = this.position().subtract(chargeStart);

        if (current.dot(chargeLine) >= chargeLine.lengthSqr()) {
            stopCharge(false);
            return;
        }

        if (chargeTicks >= CHARGE_MAX_TICKS) {
            stopCharge(false);
        }
    }

    public void startCharge(LivingEntity target) {

        if (target == null) {
            System.out.println("startCharge -> target NULL");
            return;
        }

        if (!target.isAlive()) {
            System.out.println("startCharge -> target DEAD");
            return;
        }

        if (this.isCharging()) {
            System.out.println("startCharge -> already charging");
            return;
        }

        System.out.println("START CHARGE: " + this.getUUID());

        // Clear any leftover MOVE_TO state (e.g. from the pre-charge
        // repositioning in PlagueChargerAttackGoal) so nothing stale can
        // carry into the dash.
        getPlagueMoveControl().stop();

        chargeVictims.clear();

        chargeTarget = target;
        chargeTicks = 0;
        setCharging(true);
        setChargeAnimating(true);
        setChargeLaunched(false);

        chargeStart = this.position();

        Vec3 toTarget = target.position().subtract(position());

        Vec3 direction = toTarget.lengthSqr() > 1.0E-4D
                ? toTarget.normalize()
                : getLookAngle().normalize();

        double travelDistance =
                Math.max(toTarget.length(), 1.0D) + CHARGE_OVERSHOOT_DISTANCE;

        chargeDestination = chargeStart.add(direction.scale(travelDistance));

        // Windup is a fixed duration played at natural (1.0) speed,
        // decoupled from travel distance, so short charges still get a
        // proper telegraph instead of a near-instant flash. Only the
        // post-windup dash portion of the clip gets scaled to match travel
        // time.
        chargeWindupTicks = Math.round(CHARGE_LAUNCH_KEYFRAME_FRACTION * CHARGE_ANIM_LENGTH_TICKS);

        double expectedTravelTicks = Math.max(travelDistance / CHARGE_SPEED, 1.0D);
        float dashAnimTicksRemaining = CHARGE_ANIM_LENGTH_TICKS - chargeWindupTicks;
        setChargeAnimSpeed((float)(dashAnimTicksRemaining / expectedTravelTicks));

        // Face the direction of the dash immediately.
        float yaw = (float)(-Math.toDegrees(Math.atan2(direction.x, direction.z)));
        chargeYaw = yaw;
        this.setYRot(yaw);
        this.setYBodyRot(yaw);
        this.setYHeadRot(yaw);
        this.yRotO = yaw;

        // Hold in place through the windup - tick() launches the actual
        // dash once chargeTicks reaches chargeWindupTicks, timed to the
        // animation's launch keyframe.
        this.setDeltaMovement(Vec3.ZERO);
        this.hurtMarked = true;
    }

    public void stopCharge(boolean hit) {

        System.out.println("STOP CHARGE: " + this.getUUID() + " hit=" + hit);

        finishedCharge = true;

        setCharging(false);

        chargeTarget = null;
        chargeDestination = null;
        chargeStart = null;

        chargeTicks = -1;

        this.getNavigation().stop();

        this.setDeltaMovement(Vec3.ZERO);

        getPlagueMoveControl().stop();

        if (moveControl instanceof PlagueChargerMoveControl control) {
            control.stop();
        }

        setDeltaMovement(Vec3.ZERO);

        if (hit) {
            chargeAnimReleaseTicks = 0;
        } else {
            setChargeAnimating(false);
            chargeAnimReleaseTicks = -1;
        }
    }

    @Override
    public boolean doHurtTarget(Entity target) {

        float baseDamage = (float) this.getAttributeValue(Attributes.ATTACK_DAMAGE);
        float damage = baseDamage + (this.spellPower * DASH_SPELLPOWER_MULTIPLIER);

        DamageSource damageSource = this.damageSources().mobAttack(this);

        boolean success = target.hurt(damageSource, damage);

        if (success) {
            this.setLastHurtMob(target);

            if (target instanceof LivingEntity livingTarget) {
                livingTarget.addEffect(new MobEffectInstance(
                        ROACWEffectRegistry.PLAGUE.get(),
                        DASH_PLAGUE_DURATION_TICKS,
                        DASH_PLAGUE_AMPLIFIER,
                        false,
                        true));
            }
        }

        return success;
    }

    @Override
    public void performRangedAttack(LivingEntity target, float distanceFactor) {
        if (this.level() instanceof ServerLevel serverLevel) {

            LivingEntity owner = getSummonOwner();

            Vec3 look = this.getLookAngle().normalize();
            Vec3 right = new Vec3(-look.z, 0.0D, look.x).normalize();
            Vec3 spawnPos = this.position()
                    .add(0.0D, 0.55D, 0.0D)
                    .add(right.scale(0.15D))
                    .add(look.scale(0.65D));

            Vec3 direction = target.getEyePosition().subtract(spawnPos).normalize();

            PlagueChargerStingerEntity stinger = new PlagueChargerStingerEntity(
                    ROACWEntityRegistry.PLAGUE_CHARGER_STINGER.get(), this.level(),
                    this, owner, spawnPos, direction);
            stinger.setOldPosAndRot();
            stinger.setDamage(this.spellPower * STINGER_DAMAGE_MULTIPLIER);

            serverLevel.playSound(null, this.blockPosition(), SoundEvents.GENERIC_EXPLODE,
                    SoundSource.NEUTRAL, 0.4F, 1.6F);
            serverLevel.addFreshEntity(stinger);

        }
    }

    @Override
    public void onUnSummon() {
        if (this.level() instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(ROACWParticleRegistry.PLAGUE_CLOUD.get(),
                    this.getX(), this.getY(1.0D), this.getZ(),
                    24, 0.3D, 0.4D, 0.3D, 0.02D);
            serverLevel.playSound(null, this.blockPosition(), SoundEvents.WITHER_SKELETON_AMBIENT, // TODO placeholder sound
                    SoundSource.NEUTRAL, 0.7F, 0.6F);
        }
        this.discard();
    }

    @Override
    public void onRemovedFromWorld() {
        super.onRemovedFromWorld();
        onRemovedHelper(this);
    }

    @Override
    public void die(DamageSource damageSource) {
        onDeathHelper();
        super.die(damageSource);
    }

    // Only re-adopt the owner's last-hurt-mob as our target if they hit it within this many ticks.
    private static final int OWNER_TARGET_FRESHNESS_TICKS = 100; // 5s

    // Multiplier applied to incoming damage while mid-charge (0.5 = 50% reduction).
    private static final float CHARGE_DAMAGE_REDUCTION_MULTIPLIER = 0.5F;

    // Dash damage = ATTACK_DAMAGE attribute + (spellPower * this). spellPower
    // is whatever the summoning spell passed via setSpellPower() - if that
    // value already reflects dual-school scaling from the original spell
    // class, the dash inherits it automatically.
    private static final float DASH_SPELLPOWER_MULTIPLIER = 1.0F;

    // Out-of-combat regen: heal this much every this many ticks while there's no current target.
    private static final int OUT_OF_COMBAT_REGEN_INTERVAL_TICKS = 20; // once per second
    private static final float OUT_OF_COMBAT_REGEN_AMOUNT = 0.5F;
    private static final int OUT_OF_COMBAT_GRACE_TICKS = 100; // 5s after last hit/target before regen starts

    // Multiplier applied to spellPower to get the stinger's damage.
    private static final float STINGER_DAMAGE_MULTIPLIER = 1.0F;

    // Plague applied on a successful dash hit. Registry field name
    // (ROACWEffectRegistry.PLAGUE) and amplifier scale not verified against
    // your actual PlagueEffect/registry source - flag if these don't match.
    // Kept as a flat amplifier rather than routing through
    // Utils.getNatureScaledPlagueAmplifier() since the charger isn't
    // casting a spell here, just landing a physical hit - let me know if
    // you'd rather it scale off spellPower the same way damage does.
    private static final int DASH_PLAGUE_DURATION_TICKS = 200; // 10s
    private static final int DASH_PLAGUE_AMPLIFIER = 0;

    @Override
    public boolean hurt(DamageSource source, float amount) {
        if (shouldIgnoreDamage(source)) {
            return false;
        }
        ticksSinceCombat = 0;
        if (this.isCharging()) {
            amount *= CHARGE_DAMAGE_REDUCTION_MULTIPLIER;
        }
        return super.hurt(source, amount);
    }

    // HurtByTargetGoal reads getLastHurtByMob() to decide who to retaliate
    // against - skipping the record here (rather than special-casing inside
    // a subclassed goal, which would mean guessing at HurtByTargetGoal's
    // internals) means a Charger simply never learns its owner, or a
    // same-side Charger, counts as "something that hurt me".
    @Override
    public void setLastHurtByMob(@Nullable LivingEntity entity) {
        if (entity != null && entity == this.getSummonOwner()) {
            return;
        }
        if (entity != null && this.isFriendlyCharger(entity)) {
            return;
        }
        super.setLastHurtByMob(entity);
    }

    public Vec3 getFormationOffset() {
        return FORMATION_OFFSETS[
                formationIndex % FORMATION_OFFSETS.length];
    }

    // Points at data/roacw/loot_table/entities/plague_charger.json - needs
    // creating separately, this only wires the entity to look for it.
    @Override
    protected ResourceLocation getDefaultLootTable() {
        return ResourceLocation.fromNamespaceAndPath(ROACW.MODID, "entities/plague_charger");
    }

    @Override
    public boolean removeWhenFarAway(double distanceSq) {
        return false;
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        AnimationController<PlagueChargerEntity> controller =
                new AnimationController<>(this, "controller", 5, this::animationPredicate);
        controller.triggerableAnim("instant_cast", RawAnimation.begin().thenPlay("instant_cast"));
        controllers.add(controller);
    }

    private PlayState animationPredicate(AnimationState<PlagueChargerEntity> state) {

        // Driven directly off the synced flag rather than GeckoLib's trigger
        // system.
        if (this.isChargeAnimating()) {
            // API name/signature not double-checked against your GeckoLib
            // version - flag if setAnimationSpeed doesn't exist/match.
            double speed = this.isChargeLaunched() ? this.getChargeAnimSpeed() : 1.0D;
            state.getController().setAnimationSpeed(speed);
            state.getController().setAnimation(CHARGE_ANIM);
            return PlayState.CONTINUE;
        }

        state.getController().setAnimationSpeed(1.0D);

        if (state.getController().isPlayingTriggeredAnimation()) {
            return PlayState.CONTINUE;
        }

        if (state.isMoving()) {
            state.getController().setAnimation(WALKING_ANIM);
        } else {
            state.getController().setAnimation(IDLE_ANIM);
        }

        return PlayState.CONTINUE;
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return cache;
    }
}