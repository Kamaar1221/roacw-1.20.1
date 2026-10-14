package net.kamaarion.roacw;

import com.mojang.logging.LogUtils;
import io.redspace.ironsspellbooks.render.SpellBookCurioRenderer;
import net.kamaarion.roacw.client.others.ClientTickHandler;
import net.kamaarion.roacw.client.scroll.ROACWScrollModelEvents;
import net.kamaarion.roacw.effects.effect_extras.ROACWLivingEntityEvents;
import net.kamaarion.roacw.entity.summon.hydra.HydraBodyEntity;
import net.kamaarion.roacw.entity.summon.hydra.HydraHead;
import net.kamaarion.roacw.events.ClientEvents;
import net.kamaarion.roacw.events.ServerEvents;
import net.kamaarion.roacw.items.curios.alchemical_decanter.AlchemicalDecanterCurioRenderer;
import net.kamaarion.roacw.items.curios.burst_sheath.BurstSheathCurioRenderer;
import net.kamaarion.roacw.items.curios.eldritch_soul_artifact.EldritchSoulArtifactCurioRenderer;
import net.kamaarion.roacw.items.curios.elemental_gauntlet.ElementalGauntletCurioRenderer;
import net.kamaarion.roacw.items.curios.elemental_quiver.ElementalQuiverCurioRenderer;
import net.kamaarion.roacw.items.curios.ethereal_talisman.EtherealTalismanCurioRenderer;
import net.kamaarion.roacw.items.curios.evasion_scarf.EvasionScarfCurioRenderer;
import net.kamaarion.roacw.items.curios.high_ruler_shield.HighRulerShieldCurioRenderer;
import net.kamaarion.roacw.items.curios.stasis_curse.StatisCurseCurioRenderer;
import net.kamaarion.roacw.items.curios.wings_of_rebirth.WingsOfRebirthCurioRenderer;
import net.kamaarion.roacw.loot.ROACWLootModifiers;
import net.kamaarion.roacw.network.ModMessages;
import net.kamaarion.roacw.registeries.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;
import net.minecraftforge.event.entity.EntityAttributeCreationEvent;
import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLEnvironment;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import software.bernie.geckolib.GeckoLib;
import top.theillusivec4.curios.api.client.CuriosRendererRegistry;

@Mod(ROACW.MODID)
public class ROACW {
    public static final String MODID = "roacw";
    public static final Logger LOGGER = LogUtils.getLogger();

    public ROACW(FMLJavaModLoadingContext context) {
        IEventBus modEventBus = context.getModEventBus();

        GeckoLib.initialize();

        context.registerConfig(ModConfig.Type.COMMON, Config.SERVER_SPEC);
        context.registerConfig(ModConfig.Type.CLIENT, ClientConfig.SPEC);

        MinecraftForge.EVENT_BUS.register(new ServerEvents());
        MinecraftForge.EVENT_BUS.register(ClientTickHandler.class);
        MinecraftForge.EVENT_BUS.register(ROACWLivingEntityEvents.class);


        ROACWItemRegistry.register(modEventBus);
        ROACWBlocks.register(modEventBus);
        RoaCWCreativeTab.register(modEventBus);
        ROACWAttributeRegistry.register(modEventBus);
        ROACWSchoolRegistry.register(modEventBus);
        ROACWSpellRegistry.register(modEventBus);
        ROACWEntityRegistry.register(modEventBus);
        ROACWSoundRegistry.register(modEventBus);
        ROACWEffectRegistry.register(modEventBus);
        ROACWParticleRegistry.register(modEventBus);
        ROACWRecipeRegistry.register(modEventBus);
        ROACWScrollModelEvents.register(modEventBus);
        ROACWLootModifiers.register(modEventBus);

        modEventBus.addListener(this::commonSetup);
        modEventBus.addListener(this::entityAttributes);
        modEventBus.addListener(this::clientSetup);

        if (FMLEnvironment.dist == Dist.CLIENT) {
            modEventBus.register(ClientEvents.ModBusEvents.class);
        }

        MinecraftForge.EVENT_BUS.register(this);
        modEventBus.addListener(this::addCreative);
    }

    private void clientSetup(final FMLClientSetupEvent event) {
    }

    public static ResourceLocation MODID(String exo) {
        return null;
    }

    private void commonSetup(final FMLCommonSetupEvent event) {
        event.enqueueWork(ModMessages::register);
    }

    private void entityAttributes(EntityAttributeCreationEvent event) {
        event.put(
                ROACWEntityRegistry.HYDRA_BODY.get(),
                HydraBodyEntity.createAttributes().build()
        );

        event.put(
                ROACWEntityRegistry.HYDRA_HEAD.get(),
                HydraHead.createAttributes().build()
        );
    }

    private void addCreative(BuildCreativeModeTabContentsEvent event) {
    }

    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event) {
    }

    @Mod.EventBusSubscriber(
            modid = MODID,
            bus = Mod.EventBusSubscriber.Bus.MOD,
            value = Dist.CLIENT
    )
    public static class ClientModEvents {

        @SubscribeEvent
        public static void onClientSetup(FMLClientSetupEvent event) {
            CuriosRendererRegistry.register(
                    ROACWItemRegistry.STATIS_CURSE.get(),
                    StatisCurseCurioRenderer::new
            );

            CuriosRendererRegistry.register(
                    ROACWItemRegistry.EVASION_SCARF.get(),
                    EvasionScarfCurioRenderer::new
            );

            CuriosRendererRegistry.register(
                    ROACWItemRegistry.ELEMENTAL_GAUNTLET.get(),
                    ElementalGauntletCurioRenderer::new
            );

            CuriosRendererRegistry.register(
                    ROACWItemRegistry.BURST_SHEATH.get(),
                    BurstSheathCurioRenderer::new
            );

            CuriosRendererRegistry.register(
                    ROACWItemRegistry.NIGHTMARE_TOME.get(),
                    SpellBookCurioRenderer::new
            );

            CuriosRendererRegistry.register(
                    ROACWItemRegistry.AURIC_CODEX.get(),
                    SpellBookCurioRenderer::new
            );

            CuriosRendererRegistry.register(
                    ROACWItemRegistry.EARTHEN_PALADIN_OATH.get(),
                    SpellBookCurioRenderer::new
            );

            CuriosRendererRegistry.register(
                    ROACWItemRegistry.HIGH_RULER_SHIELD.get(),
                    HighRulerShieldCurioRenderer::new
            );

            CuriosRendererRegistry.register(
                    ROACWItemRegistry.ALCHEMICAL_DECANTER.get(),
                    AlchemicalDecanterCurioRenderer::new
            );

            CuriosRendererRegistry.register(
                    ROACWItemRegistry.ETHEREAL_TALISMAN.get(),
                    EtherealTalismanCurioRenderer::new
            );

            CuriosRendererRegistry.register(
                    ROACWItemRegistry.ELEMENTAL_QUIVER.get(),
                    ElementalQuiverCurioRenderer::new
            );

            CuriosRendererRegistry.register(
                    ROACWItemRegistry.ELDRITCH_SOUL_ARTIFACT.get(),
                    EldritchSoulArtifactCurioRenderer::new
            );

            CuriosRendererRegistry.register(
                    ROACWItemRegistry.WINGS_OF_REBIRTH.get(),
                    WingsOfRebirthCurioRenderer::new
            );
        }
    }

    public static ResourceLocation id(@NotNull String path) {
        return ResourceLocation.fromNamespaceAndPath(
                ROACW.MODID,
                path
        );
    }
}