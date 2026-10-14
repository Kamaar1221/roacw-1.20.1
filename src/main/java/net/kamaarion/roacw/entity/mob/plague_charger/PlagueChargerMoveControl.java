package net.kamaarion.roacw.entity.mob.plague_charger;

import net.kamaarion.roacw.effects.DreadEffect;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.control.MoveControl;
import net.minecraft.world.phys.AABB;

import java.util.Random;

public class PlagueChargerMoveControl extends MoveControl {

    private static final double MOVEMENT_SPEED = 0.4D;
    private static final double MAX_SPEED = 0.6D;

    private static final double CHASE_SPEED = 1.5D;
    private static final double CHASE_MAX_SPEED = 1.5D;

    private static final double ATTACK_RANGE = 0.5D;
    private static final double ATTACK_RANGE_SQUARED =
            ATTACK_RANGE * ATTACK_RANGE;

    private static final int ATTACK_COOLDOWN = 15;

    private static final float TURN_SPEED = 10.0F;

    private static final double MIN_WANDER_DISTANCE = 3.0D;
    private static final double MAX_WANDER_DISTANCE = 8.0D;

    private static final double MIN_HEIGHT_CHANGE = -2.0D;
    private static final double MAX_HEIGHT_CHANGE = 2.0D;

    private static final int MIN_WANDER_TIME = 30;
    private static final int MAX_WANDER_TIME = 80;

    private final Random random = new Random();

    private double targetX;
    private double targetY;
    private double targetZ;

    private int wanderTime;
    private int attackCooldown;

    private boolean hasTarget;

    public PlagueChargerMoveControl(PlagueChargerEntity entity) {
        super(entity);
        this.wanderTime = 0;
        this.attackCooldown = 0;
        this.hasTarget = false;
    }

    @Override
    public void tick() {
        PlagueChargerEntity charger = (PlagueChargerEntity) this.mob;

        if (charger.isRemoved() || !charger.isAlive()) {
            return;
        }

        boolean hasDread = charger.getActiveEffects()
                .stream()
                .anyMatch(effect -> effect.getEffect() instanceof DreadEffect);

        if (hasDread) {
            charger.setTarget(null);
            hasTarget = false;
            return;
        }

        if (attackCooldown > 0) {
            attackCooldown--;
        }

        LivingEntity target = charger.getTarget();

        if (isValidTarget(charger, target)) {
            hasTarget = false;
            chaseTarget(charger, target);
            return;
        }

        if (!hasTarget || shouldPickNewTarget(charger)) {
            pickNewTarget(charger);
        }

        if (!hasTarget) {
            slowDown(charger);
            return;
        }

        moveTowardWanderTarget(charger);
    }

    private boolean isValidTarget(
            PlagueChargerEntity charger,
            LivingEntity target
    ) {
        if (target == null || !target.isAlive()) {
            return false;
        }

        if (target instanceof PlagueChargerEntity) {
            return false;
        }

        return charger.distanceToSqr(target) <= 64.0D * 64.0D;
    }

    private void chaseTarget(
            PlagueChargerEntity charger,
            LivingEntity target
    ) {
        snapToTarget(charger, target);

        double dx = target.getX() - charger.getX();
        double dy = target.getY(0.5D) - charger.getY(0.5D);
        double dz = target.getZ() - charger.getZ();

        double distance = Math.sqrt(dx * dx + dy * dy + dz * dz);

        if (distance < 0.001D) {
            slowDown(charger);
            return;
        }

        if (charger.distanceToSqr(target) <= ATTACK_RANGE_SQUARED) {
            slowDown(charger);
            snapToTarget(charger, target);

            if (attackCooldown <= 0) {
                charger.doHurtTarget(target);
                attackCooldown = ATTACK_COOLDOWN;
            }

            return;
        }

        dx /= distance;
        dy /= distance;
        dz /= distance;

        double desiredX = dx * CHASE_SPEED;
        double desiredY = dy * CHASE_SPEED;
        double desiredZ = dz * CHASE_SPEED;

        double currentX = charger.getDeltaMovement().x;
        double currentY = charger.getDeltaMovement().y;
        double currentZ = charger.getDeltaMovement().z;

        double acceleration = 0.12D;

        double newX = Mth.lerp(acceleration, currentX, desiredX);
        double newY = Mth.lerp(acceleration, currentY, desiredY);
        double newZ = Mth.lerp(acceleration, currentZ, desiredZ);

        double speed = Math.sqrt(newX * newX + newY * newY + newZ * newZ);

        if (speed > CHASE_MAX_SPEED) {
            double scale = CHASE_MAX_SPEED / speed;
            newX *= scale;
            newY *= scale;
            newZ *= scale;
        }

        charger.setDeltaMovement(newX, newY, newZ);
    }

    private void snapToTarget(
            PlagueChargerEntity charger,
            LivingEntity target
    ) {
        double dx = target.getX() - charger.getX();
        double dz = target.getZ() - charger.getZ();

        if (Math.abs(dx) < 0.001D && Math.abs(dz) < 0.001D) {
            return;
        }

        float targetYaw = (float) (Mth.atan2(dz, dx) * (180.0D / Math.PI)) - 90.0F;

        charger.setYRot(targetYaw);
        charger.yRotO = targetYaw;
        charger.setYBodyRot(targetYaw);
        charger.setYHeadRot(targetYaw);
    }

    private void faceDirection(
            PlagueChargerEntity charger,
            double dx,
            double dz
    ) {
        if (Math.abs(dx) < 0.001D && Math.abs(dz) < 0.001D) {
            return;
        }

        float targetYaw = (float) (Mth.atan2(dz, dx) * (180.0D / Math.PI)) - 90.0F;

        charger.setYRot(
                Mth.approachDegrees(
                        charger.getYRot(),
                        targetYaw,
                        TURN_SPEED
                )
        );

        charger.yRotO = charger.getYRot();
        charger.setYBodyRot(charger.getYRot());
        charger.setYHeadRot(charger.getYRot());
    }

    private boolean shouldPickNewTarget(PlagueChargerEntity charger) {
        wanderTime--;

        if (wanderTime <= 0) {
            return true;
        }

        double distanceSquared = charger.distanceToSqr(targetX, targetY, targetZ);

        if (distanceSquared <= 1.5D * 1.5D) {
            return true;
        }

        return !isDestinationClear(charger);
    }

    private void pickNewTarget(PlagueChargerEntity charger) {
        double angle = random.nextDouble() * Math.PI * 2.0D;

        double distance = MIN_WANDER_DISTANCE
                + random.nextDouble() * (MAX_WANDER_DISTANCE - MIN_WANDER_DISTANCE);

        double offsetX = Math.cos(angle) * distance;
        double offsetZ = Math.sin(angle) * distance;

        double offsetY = MIN_HEIGHT_CHANGE
                + random.nextDouble() * (MAX_HEIGHT_CHANGE - MIN_HEIGHT_CHANGE);

        targetX = charger.getX() + offsetX;
        targetY = charger.getY() + offsetY;
        targetZ = charger.getZ() + offsetZ;

        if (!isDestinationClear(charger)) {
            hasTarget = false;
            wanderTime = 10;
            return;
        }

        hasTarget = true;
        wanderTime = MIN_WANDER_TIME
                + random.nextInt(MAX_WANDER_TIME - MIN_WANDER_TIME + 1);
    }

    private boolean isDestinationClear(PlagueChargerEntity charger) {
        AABB box = charger.getBoundingBox().move(
                targetX - charger.getX(),
                targetY - charger.getY(),
                targetZ - charger.getZ()
        );

        int minX = Mth.floor(box.minX);
        int maxX = Mth.floor(box.maxX);
        int minY = Mth.floor(box.minY);
        int maxY = Mth.floor(box.maxY);
        int minZ = Mth.floor(box.minZ);
        int maxZ = Mth.floor(box.maxZ);

        for (int x = minX; x <= maxX; x++) {
            for (int y = minY; y <= maxY; y++) {
                for (int z = minZ; z <= maxZ; z++) {
                    BlockPos pos = new BlockPos(x, y, z);
                    var state = charger.level().getBlockState(pos);

                    if (!state.isSolidRender(charger.level(), pos)) {
                        continue;
                    }

                    if (!state.getCollisionShape(charger.level(), pos).isEmpty()) {
                        return false;
                    }
                }
            }
        }

        return true;
    }

    private void moveTowardWanderTarget(PlagueChargerEntity charger) {
        double dx = targetX - charger.getX();
        double dy = targetY - charger.getY();
        double dz = targetZ - charger.getZ();

        double distance = Math.sqrt(dx * dx + dy * dy + dz * dz);

        if (distance < 0.001D) {
            hasTarget = false;
            slowDown(charger);
            return;
        }

        dx /= distance;
        dy /= distance;
        dz /= distance;

        double desiredX = dx * MOVEMENT_SPEED;
        double desiredY = dy * MOVEMENT_SPEED;
        double desiredZ = dz * MOVEMENT_SPEED;

        double currentX = charger.getDeltaMovement().x;
        double currentY = charger.getDeltaMovement().y;
        double currentZ = charger.getDeltaMovement().z;

        double acceleration = 0.10D;

        double newX = Mth.lerp(acceleration, currentX, desiredX);
        double newY = Mth.lerp(acceleration, currentY, desiredY);
        double newZ = Mth.lerp(acceleration, currentZ, desiredZ);

        double speed = Math.sqrt(newX * newX + newY * newY + newZ * newZ);

        if (speed > MAX_SPEED) {
            double scale = MAX_SPEED / speed;
            newX *= scale;
            newY *= scale;
            newZ *= scale;
        }

        charger.setDeltaMovement(newX, newY, newZ);

        faceDirection(charger, dx, dz);
    }

    private void slowDown(PlagueChargerEntity charger) {
        charger.setDeltaMovement(charger.getDeltaMovement().scale(0.92D));
    }
}