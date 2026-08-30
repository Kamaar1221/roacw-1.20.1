package net.kamaarion.roacw.entity.summon.plague_charger;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;

import java.util.EnumSet;

public class PlagueChargerAttackGoal extends Goal {

    private final PlagueChargerEntity charger;

    private record AttackData(String animation, int duration, int actionFrame) {}

    private static final AttackData STINGER =
            new AttackData("instant_cast", 8, 4);

    private static final double CHARGE_START_RANGE = 10.0D;
    private static final double CHARGE_MIN_RANGE = 7.5D;

    private static final double STINGER_MIN_RANGE = 8.0D;
    private static final double STINGER_IDEAL_RANGE = 11.0D;
    private static final double STINGER_MAX_RANGE = 15.0D;

    private static final int STINGER_COOLDOWN = 15;
    private static final int CHARGE_COOLDOWN = 20;

    private AttackData currentAttack;

    private LivingEntity target;

    private int attackTimer = -1;

    private int stingerCooldown = 0;
    private int chargeCooldown = 0;

    public PlagueChargerAttackGoal(PlagueChargerEntity charger) {
        this.charger = charger;
        this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        LivingEntity target = charger.getTarget();
        return target != null && target.isAlive();
    }

    @Override
    public boolean canContinueToUse() {
        LivingEntity target = charger.getTarget();

        return target != null
                && target.isAlive()
                && charger.isAlive();
    }

    @Override
    public void start() {

        charger.stopMoving();

        target = charger.getTarget();
        currentAttack = null;
        attackTimer = -1;
    }

    @Override
    public void stop() {

        target = null;

        if (currentAttack != null) {
            charger.stopTriggeredAnimation("controller", currentAttack.animation());
        }

        currentAttack = null;
        attackTimer = -1;

        charger.stopMoving();

        if (charger.isCharging()) {
            charger.stopCharge(false);
        }
    }

    @Override
    public void tick() {

        target = charger.getTarget();

        if (target == null || !target.isAlive()) {
            return;
        }

        if (!charger.isCharging()) {
            charger.faceTarget(target, 12.0F);
        }

        if (stingerCooldown > 0)
            stingerCooldown--;

        if (chargeCooldown > 0)
            chargeCooldown--;

        // The cooldown used to be loaded in tickCharger() the moment
        // startCharge() was called, and ticked down concurrently with the
        // dash. Dashes can run up to CHARGE_MAX_TICKS (40) while the
        // cooldown is only 20, so it routinely hit zero before the dash
        // even finished - letting tickCharger() immediately re-charge on
        // the very next tick, straight back toward a target the first dash
        // had already overshot past. Load it here instead, exactly when
        // the dash actually completes, so a full cooldown always follows.
        if (charger.consumeFinishedCharge()) {
            chargeCooldown = CHARGE_COOLDOWN;
        }

        if (charger.isCharging()) {
            return;
        }

        if (attackTimer >= 0) {
            tickStingerWindup();
            return;
        }

        // Wild (unsummoned) chargers don't have a squad role to divide labor
        // with - they need both behaviors in one entity, picked by distance
        // instead of an assigned CombatRole.
        if (charger.getSummonOwner() == null) {
            tickHybrid();
            return;
        }

        switch (charger.getCombatRole()) {

            case CHARGER -> tickCharger();

            case RANGED -> tickRanged();
        }
    }

    private void tickHybrid() {

        double distance = charger.distanceTo(target);

        // Close enough to charge - same thresholds/cooldown handling as
        // tickCharger().
        if (distance <= CHARGE_START_RANGE) {

            if (chargeCooldown > 0 || !charger.hasLineOfSight(target)) {
                charger.moveToRange(target, CHARGE_MIN_RANGE, 0.6D);
                return;
            }

            charger.startCharge(target);
            return;
        }

        // Otherwise behave like tickRanged() for anything beyond charge
        // range.
        if (distance > STINGER_MAX_RANGE) {
            charger.moveToRange(target, STINGER_IDEAL_RANGE, 1.0D);
            return;
        }

        charger.moveToRange(target, STINGER_IDEAL_RANGE, 0.45D);

        if (stingerCooldown <= 0) {
            startStinger();
        }
    }

    private void tickCharger() {

        double distance = charger.distanceTo(target);

        // Too far away.
        if (distance > CHARGE_START_RANGE) {
            charger.moveToward(target, 1.25D);
            return;
        }

        // Wait for cooldown before another charge, or if there's no clear
        // line of sight (e.g. a wall between us and the target) - dashing
        // in a straight line at 1.6 blocks/tick into an obstruction is what
        // was causing chargers to visibly tunnel through thin walls.
        // Reposition toward the target instead of standing fully still -
        // after a charge overshoots past the target, faceTarget() alone has
        // to rotate the charger a large amount to re-orient, and with zero
        // translation that reads as spinning in place. Moving while turning
        // hides the re-orientation.
        if (chargeCooldown > 0 || !charger.hasLineOfSight(target)) {
            charger.moveToRange(target, CHARGE_MIN_RANGE, 0.6D);
            return;
        }

        charger.startCharge(target);
    }

    private void tickRanged() {

        double distance = charger.distanceTo(target);

        // Too far away.
        if (distance > STINGER_MAX_RANGE) {

            charger.moveToRange(target, STINGER_IDEAL_RANGE, 1.0D);
            return;
        }

        // Too close.
        if (distance < STINGER_MIN_RANGE) {

            charger.moveToRange(target, STINGER_IDEAL_RANGE, 1.0D);
            return;
        }

        // Stay moving slightly while maintaining range.
        charger.moveToRange(target, STINGER_IDEAL_RANGE, 0.45D);

        if (stingerCooldown <= 0) {
            startStinger();
        }
    }

    private void startStinger() {

        currentAttack = STINGER;
        attackTimer = STINGER.duration();

        charger.triggerAnim("controller", STINGER.animation());
    }

    private void tickStingerWindup() {

        attackTimer--;

        if (attackTimer == currentAttack.actionFrame()) {
            charger.performRangedAttack(target, 1.0F);
        }

        if (attackTimer <= 0) {

            charger.stopTriggeredAnimation("controller", "instant_cast");

            stingerCooldown = STINGER_COOLDOWN;

            currentAttack = null;
            attackTimer = -1;
        }
    }
}