package net.kamaarion.roacw.events;

import net.kamaarion.roacw.ROACW;
import net.kamaarion.roacw.entity.spells.earthly_virtue.EarthlyVirtueShardsRenderer;
import net.kamaarion.roacw.entity.spells.impaling_column.ImpalingColumnShardsRenderer;
import net.kamaarion.roacw.entity.spells.final_rend.FinalRendAoERenderer;
import net.kamaarion.roacw.entity.spells.quick_strike.QuickStrikeAoERenderer;
import net.kamaarion.roacw.registeries.ROACWEntityRegistry;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.client.event.RegisterParticleProvidersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = ROACW.MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = {Dist.CLIENT})
public class ClientSetup {
    @SubscribeEvent
    public static void registerEntityRenderers(EntityRenderersEvent.RegisterRenderers event)
    {
        event.registerEntityRenderer(ROACWEntityRegistry.QUICK_STRIKE.get(), QuickStrikeAoERenderer::new);
        event.registerEntityRenderer(ROACWEntityRegistry.FINAL_REND.get(), FinalRendAoERenderer::new);
        event.registerEntityRenderer(ROACWEntityRegistry.IMPALING_COLUMN_SHARDS.get(), ImpalingColumnShardsRenderer::new);
        event.registerEntityRenderer(ROACWEntityRegistry.EARTHLY_VIRTUE_SHARDS.get(), EarthlyVirtueShardsRenderer::new);

    }

    @SubscribeEvent
    public static void registerParticles(RegisterParticleProvidersEvent event)
    {
    }
}