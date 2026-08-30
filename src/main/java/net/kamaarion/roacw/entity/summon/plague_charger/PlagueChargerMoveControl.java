package net.kamaarion.roacw.entity.summon.plague_charger;

import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.ai.control.MoveControl;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

public class PlagueChargerMoveControl extends MoveControl {

    private final PlagueChargerEntity charger;

    // How far ahead (in the direction of travel) to look for obstructions
    // each tick. Short enough to stay responsive to nearby geometry, long
    // enough to start climbing before actually touching anything.
    private static final double LOOKAHEAD_DISTANCE = 2.5D;

    // Extra clearance added above the detected obstruction's top surface,
    // so the charger clears it with margin instead of skimming the top
    // block.
    private static final double CLEARANCE_BUFFER = 1.25D;

    // How many blocks up from the obstruction's hit point we scan to find
    // its actual top surface - handles multi-block-tall obstructions
    // (trees, walls) without needing per-obstruction height data.
    private static final int OBSTRUCTION_SCAN_HEIGHT = 6;

    // How quickly the vertical avoidance bias ramps toward its target each
    // tick. Kept low deliberately so climbing reads as flight, not a hop -
    // this is the actual fix for the old "bump on contact" behavior, which
    // only ever applied for the single tick horizontalCollision was true
    // and so looked like a jump rather than a climb.
    private static final double AVOIDANCE_LERP = 0.12D;

    // Fallback vertical bias applied if the charger is still touching
    // something horizontally despite the lookahead (e.g. thin geometry the
    // ray slipped past, or a sudden target change). Smaller and smoother
    // than the old instant +0.30 nudge, but still guarantees it won't just
    // sit grinding against a wall with zero vertical component.
    private static final double COLLISION_FALLBACK_AVOIDANCE = 0.20D;

    // Current smoothed vertical bias applied to counter obstructions. Ramps
    // up toward a target when something's ahead and back to 0 when the
    // path is clear, rather than being an instant one-shot nudge.
    private double verticalAvoidance = 0.0D;

    public PlagueChargerMoveControl(PlagueChargerEntity charger) {
        super(charger);
        this.charger = charger;
    }

    public void moveToward(double x, double y, double z, double speed) {
        this.setWantedPosition(x, y, z, speed);
    }

    public void stop() {
        this.operation = Operation.WAIT;
        verticalAvoidance = 0.0D;
        charger.setDeltaMovement(Vec3.ZERO);
    }

    @Override
    public void tick() {

        // While charging, PlagueChargerEntity.tick() fully owns velocity and
        // rotation for the dash. If a leftover MOVE_TO operation (set before
        // the charge began) is allowed to keep running here too, it steers
        // and rotates toward whatever stale destination was last set,
        // fighting the dash line and producing a mid-charge spin. Just do
        // nothing until the charge is over.
        if (charger.isCharging()) {
            return;
        }

        // Smoothly slow down when we're not moving anywhere.
        if (this.operation != Operation.MOVE_TO) {
            verticalAvoidance = 0.0D;
            charger.setDeltaMovement(
                    charger.getDeltaMovement().scale(0.90D)
            );
            return;
        }

        Vec3 delta = new Vec3(
                this.wantedX - charger.getX(),
                this.wantedY - charger.getY(),
                this.wantedZ - charger.getZ()
        );

        double distance = delta.length();

        double stopDistance = 0.40D;

        if (distance <= stopDistance) {
            this.operation = Operation.WAIT;
            verticalAvoidance = 0.0D;
            charger.setDeltaMovement(Vec3.ZERO);
            return;
        }

        Vec3 desiredVelocity = delta.normalize().scale(this.speedModifier);

        // Smooth hover movement.
        Vec3 velocity = charger.getDeltaMovement().lerp(desiredVelocity, 0.18D);

        double maxSpeed = this.speedModifier;

        if (velocity.length() > maxSpeed) {
            velocity = velocity.normalize().scale(maxSpeed);
        }

        // Look ahead along the horizontal travel direction for obstructions
        // and start climbing before contact, instead of reacting only once
        // horizontalCollision is already true. This is what turns "bump
        // into a block, hop over it" into an actual climb over the
        // obstruction as part of normal flight.
        double targetAvoidance = computeTargetAvoidance(delta);

        verticalAvoidance = Mth.lerp(AVOIDANCE_LERP, verticalAvoidance, targetAvoidance);

        // Safety net for anything the lookahead ray missed.
        if (charger.horizontalCollision && delta.y > -0.5D) {
            verticalAvoidance = Math.max(verticalAvoidance, COLLISION_FALLBACK_AVOIDANCE);
        }

        velocity = velocity.add(0.0D, verticalAvoidance, 0.0D);

        charger.setDeltaMovement(velocity);
    }

    /**
     * Casts a ray from the charger's current position toward the wanted
     * destination, out to LOOKAHEAD_DISTANCE, along the horizontal
     * direction only (at the charger's current altitude, so a destination
     * that's simply higher or lower doesn't get mistaken for an
     * obstruction). If it hits a block, scans upward from the hit to find
     * the obstruction's actual top surface and returns how much vertical
     * velocity is wanted to clear it with CLEARANCE_BUFFER to spare.
     * Returns 0 when the path ahead is clear.
     */
    private double computeTargetAvoidance(Vec3 delta) {

        Vec3 horizontalDelta = new Vec3(delta.x, 0.0D, delta.z);

        if (horizontalDelta.lengthSqr() < 1.0E-4D) {
            return 0.0D;
        }

        Vec3 direction = horizontalDelta.normalize();

        Vec3 from = charger.position();
        Vec3 to = from.add(direction.scale(Math.min(LOOKAHEAD_DISTANCE, horizontalDelta.length())));

        Level level = charger.level();

        BlockHitResult hit = level.clip(new ClipContext(
                from, to,
                ClipContext.Block.COLLIDER,
                ClipContext.Fluid.NONE,
                charger
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
        double clearanceRemaining = neededY - charger.getY();

        if (clearanceRemaining <= 0.0D) {
            return 0.0D;
        }

        // Cap so a very tall obstruction still produces a bounded climb
        // rate rather than an unbounded vertical velocity spike.
        return Math.min(clearanceRemaining * 0.15D, 0.35D);
    }
}