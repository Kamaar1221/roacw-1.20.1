package net.kamaarion.roacw.client.others;

import net.kamaarion.roacw.client.others.MarsRarityColorHelp;
import net.kamaarion.roacw.registeries.ROACWItemRegistry;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.List;

@Mod.EventBusSubscriber(
        modid = "roacw",
        value = Dist.CLIENT,
        bus = Mod.EventBusSubscriber.Bus.FORGE
)
public class MarsRarityTooltipHandler {

    @SubscribeEvent
    public static void onItemTooltip(ItemTooltipEvent event) {
        ItemStack stack = event.getItemStack();

        if (stack.isEmpty()) {
            return;
        }

        if (stack.getItem().getRarity(stack) != ROACWItemRegistry.EXO_ENGINEERED) {
            return;
        }

        List<Component> tooltip = event.getToolTip();

        if (tooltip.isEmpty()) {
            return;
        }

        Component originalName = tooltip.get(0);

        Component animatedName =
                MarsRarityColorHelp.createRainbowWave(originalName);

        tooltip.set(0, animatedName);
    }
}