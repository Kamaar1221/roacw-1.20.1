package net.kamaarion.roacw;

import com.mojang.logging.LogUtils;
import io.redspace.ironsspellbooks.render.SpellBookCurioRenderer;
import net.kamaarion.roacw.client.CustomAnimatedParticle;
import net.kamaarion.roacw.events.ServerEvents;
import net.kamaarion.roacw.items.curios.burst_sheath.BurstSheathCurioRenderer;
import net.kamaarion.roacw.items.curios.high_ruler_shield.HighRulerShieldCurioRenderer;
import net.kamaarion.roacw.network.ModMessages;
import net.kamaarion.roacw.registeries.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.client.event.RegisterParticleProvidersEvent;
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;
import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModLoadingContext; // Added import for registration
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.kamaarion.roacw.items.curios.elemental_gauntlet.ElementalGauntletCurioRenderer;
import net.kamaarion.roacw.items.curios.evasion_scarf.EvasionScarfCurioRenderer;
import net.kamaarion.roacw.items.curios.stasis_curse.StatisCurseCurioRenderer;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import top.theillusivec4.curios.api.client.CuriosRendererRegistry;

@Mod(ROACW.MODID)
public class ROACW {
    public static final String MODID = "roacw";
    private static final Logger LOGGER = LogUtils.getLogger();

    public ROACW(FMLJavaModLoadingContext context) {
        IEventBus modEventBus = context.getModEventBus();

        context.registerConfig(ModConfig.Type.CLIENT, Config.SPEC);
        // Registering the new server-side configurations safely without changing Client specifications
        ModLoadingContext.get().registerConfig(ModConfig.Type.SERVER, Config.SPEC);

        MinecraftForge.EVENT_BUS.register(new ServerEvents());
        // Register the Client Tick Handler to listen continuously for keybind presses
        MinecraftForge.EVENT_BUS.register(net.kamaarion.roacw.client.ClientTickHandler.class);

        ROACWItemRegistry.register(modEventBus);
        ROACWBlocks.register(modEventBus);
        RoaCWCreativeTab.register(modEventBus);
        ROACWAttributeRegistry.register(modEventBus);
        ROACWSchoolRegistry.register(modEventBus);
        ROACWSpellRegistries.register(modEventBus);
        ROACWEntityRegistry.register(modEventBus);
        ROACWSoundRegistry.register(modEventBus);
        ROACWEffectRegistry.register(modEventBus);
        ROACWParticleRegistry.register(modEventBus);

        modEventBus.addListener(this::commonSetup);
        MinecraftForge.EVENT_BUS.register(this);
        modEventBus.addListener(this::addCreative);
    }

    public static ResourceLocation MODID(String exo) {
        return null;
    }

    private void commonSetup(final FMLCommonSetupEvent event) {
        event.enqueueWork(ModMessages::register);
    }

    private void addCreative(BuildCreativeModeTabContentsEvent event) {
    }

    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event) {
    }

    @Mod.EventBusSubscriber(modid = MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
    public static class ClientModEvents {
        @SubscribeEvent
        public static void onClientSetup(FMLClientSetupEvent event) {
            CuriosRendererRegistry.register(ROACWItemRegistry.STATIS_CURSE.get(), StatisCurseCurioRenderer::new);
            CuriosRendererRegistry.register(ROACWItemRegistry.EVASION_SCARF.get(), EvasionScarfCurioRenderer::new);
            CuriosRendererRegistry.register(ROACWItemRegistry.ELEMENTAL_GAUNTLET.get(), ElementalGauntletCurioRenderer::new);
            CuriosRendererRegistry.register(ROACWItemRegistry.BURST_SHEATH.get(), BurstSheathCurioRenderer::new);
            CuriosRendererRegistry.register(ROACWItemRegistry.NIGHTMARE_TOME.get(), SpellBookCurioRenderer::new);
            CuriosRendererRegistry.register(ROACWItemRegistry.HIGH_RULER_SHIELD.get(), HighRulerShieldCurioRenderer::new);
        }

        @SubscribeEvent
        public static void registerParticleProviders(RegisterParticleProvidersEvent event) {
            event.registerSpriteSet(
                    ROACWParticleRegistry.SHADOWFLAME.get(),
                    CustomAnimatedParticle.Provider::new
            );
        }
    }

    public static ResourceLocation id(@NotNull String path) {
        return ResourceLocation.fromNamespaceAndPath(ROACW.MODID, path);
    }
}
