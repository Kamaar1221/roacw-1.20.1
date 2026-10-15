package net.kamaarion.roacw.progression;

import net.kamaarion.roacw.registeries.ROACWTags;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;

@Mod.EventBusSubscriber(modid = "roacw", bus = Mod.EventBusSubscriber.Bus.FORGE)
public class BossGateEvents {

    @SubscribeEvent
    public static void onLivingDeath(LivingDeathEvent event) {
        LivingEntity entity = event.getEntity();

        if (entity.level().isClientSide) {
            return;
        }

        if (!entity.getType().is(ROACWTags.GATE_BOSSES)) {
            return;
        }

        ResourceLocation id = ForgeRegistries.ENTITY_TYPES.getKey(entity.getType());
        MinecraftServer server = entity.level().getServer();

        if (id != null && server != null) {
            BossGateData.markKilled(server, id);
        }
    }
}