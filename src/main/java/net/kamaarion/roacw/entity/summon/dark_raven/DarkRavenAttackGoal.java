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

    private final DarkRavenEntity raven;

    private LivingEntity target;
    private int chargeCooldown = 0;
    private boolean rolledTeleportThisWindow = false;

    public DarkRavenAttackGoal(DarkRavenEntity raven) {
        this.raven = raven;
        this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        LivingEntity target = raven.getTarget();
        return target != null && target.isAlive();
    }

    @Override
    public boolean canContinueToUse() {
        LivingEntity target = raven.getTarget();
        return target != null && target.isAlive() && raven.isAlive();
    }

    @Override
    public void start() {
        raven.stopMoving();
        target = raven.getTarget();

        chargeCooldown = MIN_INITIAL_DELAY
                + raven.getRandom().nextInt(MAX_INITIAL_DELAY - MIN_INITIAL_DELAY + 1);

        rolledTeleportThisWindow = false;
    }

    @Override
    public void stop() {
        target = null;
        raven.stopMoving();

        if (raven.isCharging()) {
            raven.stopCharge(false);
        }
    }

    @Override
    public void tick() {
        target = raven.getTarget();

        if (target == null || !target.isAlive()) {
            return;
        }

        if (!raven.isCharging()) {
            raven.faceTarget(target, 12.0F);
        }

        if (chargeCooldown > 0) {
            chargeCooldown--;
        }

        if (raven.consumeFinishedCharge()) {
            chargeCooldown = MIN_CHARGE_COOLDOWN
                    + raven.getRandom().nextInt(MAX_CHARGE_COOLDOWN - MIN_CHARGE_COOLDOWN + 1);

            rolledTeleportThisWindow = false;
        }

        if (raven.isCharging()) {
            return;
        }

        if (chargeCooldown <= 0 && !rolledTeleportThisWindow) {
            rolledTeleportThisWindow = true;

            if (raven.getRandom().nextFloat() < DarkRavenEntity.TELEPORT_CHARGE_CHANCE) {
                raven.teleportChargeTo(target);
                return;
            }
        }

        double distance = raven.distanceTo(target);

        if (distance > CHARGE_START_RANGE) {
            raven.moveToward(target, 1.25D);
            return;
        }

        if (chargeCooldown > 0 || !raven.hasLineOfSight(target)) {
            raven.moveToRange(target, CHARGE_MIN_RANGE, 0.6D);
            return;
        }

        raven.startCharge(target);
    }
}