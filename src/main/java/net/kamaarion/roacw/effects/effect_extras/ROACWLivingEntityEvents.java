package net.kamaarion.roacw.effects.effect_extras;

import net.kamaarion.roacw.effects.DreadEffect;
import net.kamaarion.roacw.registeries.ROACWEffectRegistry;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.LivingChangeTargetEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class ROACWLivingEntityEvents {

    @SubscribeEvent
    public static void onLivingChangeTarget(LivingChangeTargetEvent event) {
        LivingEntity entity = event.getEntity();

        if (!(entity instanceof Mob mob)) {
            return;
        }

        if (!mob.hasEffect(ROACWEffectRegistry.DREAD.get())) {
            return;
        }

        LivingEntity fearSource = DreadEffect.getFearSource(mob);

        if (fearSource == null || !fearSource.isAlive()) {
            return;
        }

        if (event.getNewTarget() == fearSource) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void onLivingAttack(LivingAttackEvent event) {
        if (!(event.getSource().getEntity() instanceof Mob mob)) {
            return;
        }

        if (!mob.hasEffect(ROACWEffectRegistry.DREAD.get())) {
            return;
        }

        LivingEntity fearSource = DreadEffect.getFearSource(mob);

        if (fearSource == null || !fearSource.isAlive()) {
            return;
        }

        if (event.getEntity() == fearSource) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void onEntityJoinLevel(EntityJoinLevelEvent event) {
        Entity entity = event.getEntity();

        if (!(entity instanceof Projectile projectile)) {
            return;
        }

        if (!(projectile.getOwner() instanceof Mob mob)) {
            return;
        }

        if (!mob.hasEffect(ROACWEffectRegistry.DREAD.get())) {
            return;
        }

        LivingEntity fearSource = DreadEffect.getFearSource(mob);

        if (fearSource == null || !fearSource.isAlive()) {
            return;
        }

        event.setCanceled(true);
    }
}