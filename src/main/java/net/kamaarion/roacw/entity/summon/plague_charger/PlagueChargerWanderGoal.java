package net.kamaarion.roacw.entity.summon.plague_charger;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;
import java.util.EnumSet;

public class PlagueChargerWanderGoal extends Goal {

    private static final double MAX_OWNER_DISTANCE = 10.0D;
    private static final double TELEPORT_DISTANCE = 40.0D;

    // Average ticks spent paused between wander legs once a destination is
    // reached - rolled as a 1-in-N chance each tick rather than a fixed
    // timer, so pause length varies naturally instead of every pause being
    // identical.
    private static final int WANDER_START_CHANCE = 60;
    private static final double WANDER_REACHED_DIST_SQ = 4.0D;

    // How many random candidate points to try before giving up for this
    // tick. Candidates landing inside solid terrain are rejected outright -
    // previously an in-block candidate would still be sent to the move
    // control, and the charger would fly at it forever, repeatedly
    // triggering horizontalCollision against the same wall/hill it could
    // never actually enter. That collision-hop cycling was a second source
    // of the "stuck hopping" symptom independent of the move control's own
    // obstacle handling.
    private static final int MAX_CANDIDATE_ATTEMPTS = 6;

    private final PlagueChargerEntity charger;

    @Nullable
    private Vec3 wanderTarget;

    public PlagueChargerWanderGoal(PlagueChargerEntity charger) {
        this.charger = charger;
        this.setFlags(EnumSet.of(Goal.Flag.MOVE));
    }

    @Override
    public boolean canUse() {
        return charger.getTarget() == null
                && !charger.isCharging();
    }

    @Override
    public boolean canContinueToUse() {
        return charger.getTarget() == null
                && !charger.isCharging();
    }

    @Override
    public void stop() {
        // Clear so the next time this goal starts up, it picks a fresh
        // destination from wherever the charger actually is then, rather
        // than resuming a stale target from before it last chased a
        // combat target.
        wanderTarget = null;
    }

    @Override
    public void tick() {

        LivingEntity owner = charger.getSummonOwner();

        // No owner? Behave like a normal wandering mob.
        if (owner == null) {
            randomWander(charger.blockPosition());
            return;
        }

        double distance = charger.distanceTo(owner);

        // If something caused us to become extremely separated,
        // just teleport back beside the owner.
        if (distance > TELEPORT_DISTANCE) {

            charger.moveTo(
                    owner.getX(),
                    owner.getY() + 1.5D,
                    owner.getZ(),
                    charger.getYRot(),
                    charger.getXRot()
            );

            charger.setDeltaMovement(0, 0, 0);
            return;
        }

        // Too far away? Fly directly back.
        if (distance > MAX_OWNER_DISTANCE) {

            charger.getMoveControl().setWantedPosition(
                    owner.getX(),
                    owner.getY() + 1.5D,
                    owner.getZ(),
                    0.8D
            );

            return;
        }

        // Fly to this charger's assigned formation slot.
        Vec3 offset = charger.getFormationOffset();

        // Rotate the slot so it follows the player's facing.
        offset = offset.yRot((float) Math.toRadians(-owner.getYRot()));

        Vec3 destination = owner.position().add(offset);

        charger.getMoveControl().setWantedPosition(
                destination.x,
                destination.y,
                destination.z,
                0.35D
        );

        charger.getLookControl().setLookAt(
                destination.x,
                destination.y,
                destination.z,
                180.0F,
                20.0F
        );
    }

    private void randomWander(BlockPos origin) {

        boolean reachedOrMissing = wanderTarget == null
                || charger.position().distanceToSqr(wanderTarget) < WANDER_REACHED_DIST_SQ;

        if (reachedOrMissing) {

            // Clear so the charger visibly settles/hovers here rather than
            // still steering at the old point while we decide whether to
            // move again.
            wanderTarget = null;

            // Only roll a chance to start a new leg each tick while paused -
            // this is what actually produces a natural, variable-length
            // pause between destinations instead of instantly darting to
            // the next one the moment the last one's reached.
            if (charger.getRandom().nextInt(WANDER_START_CHANCE) != 0) {
                return;
            }

            wanderTarget = pickClearWanderTarget(origin);

            if (wanderTarget == null) {
                // Every candidate this roll landed inside solid terrain -
                // just skip this tick and let the next WANDER_START_CHANCE
                // roll try again, rather than sending the charger at a
                // point it can never actually occupy.
                return;
            }
        }

        charger.getMoveControl().setWantedPosition(
                wanderTarget.x,
                wanderTarget.y,
                wanderTarget.z,
                0.3D
        );

        charger.getLookControl().setLookAt(
                wanderTarget.x,
                wanderTarget.y,
                wanderTarget.z,
                180.0F,
                20.0F
        );
    }

    /**
     * Rolls up to MAX_CANDIDATE_ATTEMPTS random points in the usual wander
     * volume around origin and returns the first one that isn't inside
     * solid terrain. Returns null if every attempt this roll was blocked.
     */
    @Nullable
    private Vec3 pickClearWanderTarget(BlockPos origin) {

        Level level = charger.level();

        for (int attempt = 0; attempt < MAX_CANDIDATE_ATTEMPTS; attempt++) {

            BlockPos candidate = origin.offset(
                    charger.getRandom().nextInt(15) - 7,
                    charger.getRandom().nextInt(7) - 3,
                    charger.getRandom().nextInt(15) - 7
            );

            if (!level.getBlockState(candidate).getCollisionShape(level, candidate).isEmpty()) {
                continue;
            }

            return new Vec3(
                    candidate.getX() + 0.5D,
                    candidate.getY(),
                    candidate.getZ() + 0.5D
            );
        }

        return null;
    }
}