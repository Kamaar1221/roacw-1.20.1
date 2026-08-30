package net.kamaarion.roacw.entity.summon.belladonna_spirit;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;

import java.util.EnumSet;

public class BelladonnaSpiritWanderGoal extends Goal {

    private static final double IDLE_RADIUS = 3.5D;
    private static final double RETURN_DISTANCE = 10.0D;
    private static final double HOVER_HEIGHT = 2.0D;

    private final BelladonnaSpiritEntity spirit;

    public BelladonnaSpiritWanderGoal(BelladonnaSpiritEntity spirit) {
        this.spirit = spirit;
        this.setFlags(EnumSet.of(Goal.Flag.MOVE));
    }

    @Override
    public boolean canUse() {
        return spirit.getTarget() == null
                && !spirit.getMoveControl().hasWanted()
                && spirit.getRandom().nextInt(reducedTickDelay(10)) == 0;
    }

    @Override
    public boolean canContinueToUse() {
        return false;
    }

    @Override
    public void tick() {

        LivingEntity owner = spirit.getSummonOwner();

        if (owner == null)
            return;

        // Too far away? Fly straight back to the owner.
        if (spirit.distanceToSqr(owner) > RETURN_DISTANCE * RETURN_DISTANCE) {

            spirit.getMoveControl().setWantedPosition(
                    owner.getX(),
                    owner.getY() + HOVER_HEIGHT,
                    owner.getZ(),
                    0.35D
            );

            return;
        }

        // Pick a random hover point around the owner.
        double angle = spirit.getRandom().nextDouble() * Math.PI * 2.0D;
        double radius = 1.5D + spirit.getRandom().nextDouble() * IDLE_RADIUS;

        double x = owner.getX() + Math.cos(angle) * radius;
        double z = owner.getZ() + Math.sin(angle) * radius;
        double y = owner.getY() + HOVER_HEIGHT + (spirit.getRandom().nextDouble() - 0.5D);

        BlockPos pos = BlockPos.containing(x, y, z);

        if (spirit.level().isEmptyBlock(pos)) {

            spirit.getMoveControl().setWantedPosition(
                    x,
                    y,
                    z,
                    0.18D
            );

            spirit.getLookControl().setLookAt(
                    owner,
                    180.0F,
                    20.0F
            );
        }
    }
}