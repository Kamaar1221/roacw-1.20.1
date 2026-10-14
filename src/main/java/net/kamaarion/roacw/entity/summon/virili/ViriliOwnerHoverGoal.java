package net.kamaarion.roacw.entity.summon.virili;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.phys.Vec3;

import java.util.EnumSet;

public class ViriliOwnerHoverGoal extends Goal {

    private final ViriliEntity mob;

    private static final double HOVER_BACK_DISTANCE = 1.5D;
    private static final double HOVER_SIDE_DISTANCE = 1.0D;
    private static final double HOVER_HEIGHT = 0.75D;

    private static final double HOVER_SPEED = 0.12D;
    private static final double HOVER_POSITION_CORRECTION = 0.08D;
    private static final double MAX_TARGET_DISTANCE = 24.0D;

    public ViriliOwnerHoverGoal(ViriliEntity mob) {
        this.mob = mob;

        this.setFlags(EnumSet.of(
                Goal.Flag.MOVE,
                Goal.Flag.LOOK
        ));
    }

    @Override
    public boolean canUse() {
        LivingEntity owner = this.mob.getOwner();

        if (owner == null || !owner.isAlive()) {
            return false;
        }

        LivingEntity target = this.mob.getTarget();

        if (target != null
                && target.isAlive()
                && target != owner
                && this.mob.distanceToSqr(target)
                <= MAX_TARGET_DISTANCE * MAX_TARGET_DISTANCE) {
            return false;
        }

        if (target != null
                && (!target.isAlive()
                || target == owner
                || this.mob.distanceToSqr(target)
                > MAX_TARGET_DISTANCE * MAX_TARGET_DISTANCE)) {
            this.mob.setTarget(null);
        }

        return true;
    }

    @Override
    public boolean canContinueToUse() {
        LivingEntity owner = this.mob.getOwner();

        if (owner == null || !owner.isAlive()) {
            return false;
        }

        LivingEntity target = this.mob.getTarget();

        if (target != null
                && target.isAlive()
                && target != owner
                && this.mob.distanceToSqr(target)
                <= MAX_TARGET_DISTANCE * MAX_TARGET_DISTANCE) {
            return false;
        }

        if (target != null
                && (!target.isAlive()
                || target == owner
                || this.mob.distanceToSqr(target)
                > MAX_TARGET_DISTANCE * MAX_TARGET_DISTANCE)) {
            this.mob.setTarget(null);
        }

        return true;
    }

    @Override
    public void start() {
        this.mob.resetTransientAiState();
        this.mob.setTarget(null);
    }

    @Override
    public void stop() {
        this.mob.setFlightControlLocked(false);
        this.mob.setAttackRotationLocked(false);
    }

    @Override
    public void tick() {
        LivingEntity owner = this.mob.getOwner();

        if (owner == null || !owner.isAlive()) {
            this.mob.setDeltaMovement(Vec3.ZERO);
            return;
        }

        LivingEntity target = this.mob.getTarget();

        if (target != null
                && target.isAlive()
                && target != owner
                && this.mob.distanceToSqr(target)
                <= MAX_TARGET_DISTANCE * MAX_TARGET_DISTANCE) {
            return;
        }

        if (target != null) {
            this.mob.setTarget(null);
        }

        double yawRadians = Math.toRadians(owner.getYRot());

        double desiredX = owner.getX()
                + Math.sin(yawRadians) * HOVER_BACK_DISTANCE
                + Math.cos(yawRadians) * HOVER_SIDE_DISTANCE;

        double desiredZ = owner.getZ()
                - Math.cos(yawRadians) * HOVER_BACK_DISTANCE
                + Math.sin(yawRadians) * HOVER_SIDE_DISTANCE;

        double desiredY = owner.getY() + HOVER_HEIGHT;

        Vec3 desiredPosition = new Vec3(
                desiredX,
                desiredY,
                desiredZ
        );

        Vec3 difference = desiredPosition.subtract(
                this.mob.position()
        );

        double distance = difference.length();

        if (distance < 0.25D) {
            this.mob.setDeltaMovement(
                    this.mob.getDeltaMovement().scale(0.85D)
            );
        } else {
            Vec3 desiredVelocity = difference.normalize()
                    .scale(HOVER_SPEED);

            Vec3 movement = this.mob.getDeltaMovement()
                    .scale(0.75D)
                    .add(desiredVelocity.scale(0.25D));

            movement = movement.add(
                    difference.scale(HOVER_POSITION_CORRECTION)
            );

            double maxSpeed = 0.45D;

            if (movement.lengthSqr() > maxSpeed * maxSpeed) {
                movement = movement.normalize().scale(maxSpeed);
            }

            this.mob.setDeltaMovement(movement);
        }

        Vec3 lookDirection = owner.position()
                .add(0.0D, owner.getBbHeight() * 0.5D, 0.0D)
                .subtract(this.mob.position());

        if (lookDirection.lengthSqr() > 0.0001D) {
            float yaw = (float) (
                    Math.atan2(
                            lookDirection.z,
                            lookDirection.x
                    ) * 180.0D / Math.PI
            ) - 90.0F;

            this.mob.setYRot(yaw);
            this.mob.setYHeadRot(yaw);

            this.mob.yRotO = yaw;
            this.mob.yHeadRotO = yaw;
            this.mob.yBodyRot = yaw;
            this.mob.yBodyRotO = yaw;
        }
    }
}