package net.kamaarion.roacw.progression;

import net.kamaarion.roacw.ROACW;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.event.AddReloadListenerEvent;
import net.minecraftforge.event.entity.living.MobSpawnEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;

@Mod.EventBusSubscriber(modid = ROACW.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class SpawnGateEvents {

    @SubscribeEvent
    public static void onAddReloadListeners(AddReloadListenerEvent event) {
        event.addListener(new SpawnGateManager());
    }

    @SubscribeEvent
    public static void onFinalizeSpawn(MobSpawnEvent.FinalizeSpawn event) {
        ResourceLocation id = ForgeRegistries.ENTITY_TYPES.getKey(event.getEntity().getType());

        if (id == null) {
            return;
        }

        SpawnGateManager.Gate gate = SpawnGateManager.get(id);

        if (gate == null || !gate.spawnTypes().contains(event.getSpawnType())) {
            return;
        }

        if (!BossGateData.hasKilled(event.getLevel(), gate.boss())) {
            event.setSpawnCancelled(true);
        }
    }
}