package net.kamaarion.roacw.entity.summon.belladonna_spirit;

import net.minecraft.util.Mth;
import net.minecraft.world.entity.ai.control.MoveControl;
import net.minecraft.world.phys.Vec3;

public class BelladonnaSpiritMoveControl extends MoveControl {

    private final BelladonnaSpiritEntity spirit;

    public BelladonnaSpiritMoveControl(BelladonnaSpiritEntity spirit) {
        super(spirit);
        this.spirit = spirit;
    }

    @Override
    public void tick() {
        if (this.operation == Operation.MOVE_TO) {
            Vec3 delta = new Vec3(this.wantedX - spirit.getX(), this.wantedY - spirit.getY(), this.wantedZ - spirit.getZ());
            double dist = delta.length();

            if (dist < spirit.getBoundingBox().getSize()) {
                this.operation = Operation.WAIT;
                spirit.setDeltaMovement(spirit.getDeltaMovement().scale(0.5D));
            } else {
                spirit.setDeltaMovement(spirit.getDeltaMovement().add(delta.scale(this.speedModifier * 0.05D / dist)));

                if (spirit.getTarget() == null) {
                    Vec3 motion = spirit.getDeltaMovement();
                    spirit.setYRot(-((float) Mth.atan2(motion.x, motion.z)) * (180F / (float) Math.PI));
                    spirit.yBodyRot = spirit.getYRot();
                } else {
                    double dx = spirit.getTarget().getX() - spirit.getX();
                    double dz = spirit.getTarget().getZ() - spirit.getZ();
                    spirit.setYRot(-((float) Mth.atan2(dx, dz)) * (180F / (float) Math.PI));
                    spirit.yBodyRot = spirit.getYRot();
                }
            }
        }
    }
}