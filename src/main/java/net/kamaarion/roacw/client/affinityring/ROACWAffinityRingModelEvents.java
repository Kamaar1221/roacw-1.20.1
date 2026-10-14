package net.kamaarion.roacw.client.affinityring;

import net.kamaarion.roacw.ROACW;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ModelEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(
        modid = ROACW.MODID,
        bus = Mod.EventBusSubscriber.Bus.MOD,
        value = Dist.CLIENT
)
public final class ROACWAffinityRingModelEvents {

    private ROACWAffinityRingModelEvents() {
    }

    @SubscribeEvent
    public static void registerAdditionalModels(
            ModelEvent.RegisterAdditional event
    ) {

        ROACWAffinityRingTextureRegistry.register();


        for (ResourceLocation modelLocation :
                ROACWAffinityRingTextureRegistry
                        .getRegisteredModels()
                        .values()) {

            event.register(modelLocation);
        }
    }
}