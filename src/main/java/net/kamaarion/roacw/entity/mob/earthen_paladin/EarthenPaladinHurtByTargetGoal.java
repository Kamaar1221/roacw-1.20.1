package net.kamaarion.roacw.entity.mob.earthen_paladin;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.player.Player;

public class EarthenPaladinHurtByTargetGoal extends HurtByTargetGoal {

    private final EarthenPaladinEntity paladin;

    public EarthenPaladinHurtByTargetGoal(EarthenPaladinEntity paladin) {
        super(paladin);
        this.paladin = paladin;
    }

    @Override
    public boolean canUse() {
        if (!super.canUse()) {
            return false;
        }

        LivingEntity attacker = this.paladin.getLastHurtByMob();

        if (attacker == null) {
            return false;
        }

        // Never retaliate against another Earthen Paladin.
        if (attacker instanceof EarthenPaladinEntity) {
            return false;
        }

        // Only retaliate against players or hostile mobs.
        return attacker instanceof Enemy || attacker instanceof Player;
    }
}