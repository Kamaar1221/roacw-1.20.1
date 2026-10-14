package net.kamaarion.roacw.registeries;

import net.kamaarion.roacw.ROACW;
import net.kamaarion.roacw.entity.mob.earthen_paladin.EarthenPaladinEntity;
import net.kamaarion.roacw.entity.summon.baby_plaguebringer.BabyPlaguebringerEntity;
import net.kamaarion.roacw.entity.summon.belladonna_spirit.BelladonnaSpiritEntity;
import net.kamaarion.roacw.entity.summon.dark_raven.DarkRavenEntity;
import net.kamaarion.roacw.entity.summon.endo_hydra.EndoHydraEntity;
import net.kamaarion.roacw.entity.summon.mini_plaguebringer.MiniPlaguebringerEntity;
import net.kamaarion.roacw.entity.mob.plague_charger.PlagueChargerEntity;
import net.kamaarion.roacw.entity.summon.virili.ViriliEntity;
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
        event.put(ROACWEntityRegistry.ENDO_HYDRA.get(), EndoHydraEntity.createAttributes().build());
        event.put(ROACWEntityRegistry.MINI_PLAGUEBRINGER.get(), MiniPlaguebringerEntity.createAttributes().build());
        event.put(ROACWEntityRegistry.VIRILI.get(), ViriliEntity.createAttributes().build());
        event.put(ROACWEntityRegistry.BABY_PLAGUEBRINGER.get(), BabyPlaguebringerEntity.createAttributes().build());



    }
}