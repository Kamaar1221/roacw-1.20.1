package net.rev.roacw.registeries;

import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;
import net.rev.roacw.ROACW;

public class RoaCWCreativeTab {
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TAB =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, ROACW.MODID);

    public static final RegistryObject<CreativeModeTab> ROACW_ITEMS = CREATIVE_MODE_TAB.register("roacw", () -> CreativeModeTab.builder().icon(()-> new ItemStack(ROACWItems.AURIC_TESLA_ROYAL_HELM.get()))
            .title(Component.translatable("creativetab.roacw"))
            .displayItems((itemDisplayParameters, output) -> {
                output.accept(ROACWItems.MARS_VISOR.get());
                output.accept(ROACWItems.MARS_ENGINE.get());
                output.accept(ROACWItems.MARS_LEG_GUARDS.get());
                output.accept(ROACWItems.MARS_BOOSTERS.get());
                output.accept(ROACWItems.AURIC_TESLA_ROYAL_HELM.get());
                output.accept(ROACWItems.AURIC_TESLA_CUIRASS.get());
                output.accept(ROACWItems.AURIC_TESLA_LEGGINGS.get());
                output.accept(ROACWItems.AURIC_TESLA_GREAVES.get());
                output.accept(ROACWBlocks.AURIC_ORE.get());
                output.accept(ROACWBlocks.DEEPSLATE_AURIC_ORE.get());
                output.accept(ROACWBlocks.RAW_AURIC_BLOCK.get());
                output.accept(ROACWBlocks.AURIC_BLOCK.get());
                output.accept(ROACWItems.RAW_AURIC.get());
                output.accept(ROACWItems.AURIC_INGOT.get());
                output.accept(ROACWItems.CHARGED_AURIC_INGOT.get());
                output.accept(ROACWItems.FRIED_CHICKEN.get());
            })
            .build());

    public static void register(IEventBus eventBus) {
        CREATIVE_MODE_TAB.register(eventBus);
    }
}
