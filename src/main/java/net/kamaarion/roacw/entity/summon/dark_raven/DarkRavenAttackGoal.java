package net.kamaarion.roacw.entity.summon.dark_raven;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;

import java.util.EnumSet;

public class DarkRavenAttackGoal extends Goal {

    private static final double CHARGE_START_RANGE = 10.0D;
    private static final double CHARGE_MIN_RANGE = 7.5D;

    private static final int MIN_CHARGE_COOLDOWN = 15;
    private static final int MAX_CHARGE_COOLDOWN = 35;
    private static final int MIN_INITIAL_DELAY = 5;
    private static final int MAX_INITIAL_DELAY = 25;

    private static final int LOST_SIGHT_TIMEOUT = 40;
    private static final int UNREACHABLE_TARGET_COOLDOWN = 100;

    private final DarkRavenEntity raven;

    private LivingEntity target;
    private int chargeCooldown;
    private int lostSightTicks;
    private boolean rolledTeleportThisWindow;

    public DarkRavenAttackGoal(DarkRavenEntity raven) {
        this.raven = raven;
        this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        LivingEntity currentTarget = raven.getTarget();

        return currentTarget != null
                && currentTarget.isAlive()
                && !raven.isCharging()
                && !raven.isTargetTemporarilyUnreachable(currentTarget);
    }

    @Override
    public boolean canContinueToUse() {
        LivingEntity currentTarget = raven.getTarget();

        return raven.isAlive()
                && currentTarget != null
                && currentTarget.isAlive()
                && !raven.isTargetTemporarilyUnreachable(currentTarget);
    }

    @Override
    public void start() {
        target = raven.getTarget();
        lostSightTicks = 0;

        chargeCooldown = MIN_INITIAL_DELAY
                + raven.getRandom().nextInt(
                MAX_INITIAL_DELAY - MIN_INITIAL_DELAY + 1
        );

        rolledTeleportThisWindow = false;
    }

    @Override
    public void stop() {
        target = null;
        lostSightTicks = 0;

        if (raven.isCharging()) {
            raven.stopCharge(false);
        }

        raven.stopMoving();
    }

    @Override
    public void tick() {
        target = raven.getTarget();

        if (target == null || !target.isAlive()) {
            abandonTarget(false);
            return;
        }

        if (raven.isTargetTemporarilyUnreachable(target)) {
            abandonTarget(false);
            return;
        }

        if (raven.isCharging()) {
            return;
        }

        if (!raven.hasLineOfSight(target)) {
            lostSightTicks++;

            if (lostSightTicks >= LOST_SIGHT_TIMEOUT) {
                abandonTarget(true);
                return;
            }

            raven.moveToRange(target, CHARGE_MIN_RANGE, 0.6D);
            return;
        }

        lostSightTicks = 0;
        raven.faceTarget(target, 12.0F);

        if (chargeCooldown > 0) {
            chargeCooldown--;
        }

        if (raven.consumeFinishedCharge()) {
            chargeCooldown = MIN_CHARGE_COOLDOWN
                    + raven.getRandom().nextInt(
                    MAX_CHARGE_COOLDOWN - MIN_CHARGE_COOLDOWN + 1
            );

            rolledTeleportThisWindow = false;
        }

        if (chargeCooldown <= 0 && !rolledTeleportThisWindow) {
            rolledTeleportThisWindow = true;

            if (raven.getRandom().nextFloat()
                    < DarkRavenEntity.TELEPORT_CHARGE_CHANCE) {
                raven.teleportChargeTo(target);
                return;
            }
        }

        double distance = raven.distanceTo(target);

        if (distance > CHARGE_START_RANGE) {
            raven.moveToward(target, 1.25D);
            return;
        }

        if (chargeCooldown > 0) {
            raven.moveToRange(target, CHARGE_MIN_RANGE, 0.6D);
            return;
        }

        raven.startCharge(target);
    }

    private void abandonTarget(boolean markUnreachable) {
        LivingEntity currentTarget = raven.getTarget();

        if (raven.isCharging()) {
            raven.stopCharge(false);
        }

        if (markUnreachable && currentTarget != null) {
            raven.markTargetUnreachable(
                    currentTarget,
                    UNREACHABLE_TARGET_COOLDOWN
            );
        }

        raven.setTarget(null);

        target = null;
        lostSightTicks = 0;
        chargeCooldown = 0;
        rolledTeleportThisWindow = false;

        raven.stopMoving();
    }
}