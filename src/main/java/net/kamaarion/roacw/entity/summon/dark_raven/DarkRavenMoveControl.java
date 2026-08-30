package net.kamaarion.roacw.entity.summon.dark_raven;

import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.ai.control.MoveControl;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

/**
 * Impulse-based flight control for the Dark Raven.
 *
 * The raven accelerates toward the position supplied by DarkRavenPerchGoal
 * rather than instantly moving toward it. Light friction allows old momentum
 * to bleed off when the destination changes.
 *
 * The movement also contains a small amount of natural flight variation:
 * - slight speed variation
 * - gentle horizontal/vertical steering variation
 * - softer braking when approaching a destination
 *
 * Rotation is NOT handled here. Dark Raven owns rotation elsewhere.
 */
public class DarkRavenMoveControl extends MoveControl {

    private final DarkRavenEntity raven;

    /*
     * How much of the previous tick's velocity survives each tick.
     *
     * Higher = smoother/more floaty.
     * Lower = more responsive.
     */
    private static final double VELOCITY_RETENTION = 0.92D;

    /*
     * How strongly the raven accelerates toward its destination.
     */
    private static final double ACCELERATION = 0.05D;

    /*
     * Amount of natural flight variation.
     *
     * These are intentionally small. The raven should still reliably
     * reach the destination chosen by DarkRavenPerchGoal.
     */
    private static final double HORIZONTAL_DRIFT = 0.012D;
    private static final double VERTICAL_DRIFT = 0.008D;

    /*
     * How quickly the random flight drift changes.
     *
     * We don't want a completely new random direction every tick because
     * that would look jittery. Instead, the values smoothly change over
     * time.
     */
    private static final double DRIFT_CHANGE_SPEED = 0.08D;

    /*
     * Maximum amount of speed variation.
     *
     * 0.15 means the raven's acceleration can vary by roughly ±15%.
     */
    private static final double SPEED_VARIATION = 0.15D;

    /*
     * ---------------------------------------------------------------
     * OBSTACLE CLIMBING
     * ---------------------------------------------------------------
     *
     * Ported from PlagueChargerMoveControl. Without this, the raven
     * flies in a dead straight line toward wherever
     * DarkRavenPerchGoal points it - fine over open air, but
     * findPerchSpot() now reaches for rooftops/branches/ledges up to
     * PERCH_RADIUS away, which routinely means going up and over a
     * wall or tree trunk to get there. Without any obstacle handling,
     * the raven just rams the side of it and gets stuck, unable to
     * reach the perch it chose.
     *
     * Casts a ray along the horizontal direction of travel each tick;
     * if it hits something, scans upward from the hit to find the
     * obstruction's actual top and ramps a smoothed vertical bias
     * toward clearing it, rather than reacting only once collision is
     * already happening.
     */
    private static final double LOOKAHEAD_DISTANCE = 2.5D;
    private static final double CLEARANCE_BUFFER = 1.0D; // smaller than the charger's 1.25 - raven's hitbox is much smaller
    private static final int OBSTRUCTION_SCAN_HEIGHT = 6;
    private static final double AVOIDANCE_LERP = 0.12D;
    private static final double COLLISION_FALLBACK_AVOIDANCE = 0.20D;

    // PlagueChargerEntity mostly moves level, so its move control never
    // needed to distinguish "obstacle ahead" from "intentionally
    // descending." Dark Raven regularly needs to descend substantially to
    // reach a perch spot (ground-level or landing ON TOP of something
    // elevated) - without this guard, the lookahead below fires on
    // whatever terrain happens to be near the raven's CURRENT (often
    // still-elevated, mid-flight) altitude and biases it to climb, which
    // directly fights every attempt to descend and land, on any surface.
    // findPerchSpot() already validates headroom/clearance for the actual
    // destination, so once the raven is genuinely trying to go down there's
    // nothing left for this system to protect - trust the descent instead
    // of fighting it.
    private static final double DESCENT_SUPPRESSION_THRESHOLD = -0.5D;

    private double verticalAvoidance = 0.0D;

    private double driftX = 0.0D;
    private double driftY = 0.0D;
    private double driftZ = 0.0D;

    private double targetDriftX = 0.0D;
    private double targetDriftY = 0.0D;
    private double targetDriftZ = 0.0D;

    private int driftChangeTimer = 0;

    public DarkRavenMoveControl(DarkRavenEntity raven) {
        super(raven);
        this.raven = raven;
    }

    public void moveToward(double x, double y, double z, double speed) {
        this.setWantedPosition(x, y, z, speed);
    }

    public void stop() {
        this.operation = Operation.WAIT;
        raven.setDeltaMovement(Vec3.ZERO);

        /*
         * Clear the flight drift when stopping.
         *
         * This is especially important when landing. Otherwise the raven
         * could retain a sideways drift and immediately start sliding when
         * the next movement begins.
         */
        driftX = 0.0D;
        driftY = 0.0D;
        driftZ = 0.0D;

        targetDriftX = 0.0D;
        targetDriftY = 0.0D;
        targetDriftZ = 0.0D;

        driftChangeTimer = 0;
        verticalAvoidance = 0.0D;
    }

    @Override
    public void tick() {

        /*
         * DarkRavenEntity.tick() completely owns velocity during the charge.
         */
        if (raven.isCharging()) {
            return;
        }

        if (this.operation != Operation.MOVE_TO) {
            return;
        }

        Vec3 delta = new Vec3(
                this.wantedX - raven.getX(),
                this.wantedY - raven.getY(),
                this.wantedZ - raven.getZ()
        );

        double dist = delta.length();

        /*
         * Essentially arrived.
         */
        if (dist < 0.01D) {
            this.operation = Operation.WAIT;
            raven.setDeltaMovement(Vec3.ZERO);
            verticalAvoidance = 0.0D;
            return;
        }

        /*
         * ---------------------------------------------------------------
         * NATURAL FLIGHT DRIFT
         * ---------------------------------------------------------------
         *
         * Every so often, select a new tiny steering offset.
         *
         * The actual drift is interpolated toward the new value rather
         * than instantly changing direction. This produces a gentle,
         * organic "winged creature" movement instead of random jitter.
         */
        updateFlightDrift();

        /*
         * Gradually reduce acceleration as the raven approaches its
         * destination.
         *
         * Far away:
         *   approachFactor = 1.0
         *
         * 0.5 blocks away:
         *   approachFactor = 0.5
         *
         * 0.1 blocks away:
         *   approachFactor = 0.1
         */
        double approachFactor = Math.min(dist / 1.0D, 1.0D);

        /*
         * Small random speed variation.
         *
         * This makes multiple ravens flying together less synchronized.
         */
        double speedVariation =
                1.0D + ((raven.getRandom().nextDouble() * 2.0D - 1.0D)
                        * SPEED_VARIATION);

        double accelerationStrength =
                this.speedModifier
                        * ACCELERATION
                        * approachFactor
                        * speedVariation;

        Vec3 acceleration = delta
                .scale(accelerationStrength / dist)
                .add(driftX, driftY, driftZ);

        /*
         * Look ahead along the horizontal travel direction for
         * obstructions and start climbing before contact, rather than
         * only reacting once collision is already happening. This is
         * what lets the raven actually clear a wall/tree/ledge on the
         * way to an elevated perch instead of getting stuck against it.
         */
        double targetAvoidance = computeTargetAvoidance(delta);

        verticalAvoidance = Mth.lerp(AVOIDANCE_LERP, verticalAvoidance, targetAvoidance);

        // Safety net for anything the lookahead ray missed.
        if (raven.horizontalCollision && delta.y > DESCENT_SUPPRESSION_THRESHOLD) {
            verticalAvoidance = Math.max(verticalAvoidance, COLLISION_FALLBACK_AVOIDANCE);
        }

        acceleration = acceleration.add(0.0D, verticalAvoidance, 0.0D);

        /*
         * Apply inertia.
         */
        raven.setDeltaMovement(
                raven.getDeltaMovement()
                        .scale(VELOCITY_RETENTION)
                        .add(acceleration)
        );
    }

    /**
     * Casts a ray from the raven's current position toward the wanted
     * destination, out to LOOKAHEAD_DISTANCE, along the horizontal
     * direction only (at the raven's current altitude, so a destination
     * that's simply higher or lower doesn't get mistaken for an
     * obstruction). If it hits a block, scans upward from the hit to find
     * the obstruction's actual top surface and returns how much vertical
     * velocity is wanted to clear it with CLEARANCE_BUFFER to spare.
     * Returns 0 when the path ahead is clear. Direct port of
     * PlagueChargerMoveControl's version.
     */
    private double computeTargetAvoidance(Vec3 delta) {

        // Actively descending toward the target - don't fight it. See the
        // constant's comment for why this matters specifically for a
        // flying perch-seeker rather than a level-moving charger.
        if (delta.y < DESCENT_SUPPRESSION_THRESHOLD) {
            return 0.0D;
        }

        Vec3 horizontalDelta = new Vec3(delta.x, 0.0D, delta.z);

        if (horizontalDelta.lengthSqr() < 1.0E-4D) {
            return 0.0D;
        }

        Vec3 direction = horizontalDelta.normalize();

        Vec3 from = raven.position();
        Vec3 to = from.add(direction.scale(Math.min(LOOKAHEAD_DISTANCE, horizontalDelta.length())));

        Level level = raven.level();

        BlockHitResult hit = level.clip(new ClipContext(
                from, to,
                ClipContext.Block.COLLIDER,
                ClipContext.Fluid.NONE,
                raven
        ));

        if (hit.getType() != HitResult.Type.BLOCK) {
            return 0.0D;
        }

        double obstructionTopY = hit.getBlockPos().getY() + 1;

        for (int i = 1; i <= OBSTRUCTION_SCAN_HEIGHT; i++) {
            BlockPos checkPos = hit.getBlockPos().above(i);
            if (level.getBlockState(checkPos).getCollisionShape(level, checkPos).isEmpty()) {
                break;
            }
            obstructionTopY = checkPos.getY() + 1;
        }

        double neededY = obstructionTopY + CLEARANCE_BUFFER;
        double clearanceRemaining = neededY - raven.getY();

        if (clearanceRemaining <= 0.0D) {
            return 0.0D;
        }

        // Cap so a very tall obstruction still produces a bounded climb
        // rate rather than an unbounded vertical velocity spike.
        return Math.min(clearanceRemaining * 0.15D, 0.35D);
    }

    /**
     * Smoothly changes the raven's natural flight drift.
     *
     * The drift is deliberately very small so it doesn't interfere with
     * the actual destination selected by DarkRavenPerchGoal.
     */
    private void updateFlightDrift() {

        if (driftChangeTimer-- <= 0) {

            driftChangeTimer = 20
                    + raven.getRandom().nextInt(30);

            /*
             * Horizontal movement is allowed to wander slightly more
             * than vertical movement.
             */
            targetDriftX =
                    (raven.getRandom().nextDouble() * 2.0D - 1.0D)
                            * HORIZONTAL_DRIFT;

            targetDriftZ =
                    (raven.getRandom().nextDouble() * 2.0D - 1.0D)
                            * HORIZONTAL_DRIFT;

            targetDriftY =
                    (raven.getRandom().nextDouble() * 2.0D - 1.0D)
                            * VERTICAL_DRIFT;
        }

        /*
         * Smoothly approach the new drift value.
         */
        driftX += (targetDriftX - driftX) * DRIFT_CHANGE_SPEED;
        driftY += (targetDriftY - driftY) * DRIFT_CHANGE_SPEED;
        driftZ += (targetDriftZ - driftZ) * DRIFT_CHANGE_SPEED;
    }
}