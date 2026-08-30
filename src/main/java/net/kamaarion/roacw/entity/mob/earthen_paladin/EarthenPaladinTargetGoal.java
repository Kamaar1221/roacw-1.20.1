package net.kamaarion.roacw.entity.mob.earthen_paladin;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.player.Player;

public class EarthenPaladinTargetGoal
        extends NearestAttackableTargetGoal<LivingEntity> {

    public EarthenPaladinTargetGoal(EarthenPaladinEntity paladin) {
        super(
                paladin,
                LivingEntity.class,
                10,
                true,
                false,
                target ->
                        !(target instanceof EarthenPaladinEntity)
                                && (target instanceof Enemy
                                || target instanceof Player)
        );
    }
}