package net.kamaarion.roacw.client.firevisuals;

import net.kamaarion.roacw.ROACW;
import net.kamaarion.roacw.registeries.ROACWEffectRegistry;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

@Mod.EventBusSubscriber(modid = ROACW.MODID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
public final class ROACWFireTypes {

    private ROACWFireTypes() {
    }

    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(ROACWFireTypes::registerAll);
    }

    private static void registerAll() {
        CustomFireRegistry.register(
                CustomFireType.builder(ROACW.id("shadowflame"))
                        .effect(ROACWEffectRegistry.SHADOWFLAME)
                        .texture(ROACW.id("block/shadowflame_fire"))
                        .build());

        CustomFireRegistry.register(
                CustomFireType.builder(ROACW.id("brimstone_flames"))
                        .effect(ROACWEffectRegistry.BRIMSTONE_FLAMES)
                        .texture(ROACW.id("block/brimstone_flames_fire"))
                        .build());
    }
}