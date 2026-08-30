package net.kamaarion.roacw.registeries;

import net.kamaarion.roacw.ROACW;
import net.kamaarion.roacw.entity.mob.earthen_paladin.EarthenPaladinEntity;
import net.kamaarion.roacw.entity.summon.belladonna_spirit.BelladonnaSpiritEntity;
import net.kamaarion.roacw.entity.summon.dark_raven.DarkRavenEntity;
import net.kamaarion.roacw.entity.summon.plague_charger.PlagueChargerEntity;
import net.minecraftforge.event.entity.EntityAttributeCreationEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = ROACW.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ROACWEntityAttributeRegistry {

    @SubscribeEvent
    public static void registerAttributes(EntityAttributeCreationEvent event) {
        event.put(ROACWEntityRegistry.BELLADONNA_SPIRIT.get(), BelladonnaSpiritEntity.createAttributes().build());
        event.put(ROACWEntityRegistry.PLAGUE_CHARGER.get(), PlagueChargerEntity.createAttributes().build());
        event.put(ROACWEntityRegistry.EARTHEN_PALADIN.get(), EarthenPaladinEntity.createAttributes().build());
        event.put(ROACWEntityRegistry.DARK_RAVEN.get(), DarkRavenEntity.createAttributes().build());
    }
}