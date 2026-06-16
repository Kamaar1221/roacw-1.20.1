package net.kamaarion.roacw.registeries;

import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;
import net.kamaarion.roacw.ROACW;

public class RoaCWCreativeTab {
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TAB =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, ROACW.MODID);

    public static final RegistryObject<CreativeModeTab> ROACW_EQUIPMENT = CREATIVE_MODE_TAB.register("roacw_items",
            () -> CreativeModeTab.builder()
                    .icon(() -> new ItemStack(ROACWItemRegistry.AURIC_TESLA_ROYAL_HELM.get()))
                    .title(Component.translatable("creativetab.roacw.equipment"))
                    .displayItems((itemDisplayParameters, output) -> {
                        output.accept(ROACWItemRegistry.MARS_VISOR.get());
                        output.accept(ROACWItemRegistry.MARS_ENGINE.get());
                        output.accept(ROACWItemRegistry.MARS_LEG_GUARDS.get());
                        output.accept(ROACWItemRegistry.MARS_BOOSTERS.get());
                        output.accept(ROACWItemRegistry.AURIC_TESLA_ROYAL_HELM.get());
                        output.accept(ROACWItemRegistry.AURIC_TESLA_CUIRASS.get());
                        output.accept(ROACWItemRegistry.AURIC_TESLA_CUISSES.get());
                        output.accept(ROACWItemRegistry.AURIC_TESLA_BOOTS.get());
                        output.accept(ROACWItemRegistry.FEARMONGER_GREATHELM.get());
                        output.accept(ROACWItemRegistry.FEARMONGER_PLATEMAIL.get());
                        output.accept(ROACWItemRegistry.FEARMONGER_LEGPLATES.get());
                        output.accept(ROACWItemRegistry.FEARMONGER_GREAVES.get());
                        output.accept(ROACWItemRegistry.STATIS_CURSE.get());
                        output.accept(ROACWItemRegistry.EVASION_SCARF.get());
                        output.accept(ROACWItemRegistry.ELEMENTAL_GAUNTLET.get());
                        output.accept(ROACWItemRegistry.MURASAMA_BLADE.get());
                        output.accept(ROACWItemRegistry.BURST_SHEATH.get());
                        output.accept(ROACWItemRegistry.NIGHTMARE_TOME.get());
                    })
                    .build());

    public static final RegistryObject<CreativeModeTab> ROACW_ITEMS = CREATIVE_MODE_TAB.register("roacw_blocks",
            () -> CreativeModeTab.builder()
                    .icon(() -> new ItemStack(ROACWItemRegistry.ASCENDANT_SPIRIT_ESSENCE.get()))
                    .title(Component.translatable("creativetab.roacw.items"))
                    .displayItems((itemDisplayParameters, output) -> {
                        output.accept(ROACWBlocks.AURIC_ORE.get());
                        output.accept(ROACWBlocks.DEEPSLATE_AURIC_ORE.get());
                        output.accept(ROACWBlocks.RAW_AURIC_BLOCK.get());
                        output.accept(ROACWBlocks.AURIC_BLOCK.get());
                        output.accept(ROACWItemRegistry.FRIED_CHICKEN.get());
                        output.accept(ROACWItemRegistry.RAW_AURIC_CHUNK.get());
                        output.accept(ROACWItemRegistry.AURIC_INGOT.get());
                        output.accept(ROACWItemRegistry.CHARGED_AURIC_INGOT.get());
                        output.accept(ROACWItemRegistry.ASCENDANT_SPIRIT_ESSENCE.get());
                    })
                    .build());

    public static void register(IEventBus eventBus) {
        CREATIVE_MODE_TAB.register(eventBus);
    }
}
