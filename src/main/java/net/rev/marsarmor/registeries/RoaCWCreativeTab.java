package net.rev.marsarmor.registeries;

import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;
import net.rev.marsarmor.MarsArmor;

public class RoaCWCreativeTab {
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TAB =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, MarsArmor.MODID);

    public static final RegistryObject<CreativeModeTab> ROACW_ITEMS = CREATIVE_MODE_TAB.register("mars_armor", () -> CreativeModeTab.builder().icon(()-> new ItemStack(MarsItems.AURIC_TESLA_ROYAL_HELM.get()))
            .title(Component.translatable("creativetab.mars_armor"))
            .displayItems((itemDisplayParameters, output) -> {
                output.accept(MarsItems.MARS_VISOR.get());
                output.accept(MarsItems.MARS_ENGINE.get());
                output.accept(MarsItems.MARS_LEG_GUARDS.get());
                output.accept(MarsItems.MARS_BOOSTERS.get());
                output.accept(MarsItems.AURIC_TESLA_ROYAL_HELM.get());
                output.accept(MarsItems.AURIC_TESLA_CUIRASS.get());
                output.accept(MarsItems.AURIC_TESLA_LEGGINGS.get());
                output.accept(MarsItems.AURIC_TESLA_GREAVES.get());
                output.accept(MarsArmorBlocks.AURIC_ORE.get());
                output.accept(MarsArmorBlocks.DEEPSLATE_AURIC_ORE.get());
                output.accept(MarsArmorBlocks.RAW_AURIC_BLOCK.get());
                output.accept(MarsArmorBlocks.AURIC_BLOCK.get());
                output.accept(MarsItems.RAW_AURIC_INGOT.get());
                output.accept(MarsItems.AURIC_INGOT.get());
                output.accept(MarsItems.CHARGED_AURIC_INGOT.get());
                output.accept(MarsItems.FRIED_CHICKEN.get());
            })
            .build());

    public static void register(IEventBus eventBus) {
        CREATIVE_MODE_TAB.register(eventBus);
    }
}
