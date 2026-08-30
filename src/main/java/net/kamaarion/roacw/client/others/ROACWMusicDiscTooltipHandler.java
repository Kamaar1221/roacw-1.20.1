package net.kamaarion.roacw.client.others;

import net.kamaarion.roacw.ROACW;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.RecordItem;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.List;

@Mod.EventBusSubscriber(modid = ROACW.MODID)
public class ROACWMusicDiscTooltipHandler {

    @SubscribeEvent
    public static void onItemTooltip(ItemTooltipEvent event) {
        if (!(event.getItemStack().getItem() instanceof RecordItem recordItem)) {
            return;
        }

        String descKey = recordItem.getDescriptionId() + ".desc";
        String desc2Key = recordItem.getDescriptionId() + ".desc2";

        Component desc2Line = Component.translatable(desc2Key);
        if (desc2Line.getString().equals(desc2Key)) {
            return; // no .desc2 key defined for this disc, skip
        }

        List<Component> tooltip = event.getToolTip();
        String descText = Component.translatable(descKey).getString();

        int insertIndex = tooltip.size(); // fallback: end, in case desc line isn't found
        for (int i = 0; i < tooltip.size(); i++) {
            if (tooltip.get(i).getString().equals(descText)) {
                insertIndex = i + 1; // insert after the quote line
                break;
            }
        }

        tooltip.add(insertIndex, desc2Line.copy());
    }
}