package net.kamaarion.roacw;

import com.mojang.logging.LogUtils;
import mod.azure.azurelib.render.item.AzItemRendererRegistry;
import net.kamaarion.roacw.items.curios.burst_sheath.BurstSheathCurioRenderer;
import net.kamaarion.roacw.items.curios.burst_sheath.BurstSheathRenderer;
import net.kamaarion.roacw.items.curios.burst_sheath.BurstSheath;
import net.kamaarion.roacw.registeries.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;
import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.kamaarion.roacw.items.curios.elemental_gauntlet.ElementalGauntlet;
import net.kamaarion.roacw.items.curios.elemental_gauntlet.ElementalGauntletCurioRenderer;
import net.kamaarion.roacw.items.curios.evasion_scarf.EvasionScarf;
import net.kamaarion.roacw.items.curios.evasion_scarf.EvasionScarfCurioRenderer;
import net.kamaarion.roacw.items.curios.stasis_curse.StatisCurse;
import net.kamaarion.roacw.items.curios.stasis_curse.StatisCurseCurioRenderer;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import top.theillusivec4.curios.api.client.CuriosRendererRegistry;

// The value here should match an entry in the META-INF/mods.toml file
@Mod(ROACW.MODID)
public class ROACW
{
    // Define mod id in a common place for everything to reference
    public static final String MODID = "roacw";
    // Directly reference a slf4j logger
    private static final Logger LOGGER = LogUtils.getLogger();

    public ROACW(FMLJavaModLoadingContext context)
    {
        IEventBus modEventBus = context.getModEventBus();
        MinecraftForge.EVENT_BUS.register(new ServerEvents());

        ROACWItemRegistry.register(modEventBus);
        ROACWBlocks.register(modEventBus);
        RoaCWCreativeTab.register(modEventBus);
        ROACWAttributeRegistry.register(modEventBus);
        ROACWSchoolRegistry.register(modEventBus);
        ROACWSpellRegistries.register(modEventBus);
        ROACWEntityRegistry.register(modEventBus);
        ROACWSoundRegistry.register(modEventBus);
        ROACWEffectRegistry.register(modEventBus);


        // Register the commonSetup method for modloading
        modEventBus.addListener(this::commonSetup);


        // Register ourselves for server and other game events we are interested in
        MinecraftForge.EVENT_BUS.register(this);

        // Register the item to a creative tab
        modEventBus.addListener(this::addCreative);
    }

    public static ResourceLocation MODID(String exo) {
        return null;
    }

    private void commonSetup(final FMLCommonSetupEvent event)
    {
    }

    // Add the example block item to the building blocks tab
    private void addCreative(BuildCreativeModeTabContentsEvent event)
    {
    }

    // You can use SubscribeEvent and let the Event Bus discover methods to call
    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event)
    {
    }

    // You can use EventBusSubscriber to automatically register all static methods in the class annotated with @SubscribeEvent
    @Mod.EventBusSubscriber(modid = MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
    public static class ClientModEvents
    {
        @SubscribeEvent
        public static void onClientSetup(FMLClientSetupEvent event) {

            ROACWItemRegistry.getROACWItems().stream().filter(item -> item.get() instanceof StatisCurse)
                    .forEach((item) -> CuriosRendererRegistry.register(item.get(), StatisCurseCurioRenderer::new));
            ROACWItemRegistry.getROACWItems().stream().filter(item -> item.get() instanceof EvasionScarf)
                    .forEach((item) -> CuriosRendererRegistry.register(item.get(), EvasionScarfCurioRenderer::new));
            ROACWItemRegistry.getROACWItems().stream().filter(item -> item.get() instanceof ElementalGauntlet)
                    .forEach((item) -> CuriosRendererRegistry.register(item.get(), ElementalGauntletCurioRenderer::new));
            ROACWItemRegistry.getROACWItems().stream().filter(item -> item.get() instanceof BurstSheath)
                    .forEach((item) -> CuriosRendererRegistry.register(item.get(), BurstSheathCurioRenderer::new));
        }
    }
    public static ResourceLocation id(@NotNull String path)
    {
        return ResourceLocation.fromNamespaceAndPath(ROACW.MODID, path);
    }
}
