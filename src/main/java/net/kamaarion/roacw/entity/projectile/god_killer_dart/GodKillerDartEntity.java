package net.kamaarion.roacw.entity.projectile.god_killer_dart;

import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ambient.AmbientCreature;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.animal.WaterAnimal;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractHurtingProjectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.kamaarion.roacw.registeries.ROACWEffectRegistry;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.util.GeckoLibUtil;

import javax.annotation.Nullable;
import java.util.List;

/**
 * God Killer Dart — homing projectile.
 * Follows the same pattern as PlagueRocketEntity/PlagueNukeEntity: AbstractHurtingProjectile +
 * getInertia() alone controlling speed, with a blended-direction homing tick
 * layered on top.
 */
public class GodKillerDartEntity extends AbstractHurtingProjectile implements GeoEntity {

    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    // -1 == no target. Synced so the client can also orient the model correctly.
    private static final EntityDataAccessor<Integer> TARGET_ID =
            SynchedEntityData.defineId(GodKillerDartEntity.class, EntityDataSerializers.INT);

    // Max angle the dart can turn toward its target per tick. Using a true
    // angular turn (Rodrigues' rotation) rather than a vector blend, since
    // blending unit vectors and renormalizing under-corrects badly for large
    // deviation angles (e.g. right after a wide spread-cone launch) and only
    // "catches up" once the dart is already close and the angle is small.
    private static final float MAX_TURN_DEGREES_PER_TICK = 14.0F;
    private static final int MAX_LIFE_TICKS = 200;
    private static final float DAMAGE = 8.0F;

    // Ticks the dart flies straight before homing engages — gives a burst of
    // darts room to visibly fan out before they curve back onto targets.
    private static final int HOMING_DELAY_TICKS = 6;

    // A curving homing dart can pass right beside its target without the
    // vanilla per-tick movement-segment raycast ever registering a hit —
    // especially mid-turn. This forces a direct hit once the dart gets
    // within range, instead of relying purely on that raycast.
    private static final double HIT_RADIUS = 1.2;

    private static final int INFERNO_DURATION_TICKS = 100; // 5s
    private static final int INFERNO_AMPLIFIER = 0;

    // If the target ends up behind the dart's direction of travel by more
    // than this angle, the dart has clearly overshot — rather than forcing
    // MAX_TURN_DEGREES_PER_TICK to grind through a near-180° correction
    // (which reads as a violent snap/flick rather than a turn), the dart
    // fizzles out instead of fighting for an ugly recovery.
    private static final double OVERSHOOT_ANGLE_DEGREES = 100.0;

    // Overshoot is only checked within this range of the target. Darts
    // launched behind the player routinely start with a huge angle to
    // whatever's in front of them — that's an expected, intentional
    // turn-around, not a miss, and shouldn't fizzle. A large angle only
    // means "overshot" once the dart is actually near the target and has
    // flown past it.
    private static final double OVERSHOOT_CHECK_RANGE = 4.0;

    // Darts that never lock onto anything at launch would otherwise fly in
    // a straight line for the dart's full MAX_LIFE_TICKS — at full speed
    // that's ~280 blocks. Give untargeted darts a much shorter leash. This
    // is the real "aimless flight" backstop — everything below is about
    // maximizing the odds a dart never actually needs to hit this cap.
    private static final int NO_TARGET_MAX_LIFE_TICKS = 100; // 5s

    // How far and how often a targetless dart searches for something to
    // lock onto, so an enemy that wanders into its path mid-flight still
    // gets picked up rather than being ignored. The radius grows the
    // longer the dart goes without a target, so a dart that finds nothing
    // close by keeps casting a wider net instead of giving up at a fixed
    // bubble — capped so search cost doesn't grow unbounded.
    private static final double RETARGET_SEARCH_RADIUS_BASE = 10.0;
    private static final double RETARGET_SEARCH_RADIUS_GROWTH_PER_SECOND = 6.0;
    private static final double RETARGET_SEARCH_RADIUS_MAX = 40.0;
    private static final int RETARGET_INTERVAL_TICKS = 5;

    // Visual roll into turns, purely cosmetic (doesn't affect flight physics).
    private static final float MAX_BANK_ANGLE = 40.0F;
    private static final float BANK_TURN_SCALE = 2.5F; // degrees of bank per degree of turn
    private static final float BANK_SMOOTHING = 0.25F; // 0-1, higher = snappier

    @Nullable
    private LivingEntity target;
    private int life = 0;
    private float bankAngle = 0F;

    public GodKillerDartEntity(EntityType<? extends AbstractHurtingProjectile> type, Level level) {
        super(type, level);
    }

    /**
     * Spawns the dart at the shooter's eye position. Direction/speed come
     * from setDeltaMovement() after construction — don't rely on the
     * super(shooter, ...) constructor's built-in direction, since it
     * normalizes to a fixed 0.1 power regardless of what you pass in.
     */
    public GodKillerDartEntity(EntityType<? extends AbstractHurtingProjectile> type, LivingEntity shooter, Level level) {
        super(type, shooter.getX(), shooter.getEyeY() - 0.2, shooter.getZ(), 0, 0, 0, level);
        this.setOwner(shooter);
    }

    public void setTarget(@Nullable LivingEntity target) {
        this.target = target;
        this.entityData.set(TARGET_ID, target == null ? -1 : target.getId());
    }

    @Nullable
    public LivingEntity getTarget() {
        if (this.target == null && this.level() != null) {
            int id = this.entityData.get(TARGET_ID);
            if (id != -1 && this.level().getEntity(id) instanceof LivingEntity le) {
                this.target = le;
            }
        }
        return this.target;
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(TARGET_ID, -1);
    }

    @Override
    public void tick() {
        // super.tick() applies gravity-exemption/inertia/motion using the
        // deltaMovement we set last tick, then calls onHit() if it collided.
        super.tick();

        if (!this.level().isClientSide) {
            life++;
            if (life > MAX_LIFE_TICKS) {
                this.discard();
                return;
            }

            LivingEntity currentTarget = getTarget();
            if (currentTarget != null && !currentTarget.isAlive()) {
                // Cached target died — clear it so we fall through to
                // reacquisition below instead of homing on a dead reference.
                setTarget(null);
                currentTarget = null;
            }

            if (currentTarget == null && life % RETARGET_INTERVAL_TICKS == 0) {
                currentTarget = tryAcquireTarget();
            }

            if (currentTarget != null && life > HOMING_DELAY_TICKS) {
                homeTowards(currentTarget);
                checkProximityHit(currentTarget);
            } else {
                bankAngle = Mth.lerp(BANK_SMOOTHING, bankAngle, 0F);

                if (currentTarget == null && life > NO_TARGET_MAX_LIFE_TICKS) {
                    this.discard();
                    return;
                }
            }
        }
    }

    /**
     * Searches a radius around the dart's current position for a living,
     * non-allied target and locks on if one is found. Throttled to every
     * RETARGET_INTERVAL_TICKS rather than every tick to avoid running an
     * entity search 20x/second per dart.
     */
    @Nullable
    private LivingEntity tryAcquireTarget() {
        double elapsedSeconds = this.life / 20.0;
        double searchRadius = Math.min(
                RETARGET_SEARCH_RADIUS_MAX,
                RETARGET_SEARCH_RADIUS_BASE + elapsedSeconds * RETARGET_SEARCH_RADIUS_GROWTH_PER_SECOND
        );

        AABB searchArea = new AABB(this.position(), this.position()).inflate(searchRadius);

        List<LivingEntity> candidates = this.level().getEntitiesOfClass(
                LivingEntity.class, searchArea,
                entity -> entity.isAlive()
                        && entity != this.getOwner()
                        && (this.getOwner() == null || !this.getOwner().isAlliedTo(entity))
                        && isViableHostileTarget(entity)
        );

        LivingEntity nearest = null;
        double nearestDistSq = Double.MAX_VALUE;

        for (LivingEntity candidate : candidates) {
            double distSq = candidate.distanceToSqr(this);
            if (distSq < nearestDistSq) {
                nearestDistSq = distSq;
                nearest = candidate;
            }
        }

        if (nearest != null) {
            setTarget(nearest);
        }

        return nearest;
    }

    /**
     * Excludes passive/non-combat mobs (animals, villagers, players) from
     * being valid retarget candidates. Without this, a dart that fails to
     * find its intended target can lock onto a nearby cow or villager
     * instead — and since it then has a "valid" target, the short
     * NO_TARGET_MAX_LIFE_TICKS expiry never kicks in, so it just calmly
     * chases livestock for the full MAX_LIFE_TICKS instead of despawning.
     *
     * NOTE: this also excludes Player as a target, on the assumption this
     * ability isn't meant to be PvP-capable — flip that if it should be.
     * I don't know your full custom mob roster, so this only excludes
     * vanilla's passive-mob base classes; any custom passive ROACW/ISS
     * entities that don't extend Animal/WaterAnimal/AmbientCreature would
     * slip through this filter and may need their own exclusion.
     */
    private static boolean isViableHostileTarget(LivingEntity entity) {
        return !(entity instanceof Animal)
                && !(entity instanceof WaterAnimal)
                && !(entity instanceof AmbientCreature)
                && !(entity instanceof Villager)
                && !(entity instanceof Player);
    }

    /**
     * Forces a hit once the dart is close enough to its target, rather than
     * relying solely on the vanilla movement-segment raycast — see
     * HIT_RADIUS comment above.
     */
    private void checkProximityHit(LivingEntity target) {
        if (this.distanceToSqr(target) <= HIT_RADIUS * HIT_RADIUS) {
            dealHitAndDiscard(target);
        }
    }

    private void homeTowards(LivingEntity target) {
        Vec3 toTarget = target.getBoundingBox().getCenter()
                .subtract(this.position())
                .normalize();

        Vec3 currentMotion = this.getDeltaMovement();
        double speed = currentMotion.length();
        if (speed < 1.0E-5) {
            speed = 0.5; // fallback if somehow zeroed out
        }

        Vec3 normalizedCurrent = currentMotion.normalize();

        double angleToTargetDegrees = Math.toDegrees(
                Math.acos(Mth.clamp(normalizedCurrent.dot(toTarget), -1.0, 1.0))
        );

        boolean nearTarget = this.distanceToSqr(target) <= OVERSHOOT_CHECK_RANGE * OVERSHOOT_CHECK_RANGE;

        if (nearTarget && angleToTargetDegrees > OVERSHOOT_ANGLE_DEGREES) {
            fizzle();
            return;
        }

        Vec3 newDirection = rotateTowards(normalizedCurrent, toTarget, Math.toRadians(MAX_TURN_DEGREES_PER_TICK));

        updateBankAngle(normalizedCurrent, newDirection);

        this.setDeltaMovement(newDirection.scale(speed));

        // Keep rotation in sync with motion for GeckoLib rendering / hitbox facing.
        this.setYRot((float) (Mth.atan2(newDirection.x, newDirection.z) * (180F / Math.PI)));
        this.setXRot((float) (Mth.atan2(newDirection.y, newDirection.horizontalDistance()) * (180F / Math.PI)));
        this.yRotO = this.getYRot();
        this.xRotO = this.getXRot();
    }

    /**
     * A graceful miss: quick puff + spark and despawn, no damage, rather
     * than an abrupt vanish or forcing an ugly loop-back correction.
     *
     * NOTE: SoundEvents.ITEM_BREAK is a vanilla placeholder — swap for a
     * ROACWSoundRegistry entry if you've got (or want) a dedicated fizzle
     * sound for this dart.
     */
    private void fizzle() {
        if (this.level() instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(
                    ParticleTypes.SMOKE,
                    this.getX(), this.getY(), this.getZ(),
                    8, 0.1, 0.1, 0.1, 0.02
            );
        }
        this.level().playSound(
                null,
                this.getX(), this.getY(), this.getZ(),
                SoundEvents.ITEM_BREAK,
                SoundSource.NEUTRAL,
                0.5F, 1.4F
        );
        this.discard();
    }

    /**
     * Rotates `from` toward `to` by at most `maxAngleRadians`, both assumed
     * to be unit vectors. Unlike a linear blend-and-renormalize, this turns
     * a consistent angle regardless of how large the deviation is, so a
     * dart launched pointing well away from its target corrects at the same
     * rate as one that's already nearly on-target.
     */
    private static Vec3 rotateTowards(Vec3 from, Vec3 to, double maxAngleRadians) {
        double dot = Mth.clamp(from.dot(to), -1.0, 1.0);
        double angle = Math.acos(dot);

        if (angle <= maxAngleRadians || angle < 1.0E-6) {
            return to;
        }

        Vec3 axis = from.cross(to);
        if (axis.lengthSqr() < 1.0E-9) {
            // from/to are exactly opposite — pick an arbitrary perpendicular axis.
            Vec3 fallback = Math.abs(from.x) < 0.9 ? new Vec3(1, 0, 0) : new Vec3(0, 1, 0);
            axis = fallback.cross(from);
        }
        axis = axis.normalize();

        // Rodrigues' rotation formula.
        double cos = Math.cos(maxAngleRadians);
        double sin = Math.sin(maxAngleRadians);
        Vec3 term1 = from.scale(cos);
        Vec3 term2 = axis.cross(from).scale(sin);
        Vec3 term3 = axis.scale(axis.dot(from) * (1.0 - cos));
        return term1.add(term2).add(term3).normalize();
    }

    /**
     * Derives a signed horizontal turn amount between the old and new
     * flight direction, then eases bankAngle toward a proportional roll —
     * sharper turns bank harder, up to MAX_BANK_ANGLE. Purely cosmetic.
     *
     * NOTE: the sign convention for "banks right" vs "banks left" is
     * unverified against actual rendered output — flip the sign ternary
     * below if it rolls the wrong way in-game.
     */
    private void updateBankAngle(Vec3 oldDirection, Vec3 newDirection) {
        double cross = oldDirection.x * newDirection.z - oldDirection.z * newDirection.x;
        double dot = Mth.clamp(oldDirection.x * newDirection.x + oldDirection.z * newDirection.z, -1.0, 1.0);
        double turnDegrees = Math.toDegrees(Math.acos(dot));

        float sign = cross > 0 ? -1F : 1F;
        float targetBank = Mth.clamp((float) (sign * turnDegrees * BANK_TURN_SCALE), -MAX_BANK_ANGLE, MAX_BANK_ANGLE);

        bankAngle = Mth.lerp(BANK_SMOOTHING, bankAngle, targetBank);
    }

    public float getBankAngle() {
        return bankAngle;
    }

    @Override
    protected void onHitEntity(EntityHitResult result) {
        super.onHitEntity(result);
        if (!this.level().isClientSide && result.getEntity() instanceof LivingEntity hit) {
            dealHitAndDiscard(hit);
        }
    }

    private void dealHitAndDiscard(LivingEntity hit) {
        if (this.level().isClientSide || this.isRemoved()) {
            return;
        }
        // Multiple darts often converge on the same target within the same
        // fraction of a second. Without this, only the first dart's hit
        // actually deals damage — the rest arrive during that target's
        // post-hit invulnerability window and get silently no-op'd by
        // hurt(), even though they still detect the hit and discard.
        hit.invulnerableTime = 0;
        hit.hurt(
                this.damageSources().mobProjectile(this, getOwner() instanceof LivingEntity le ? le : null),
                DAMAGE
        );
        hit.addEffect(new MobEffectInstance(
                ROACWEffectRegistry.GOD_SLAYER_INFERNO.get(),
                INFERNO_DURATION_TICKS,
                INFERNO_AMPLIFIER,
                false,
                false
        ));
        this.discard();
    }

    /**
     * Deliberately does NOT auto-discard on block-type hits. AbstractHurtingProjectile's
     * vanilla movement/collision raycast runs inside super.tick(), which fires
     * before our own homing and proximity-hit logic even executes this tick —
     * so a dart actively chasing a grounded target will almost always graze
     * the ground a moment before it's close enough to register as an entity
     * hit, and get discarded by terrain instead of actually connecting. A
     * grace-period delay doesn't fix this since it's not a spawn-time issue.
     * Instead, this dart only ends via onHitEntity, checkProximityHit, or
     * the MAX_LIFE_TICKS / NO_TARGET_MAX_LIFE_TICKS timeouts — it's allowed
     * to clip through terrain while actively homing.
     */
    @Override
    protected void onHit(HitResult result) {
        super.onHit(result);
    }

    @Override
    protected ParticleOptions getTrailParticle() {
        return ParticleTypes.SMOKE;
    }

    @Override
    protected boolean shouldBurn() {
        return false;
    }

    @Override
    public boolean isNoGravity() {
        return true;
    }

    @Override
    protected float getInertia() {
        // Vanilla AbstractHurtingProjectile decays speed by default. Without
        // this override the dart gradually slows to a crawl, then our
        // near-zero-speed fallback in homeTowards() snaps it back to full
        // speed — visible as an "idle, then flip back onto target" glitch.
        return 1.0F;
    }

    // --- GeoAnimatable ---

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        // No animation controller yet — static model riding entity rotation.
        // Add one here (e.g. a spin/idle controller) once the dart has animations.
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.cache;
    }
}