package net.kamaarion.roacw.effects.effect_extras;

import net.kamaarion.roacw.entity.mob.plague_charger.PlagueChargerEntity;
import net.kamaarion.roacw.registeries.ROACWEffectRegistry;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.List;

@Mod.EventBusSubscriber
public class PlagueEvents {

    private static final double SPREAD_RADIUS = 5.0D;

    @SubscribeEvent
    public static void onLivingDeath(LivingDeathEvent event) {
        LivingEntity dying = event.getEntity();
        Level level = dying.level();
        if (level.isClientSide) return;

        MobEffectInstance plague = dying.getEffect(ROACWEffectRegistry.PLAGUE.get());
        if (plague == null) return;

        AABB spreadArea = dying.getBoundingBox().inflate(SPREAD_RADIUS);

        Entity attacker = event.getSource().getEntity();

        List<LivingEntity> nearby = level.getEntitiesOfClass(LivingEntity.class, spreadArea,
                e -> e != dying && e != attacker && e.isAlive()
                        && !(e instanceof Player)
                        && !(e instanceof PlagueChargerEntity));

        for (LivingEntity target : nearby) {
            target.addEffect(new MobEffectInstance(
                    ROACWEffectRegistry.PLAGUE.get(),
                    plague.getDuration(),
                    plague.getAmplifier(),
                    false,
                    false
            ));
        }
    }
}