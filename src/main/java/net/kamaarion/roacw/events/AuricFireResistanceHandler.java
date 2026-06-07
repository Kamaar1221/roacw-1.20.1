package net.kamaarion.roacw.events;

import net.kamaarion.roacw.effects.AuricChargeEffect;
import net.kamaarion.roacw.registeries.ROACWEffectRegistry;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = "roacw", bus = Mod.EventBusSubscriber.Bus.FORGE)
public class AuricFireResistanceHandler {

    @SubscribeEvent
    public static void onLivingDamage(LivingDamageEvent event) {
        LivingEntity entity = event.getEntity();

        if (entity != null && entity.hasEffect(ROACWEffectRegistry.AURIC_CHARGE.get())) {

            if (event.getSource().is(DamageTypeTags.IS_FIRE)) {

                event.setCanceled(true);

                entity.clearFire();
            }
        }
    }
}
