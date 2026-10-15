package net.kamaarion.roacw.entity.summon.dark_raven;

import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.ai.control.MoveControl;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

public class DarkRavenMoveControl extends MoveControl {

    private static final double MAX_SPEED = 0.30D;
    private static final double ARRIVAL_DISTANCE = 0.20D;
    private static final double SLOWDOWN_DISTANCE = 1.5D;
    private static final double STEERING_STRENGTH = 0.24D;
    private static final double IDLE_RETENTION = 0.65D;

    private static final double LOOKAHEAD_DISTANCE = 2.5D;
    private static final double CLEARANCE_BUFFER = 0.75D;
    private static final int OBSTRUCTION_SCAN_HEIGHT = 6;
    private static final double AVOIDANCE_LERP = 0.12D;
    private static final double COLLISION_FALLBACK_AVOIDANCE = 0.16D;
    private static final double DESCENT_SUPPRESSION_THRESHOLD = -0.5D;

    private final DarkRavenEntity raven;

    private double verticalAvoidance;

    public DarkRavenMoveControl(DarkRavenEntity raven) {
        super(raven);
        this.raven = raven;
    }

    public void moveToward(double x, double y, double z, double speed) {
        if (raven.isCharging()) {
            return;
        }

        setWantedPosition(x, y, z, Math.max(0.0D, speed));
    }

    @Override
    public void tick() {
        if (raven.isCharging()) {
            return;
        }

        Vec3 velocity = raven.getDeltaMovement();

        if (operation != Operation.MOVE_TO) {
            raven.setDeltaMovement(velocity.scale(IDLE_RETENTION));

            if (raven.getDeltaMovement().lengthSqr() < 0.0001D) {
                raven.setDeltaMovement(Vec3.ZERO);
            }

            verticalAvoidance = 0.0D;
            return;
        }

        Vec3 delta = new Vec3(
                wantedX - raven.getX(),
                wantedY - raven.getY(),
                wantedZ - raven.getZ()
        );

        double distance = delta.length();

        if (distance <= ARRIVAL_DISTANCE) {
            operation = Operation.WAIT;
            raven.setDeltaMovement(velocity.scale(0.4D));

            if (raven.getDeltaMovement().lengthSqr() < 0.0004D) {
                raven.setDeltaMovement(Vec3.ZERO);
            }

            verticalAvoidance = 0.0D;
            return;
        }

        Vec3 direction = delta.scale(1.0D / distance);

        double requestedSpeed = Math.min(MAX_SPEED, speedModifier);
        double distanceFactor = Mth.clamp(
                distance / SLOWDOWN_DISTANCE,
                0.0D,
                1.0D
        );

        double desiredSpeed = requestedSpeed * distanceFactor;
        Vec3 desiredVelocity = direction.scale(desiredSpeed);

        Vec3 nextVelocity = velocity.lerp(
                desiredVelocity,
                STEERING_STRENGTH
        );

        double targetAvoidance = computeTargetAvoidance(delta);

        verticalAvoidance = Mth.lerp(
                AVOIDANCE_LERP,
                verticalAvoidance,
                targetAvoidance
        );

        if (raven.horizontalCollision
                && delta.y > DESCENT_SUPPRESSION_THRESHOLD) {
            verticalAvoidance = Math.max(
                    verticalAvoidance,
                    COLLISION_FALLBACK_AVOIDANCE
            );
        }

        nextVelocity = nextVelocity.add(
                0.0D,
                verticalAvoidance,
                0.0D
        );

        double maxVelocity = Math.max(0.02D, requestedSpeed);

        if (nextVelocity.lengthSqr() > maxVelocity * maxVelocity) {
            nextVelocity = nextVelocity.normalize().scale(maxVelocity);
        }

        raven.setDeltaMovement(nextVelocity);
    }

    private double computeTargetAvoidance(Vec3 delta) {
        if (delta.y < DESCENT_SUPPRESSION_THRESHOLD) {
            return 0.0D;
        }

        Vec3 horizontalDelta = new Vec3(delta.x, 0.0D, delta.z);

        if (horizontalDelta.lengthSqr() < 1.0E-4D) {
            return 0.0D;
        }

        Vec3 direction = horizontalDelta.normalize();
        Vec3 from = raven.position();
        Vec3 to = from.add(
                direction.scale(
                        Math.min(LOOKAHEAD_DISTANCE, horizontalDelta.length())
                )
        );

        Level level = raven.level();

        BlockHitResult hit = level.clip(new ClipContext(
                from,
                to,
                ClipContext.Block.COLLIDER,
                ClipContext.Fluid.NONE,
                raven
        ));

        if (hit.getType() != HitResult.Type.BLOCK) {
            return 0.0D;
        }

        double obstructionTopY = hit.getBlockPos().getY() + 1.0D;

        for (int i = 1; i <= OBSTRUCTION_SCAN_HEIGHT; i++) {
            BlockPos checkPos = hit.getBlockPos().above(i);

            if (level.getBlockState(checkPos)
                    .getCollisionShape(level, checkPos)
                    .isEmpty()) {
                break;
            }

            obstructionTopY = checkPos.getY() + 1.0D;
        }

        double neededY = obstructionTopY + CLEARANCE_BUFFER;
        double clearanceRemaining = neededY - raven.getY();

        if (clearanceRemaining <= 0.0D) {
            return 0.0D;
        }

        return Math.min(clearanceRemaining * 0.12D, 0.25D);
    }

    public void stop() {
        operation = Operation.WAIT;
        raven.setDeltaMovement(Vec3.ZERO);
        verticalAvoidance = 0.0D;
    }
}