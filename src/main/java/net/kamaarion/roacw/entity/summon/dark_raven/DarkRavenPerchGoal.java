package net.kamaarion.roacw.entity.summon.dark_raven;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.phys.Vec3;

import java.util.EnumSet;

/**
 * No-target idle behavior: loose flight drift around the owner. No
 * landing/perching for now - that logic (findPerchSpot's ring-search,
 * PERCH_HEADROOM clearance checks, the descent-suppression fix in
 * DarkRavenMoveControl, the pre-snap re-validation) is still sitting in
 * DarkRavenEntity, just unused - worth reviewing that history before
 * picking this back up rather than starting over, since several real
 * bugs got fixed along the way even though the end-to-end result still
 * wasn't landing reliably.
 *
 * Each raven picks a random point in a radius band around the owner,
 * drifts there, and occasionally re-rolls a new point once it gets close -
 * not a fixed formation slot.
 */
public class DarkRavenPerchGoal extends Goal {

    private static final double MAX_OWNER_DISTANCE = 10.0D;
    private static final double TELEPORT_DISTANCE = 40.0D;

    private static final double FOLLOW_MIN_RADIUS = 1.5D;
    private static final double FOLLOW_MAX_RADIUS = 3.5D;

    // Height is anchored ABOVE eye level with no downward variance, so
    // ravens can't roll a point at chest/head height and clip into the
    // player - that's what the old owner.getEyeHeight() * 0.5 +/- variance
    // formula allowed (it could land as low as roughly hip height).
    // TODO tune both.
    private static final double FOLLOW_MIN_HEIGHT_ABOVE_EYES = 1.0D;
    private static final double FOLLOW_HEIGHT_VARIANCE_ABOVE = 1.5D;

    private static final double FOLLOW_SPEED = 0.30D;

    // Degrees/tick the BODY is allowed to turn toward travel direction -
    // deliberately slow so redirecting mid-drift reads as a graceful bank
    // rather than a snap-turn. Independent of LookControl's own head turn
    // speed (10.0F/20.0F passed to setLookAt calls below). TODO tune.
    private static final float BODY_TURN_RATE = 6.0F;
    private static final double FOLLOW_REACHED_DIST_SQR = 2.0D;
    private static final int FOLLOW_REROLL_CHANCE = 60; // 1-in-N per tick once reached

    private final DarkRavenEntity raven;

    private Vec3 followOffset;

    public DarkRavenPerchGoal(DarkRavenEntity raven) {
        this.raven = raven;
        this.setFlags(EnumSet.of(Flag.MOVE));
    }

    @Override
    public boolean canUse() {
        return raven.getTarget() == null && !raven.isCharging();
    }

    @Override
    public boolean canContinueToUse() {
        return canUse();
    }

    @Override
    public void start() {
        followOffset = null;
    }

    @Override
    public void stop() {
        raven.setPerched(false);
        followOffset = null;
    }

    @Override
    public void tick() {

        LivingEntity owner = raven.getSummonOwner();

        // No owner - nothing to drift around. Just hold position.
        if (owner == null) {
            raven.setPerched(false);
            raven.stopMoving();
            return;
        }

        double distance = raven.distanceTo(owner);

        // Approach height for the two "get back to the owner" branches
        // below - kept above eye level for the same reason
        // rollFollowOffset() is: so re-approaching from far away doesn't
        // route through a low point that clips into the player.
        double approachY = owner.getY() + owner.getEyeHeight() + FOLLOW_MIN_HEIGHT_ABOVE_EYES;

        // Wildly separated - snap back beside the owner rather than
        // flying the whole distance.
        if (distance > TELEPORT_DISTANCE) {
            raven.moveTo(owner.getX(), approachY, owner.getZ(),
                    raven.getYRot(), raven.getXRot());
            raven.setDeltaMovement(Vec3.ZERO);
            followOffset = null;
            return;
        }

        // Too far - close the distance directly before resuming the
        // looser wander behavior.
        if (distance > MAX_OWNER_DISTANCE) {
            followOffset = null;
            raven.moveToward(owner.getX(), approachY, owner.getZ(), 0.8D);
            raven.faceDirection(new Vec3(owner.getX(), approachY, owner.getZ()), BODY_TURN_RATE);
            raven.getLookControl().setLookAt(owner.getX(), approachY, owner.getZ(), 10.0F, 20.0F);
            return;
        }

        tickFollow(owner);
    }

    private void tickFollow(LivingEntity owner) {
        raven.setPerched(false);

        if (followOffset == null) {
            followOffset = rollFollowOffset(owner);
        }

        Vec3 destination = owner.position().add(followOffset);

        double distanceSqr = raven.position().distanceToSqr(destination);

        // Don't force the raven to perfectly stop at every destination -
        // once reasonably close, it has a chance to pick a new point,
        // which reads as organic drifting rather than snapping between
        // fixed spots.
        if (distanceSqr < FOLLOW_REACHED_DIST_SQR
                && raven.getRandom().nextInt(FOLLOW_REROLL_CHANCE) == 0) {
            followOffset = rollFollowOffset(owner);
            destination = owner.position().add(followOffset);
        }

        raven.moveToward(destination.x, destination.y, destination.z, FOLLOW_SPEED);

        // Body turns to face the direction of travel (see faceDirection's
        // doc for why this needs to happen explicitly here) while the
        // head independently looks at the same destination via
        // LookControl - without the body call, DarkRavenEntity never
        // updates yBodyRot during idle flight at all, which is why ravens
        // were facing one fixed direction indefinitely regardless of
        // where they were actually flying.
        raven.faceDirection(destination, BODY_TURN_RATE);
        raven.getLookControl().setLookAt(destination.x, destination.y, destination.z, 10.0F, 20.0F);
    }

    /**
     * Random point in a radius band around the owner - not rotated to the
     * owner's facing, so it doesn't rigidly track their turning.
     */
    private Vec3 rollFollowOffset(LivingEntity owner) {
        var random = raven.getRandom();

        double angle = random.nextDouble() * Math.PI * 2.0D;
        double radius = FOLLOW_MIN_RADIUS + random.nextDouble() * (FOLLOW_MAX_RADIUS - FOLLOW_MIN_RADIUS);
        double height = owner.getEyeHeight()
                + FOLLOW_MIN_HEIGHT_ABOVE_EYES
                + random.nextDouble() * FOLLOW_HEIGHT_VARIANCE_ABOVE;

        return new Vec3(Math.cos(angle) * radius, height, Math.sin(angle) * radius);
    }
}