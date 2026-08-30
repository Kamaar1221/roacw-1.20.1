package net.kamaarion.roacw.entity.summon.dark_raven;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;

import java.util.EnumSet;

/**
 * Dark Raven's melee charge attack goal.
 *
 * Each raven has its own randomized attack delay so groups of ravens
 * naturally stagger their charges instead of attacking simultaneously.
 */
public class DarkRavenAttackGoal extends Goal {

    private final DarkRavenEntity raven;

    /*
     * ------------------------------------------------------------------
     * CHARGE SETTINGS
     * ------------------------------------------------------------------
     */

    /**
     * Distance at which the raven begins considering a charge.
     */
    private static final double CHARGE_START_RANGE = 10.0D;

    /**
     * Preferred distance to maintain while waiting for a charge.
     */
    private static final double CHARGE_MIN_RANGE = 7.5D;

    /**
     * Minimum delay between completed charges.
     *
     * 15 ticks = 0.75 seconds.
     */
    private static final int MIN_CHARGE_COOLDOWN = 15;

    /**
     * Maximum delay between completed charges.
     *
     * 35 ticks = 1.75 seconds.
     */
    private static final int MAX_CHARGE_COOLDOWN = 35;

    /**
     * Minimum randomized delay when the attack goal first starts.
     *
     * This prevents a group of freshly summoned ravens from all
     * immediately charging on the same tick.
     */
    private static final int MIN_INITIAL_DELAY = 5;

    /**
     * Maximum randomized delay when the attack goal first starts.
     */
    private static final int MAX_INITIAL_DELAY = 25;

    private LivingEntity target;

    private int chargeCooldown = 0;

    /**
     * Whether this raven has already rolled its teleport-charge
     * chance for the CURRENT cooldown window.
     *
     * Without this, checking DarkRavenEntity.TELEPORT_CHARGE_CHANCE
     * every tick while the raven waits to be ready would effectively
     * guarantee a teleport-charge within a couple of ticks - a "20%
     * chance" would end up meaning something much closer to 100%.
     * Instead, the roll happens exactly once per cooldown window, so
     * the configured chance is what actually plays out.
     */
    private boolean rolledTeleportThisWindow = false;

    public DarkRavenAttackGoal(DarkRavenEntity raven) {
        this.raven = raven;

        this.setFlags(
                EnumSet.of(
                        Goal.Flag.MOVE,
                        Goal.Flag.LOOK
                )
        );
    }

    @Override
    public boolean canUse() {

        LivingEntity target =
                raven.getTarget();

        return target != null
                && target.isAlive();
    }

    @Override
    public boolean canContinueToUse() {

        LivingEntity target =
                raven.getTarget();

        return target != null
                && target.isAlive()
                && raven.isAlive();
    }

    @Override
    public void start() {

        raven.stopMoving();

        target =
                raven.getTarget();

        /*
         * Give every raven its own initial delay.
         *
         * This is especially useful when multiple ravens acquire the
         * same target on the same tick.
         */
        chargeCooldown =
                MIN_INITIAL_DELAY
                        + raven.getRandom().nextInt(
                        MAX_INITIAL_DELAY
                                - MIN_INITIAL_DELAY
                                + 1
                );

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

        target =
                raven.getTarget();

        if (target == null
                || !target.isAlive()) {
            return;
        }

        /*
         * Keep the raven facing its target while it isn't charging.
         */
        if (!raven.isCharging()) {
            raven.faceTarget(
                    target,
                    12.0F
            );
        }

        /*
         * Count down the raven's personal attack timer.
         */
        if (chargeCooldown > 0) {
            chargeCooldown--;
        }

        /*
         * Detect when the previous charge has finished.
         *
         * The cooldown starts AFTER the charge is finished rather than
         * when it begins, preventing the raven from immediately starting
         * another charge after a long dash.
         */
        if (raven.consumeFinishedCharge()) {

            chargeCooldown =
                    MIN_CHARGE_COOLDOWN
                            + raven.getRandom().nextInt(
                            MAX_CHARGE_COOLDOWN
                                    - MIN_CHARGE_COOLDOWN
                                    + 1
                    );

            rolledTeleportThisWindow = false;
        }

        /*
         * Never interfere with an active charge.
         */
        if (raven.isCharging()) {
            return;
        }

        /*
         * -----------------------------------------------------------
         * TELEPORT-CHARGE ROLL
         * -----------------------------------------------------------
         *
         * Rolled exactly once per cooldown window, the moment this
         * raven's personal timer is ready. If it hits, the raven
         * blinks near the target and charges immediately, skipping
         * the normal approach/range-holding logic below entirely.
         */
        if (chargeCooldown <= 0
                && !rolledTeleportThisWindow) {

            rolledTeleportThisWindow = true;

            if (raven.getRandom().nextFloat()
                    < DarkRavenEntity.TELEPORT_CHARGE_CHANCE) {

                raven.teleportChargeTo(target);

                return;
            }
        }

        double distance =
                raven.distanceTo(target);

        /*
         * Target is too far away.
         *
         * Move toward it normally until it is within charge range.
         */
        if (distance > CHARGE_START_RANGE) {

            raven.moveToward(
                    target,
                    1.25D
            );

            return;
        }

        /*
         * Still waiting for this raven's personal attack timer,
         * or the raven cannot currently see the target.
         */
        if (chargeCooldown > 0
                || !raven.hasLineOfSight(target)) {

            raven.moveToRange(
                    target,
                    CHARGE_MIN_RANGE,
                    0.6D
            );

            return;
        }

        /*
         * The raven is ready to attack.
         */
        raven.startCharge(target);
    }
}