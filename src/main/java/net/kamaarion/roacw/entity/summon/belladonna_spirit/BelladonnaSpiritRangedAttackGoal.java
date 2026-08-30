package net.kamaarion.roacw.entity.summon.belladonna_spirit;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;

import java.util.EnumSet;

public class BelladonnaSpiritRangedAttackGoal extends Goal {

    private static final double PREFERRED_RADIUS = 8.0D;
    private static final double TOO_CLOSE_RADIUS = 4.0D;

    private final BelladonnaSpiritEntity spirit;
    private final double speedModifier;
    private final int attackIntervalMin;
    private final float attackRadius;
    private final float attackRadiusSqr;

    private int attackTime = -1;

    public BelladonnaSpiritRangedAttackGoal(BelladonnaSpiritEntity spirit, double speedModifier, int attackIntervalMin, float attackRadius) {
        this.spirit = spirit;
        this.speedModifier = speedModifier;
        this.attackIntervalMin = attackIntervalMin;
        this.attackRadius = attackRadius;
        this.attackRadiusSqr = attackRadius * attackRadius;

        this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        LivingEntity target = spirit.getTarget();
        return target != null && target.isAlive();
    }

    @Override
    public boolean canContinueToUse() {
        return canUse();
    }

    @Override
    public void stop() {
        this.attackTime = -1;
    }

    @Override
    public void tick() {
        LivingEntity target = spirit.getTarget();
        if (target == null)
            return;

        double distSqr = spirit.distanceToSqr(target);
        boolean canSee = spirit.getSensing().hasLineOfSight(target);

        spirit.getLookControl().setLookAt(target, 30.0F, 30.0F);

        this.attackTime = canSee ? this.attackTime + 1 : -1;

        // Recalculate movement every so often
        if (!spirit.getMoveControl().hasWanted()
                || spirit.getRandom().nextInt(reducedTickDelay(15)) == 0) {

            double dx = spirit.getX() - target.getX();
            double dz = spirit.getZ() - target.getZ();

            double distance = Math.sqrt(dx * dx + dz * dz);

            if (distance < 0.001D) {
                distance = 1.0D;
                dx = 1.0D;
                dz = 0.0D;
            }

            dx /= distance;
            dz /= distance;

            double hx;
            double hz;

            if (distance < TOO_CLOSE_RADIUS) {

                // Fly directly away from the target.
                hx = target.getX() + dx * PREFERRED_RADIUS;
                hz = target.getZ() + dz * PREFERRED_RADIUS;

            } else {

                // Orbit around the target.
                double angle = Math.atan2(dz, dx);

                angle += spirit.getRandom().nextBoolean()
                        ? Math.toRadians(35)
                        : -Math.toRadians(35);

                hx = target.getX() + Math.cos(angle) * PREFERRED_RADIUS;
                hz = target.getZ() + Math.sin(angle) * PREFERRED_RADIUS;
            }

            double hy = target.getY() + 1.0D + spirit.getRandom().nextDouble();

            spirit.getMoveControl().setWantedPosition(
                    hx,
                    hy,
                    hz,
                    speedModifier
            );
        }

        // Fire when ready.
        if (canSee
                && this.attackTime >= attackIntervalMin
                && distSqr <= attackRadiusSqr) {

            spirit.performRangedAttack(target, 1.0F);
            this.attackTime = 0;
        }
    }
}