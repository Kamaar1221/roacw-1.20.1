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

    private final DarkRavenEntity raven;

    private static final double VELOCITY_RETENTION = 0.92D;
    private static final double ACCELERATION = 0.05D;
    private static final double HORIZONTAL_DRIFT = 0.012D;
    private static final double VERTICAL_DRIFT = 0.008D;
    private static final double DRIFT_CHANGE_SPEED = 0.08D;
    private static final double SPEED_VARIATION = 0.15D;

    private static final double LOOKAHEAD_DISTANCE = 2.5D;
    private static final double CLEARANCE_BUFFER = 1.0D;
    private static final int OBSTRUCTION_SCAN_HEIGHT = 6;
    private static final double AVOIDANCE_LERP = 0.12D;
    private static final double COLLISION_FALLBACK_AVOIDANCE = 0.20D;
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

        if (dist < 0.01D) {
            this.operation = Operation.WAIT;
            raven.setDeltaMovement(Vec3.ZERO);
            verticalAvoidance = 0.0D;
            return;
        }

        updateFlightDrift();

        double approachFactor = Math.min(dist / 1.0D, 1.0D);

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

        double targetAvoidance = computeTargetAvoidance(delta);

        verticalAvoidance = Mth.lerp(AVOIDANCE_LERP, verticalAvoidance, targetAvoidance);

        if (raven.horizontalCollision && delta.y > DESCENT_SUPPRESSION_THRESHOLD) {
            verticalAvoidance = Math.max(verticalAvoidance, COLLISION_FALLBACK_AVOIDANCE);
        }

        acceleration = acceleration.add(0.0D, verticalAvoidance, 0.0D);

        raven.setDeltaMovement(
                raven.getDeltaMovement()
                        .scale(VELOCITY_RETENTION)
                        .add(acceleration)
        );
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
        Vec3 to = from.add(direction.scale(Math.min(LOOKAHEAD_DISTANCE, horizontalDelta.length())));

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

        return Math.min(clearanceRemaining * 0.15D, 0.35D);
    }

    private void updateFlightDrift() {
        if (driftChangeTimer-- <= 0) {
            driftChangeTimer = 20 + raven.getRandom().nextInt(30);

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

        driftX += (targetDriftX - driftX) * DRIFT_CHANGE_SPEED;
        driftY += (targetDriftY - driftY) * DRIFT_CHANGE_SPEED;
        driftZ += (targetDriftZ - driftZ) * DRIFT_CHANGE_SPEED;
    }
}