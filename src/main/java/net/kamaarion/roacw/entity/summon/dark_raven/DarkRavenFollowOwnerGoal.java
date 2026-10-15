package net.kamaarion.roacw.entity.summon.dark_raven;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.phys.Vec3;

import java.util.EnumSet;

public class DarkRavenFollowOwnerGoal extends Goal {

    private static final double MAX_OWNER_DISTANCE = 10.0D;
    private static final double TELEPORT_DISTANCE = 40.0D;

    private static final double FOLLOW_MIN_RADIUS = 1.5D;
    private static final double FOLLOW_MAX_RADIUS = 3.5D;

    private static final double FOLLOW_MIN_HEIGHT_ABOVE_EYES = 1.0D;
    private static final double FOLLOW_HEIGHT_VARIANCE_ABOVE = 1.5D;

    private static final double FOLLOW_SPEED = 0.16D;
    private static final double RETURN_SPEED = 0.28D;

    private static final float BODY_TURN_RATE = 6.0F;
    private static final double FOLLOW_REACHED_DIST_SQR = 4.0D;
    private static final int FOLLOW_REROLL_CHANCE = 30;

    private final DarkRavenEntity raven;

    private Vec3 followOffset;

    public DarkRavenFollowOwnerGoal(DarkRavenEntity raven) {
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
        followOffset = null;
        raven.stopMoving();
    }

    @Override
    public void tick() {
        LivingEntity owner = raven.getSummonOwner();

        if (owner == null || !owner.isAlive()) {
            followOffset = null;
            raven.stopMoving();
            return;
        }

        double distanceSqr = raven.distanceToSqr(owner);

        Vec3 approachPosition = new Vec3(
                owner.getX(),
                owner.getY() + owner.getEyeHeight()
                        + FOLLOW_MIN_HEIGHT_ABOVE_EYES,
                owner.getZ()
        );

        if (distanceSqr > TELEPORT_DISTANCE * TELEPORT_DISTANCE) {
            raven.moveTo(
                    approachPosition.x,
                    approachPosition.y,
                    approachPosition.z,
                    raven.getYRot(),
                    raven.getXRot()
            );

            raven.setDeltaMovement(Vec3.ZERO);
            followOffset = null;
            return;
        }

        if (distanceSqr > MAX_OWNER_DISTANCE * MAX_OWNER_DISTANCE) {
            followOffset = null;

            raven.moveToward(
                    approachPosition.x,
                    approachPosition.y,
                    approachPosition.z,
                    RETURN_SPEED
            );

            raven.faceDirection(approachPosition, BODY_TURN_RATE);

            raven.getLookControl().setLookAt(
                    approachPosition.x,
                    approachPosition.y,
                    approachPosition.z,
                    10.0F,
                    20.0F
            );

            return;
        }

        if (followOffset == null) {
            followOffset = rollFollowOffset(owner);
        }

        Vec3 destination = owner.position().add(followOffset);

        double destinationDistanceSqr =
                raven.position().distanceToSqr(destination);

        if (destinationDistanceSqr < FOLLOW_REACHED_DIST_SQR
                && raven.getRandom().nextInt(FOLLOW_REROLL_CHANCE) == 0) {
            followOffset = rollFollowOffset(owner);
            destination = owner.position().add(followOffset);
        }

        raven.moveToward(
                destination.x,
                destination.y,
                destination.z,
                FOLLOW_SPEED
        );

        raven.faceDirection(destination, BODY_TURN_RATE);

        raven.getLookControl().setLookAt(
                destination.x,
                destination.y,
                destination.z,
                10.0F,
                20.0F
        );
    }

    private Vec3 rollFollowOffset(LivingEntity owner) {
        double angle = raven.getRandom().nextDouble() * Math.PI * 2.0D;

        double radius = FOLLOW_MIN_RADIUS
                + raven.getRandom().nextDouble()
                * (FOLLOW_MAX_RADIUS - FOLLOW_MIN_RADIUS);

        double height = owner.getEyeHeight()
                + FOLLOW_MIN_HEIGHT_ABOVE_EYES
                + raven.getRandom().nextDouble()
                * FOLLOW_HEIGHT_VARIANCE_ABOVE;

        return new Vec3(
                Math.cos(angle) * radius,
                height,
                Math.sin(angle) * radius
        );
    }
}