package net.kamaarion.roacw.client.scroll;

import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.client.event.ModelEvent;
import net.minecraftforge.eventbus.api.IEventBus;

public final class ROACWScrollModelEvents {

    private ROACWScrollModelEvents() {
    }

    public static void register(IEventBus modEventBus) {
        modEventBus.addListener(
                ROACWScrollModelEvents::registerAdditionalModels
        );
    }

    private static void registerAdditionalModels(
            ModelEvent.RegisterAdditional event
    ) {
        ROACWScrollTextureRegistry.register();

        for (ResourceLocation modelLocation :
                ROACWScrollTextureRegistry.getRegisteredModels().values()) {

            event.register(modelLocation);
        }
    }
}