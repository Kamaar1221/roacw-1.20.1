package net.kamaarion.roacw.items.armor.plaguebringerarmorset;

import io.redspace.ironsspellbooks.item.armor.ImbuableChestplateArmorItem;
import net.kamaarion.roacw.client.others.ModKeyMappings;
import net.kamaarion.roacw.items.armor.ROACWArmorMaterials;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import software.bernie.geckolib.renderer.GeoArmorRenderer;

import javax.annotation.Nullable;
import java.util.List;

public class PlaguebringerArmorItem extends ImbuableChestplateArmorItem {

    public PlaguebringerArmorItem(Type type, Properties settings) {
        super(ROACWArmorMaterials.PLAGUEBRINGER_ARMOR, type, settings, withManaAndSpellPowerAttribute(125, 0.05));
    }

    private static final net.minecraft.network.chat.Style PLAGUE_GREEN = net.minecraft.network.chat.Style.EMPTY
            .withColor(net.minecraft.network.chat.TextColor.parseColor("#19FA19"));

    @Override
    @OnlyIn(Dist.CLIENT)
    public GeoArmorRenderer<?> supplyRenderer() {
        return new PlaguebringerArmorRenderer();
    }

    @Override
    public void appendHoverText(ItemStack pStack, @Nullable Level pLevel, List<Component> pTooltipComponents, TooltipFlag pIsAdvanced) {
        if (pLevel != null && pLevel.isClientSide()) {
            net.minecraft.client.gui.Font font = net.minecraft.client.Minecraft.getInstance().font;
            int maxTooltipWidth = 240;

            Component loreText = Component.translatable("item.roacw.plaguebringer_frame.desc").withStyle(PLAGUE_GREEN);
            font.getSplitter().splitLines(loreText, maxTooltipWidth, loreText.getStyle()).forEach(formattedText -> {
                pTooltipComponents.add(Component.literal(formattedText.getString()).withStyle(loreText.getStyle()));
            });

            pTooltipComponents.add(Component.empty());

            Component currentBoundKey = ModKeyMappings.PLAGUEBRINGER_JET_BOOST_KEY.getTranslatedKeyMessage()
                    .copy()
                    .withStyle(ChatFormatting.RED, ChatFormatting.BOLD);

            Component tooltipName = Component.translatable("item.roacw.plaguebringer_frame.tooltip.name")
                    .withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD);
            pTooltipComponents.add(tooltipName);

            Component tooltipText = Component.translatable("item.roacw.plaguebringer_frame.tooltip.desc", currentBoundKey).withStyle(ChatFormatting.GRAY);
            font.getSplitter().splitLines(tooltipText, maxTooltipWidth, tooltipText.getStyle()).forEach(formattedText -> {
                pTooltipComponents.add(Component.literal(formattedText.getString()).withStyle(tooltipText.getStyle()));
            });

        } else {
            super.appendHoverText(pStack, pLevel, pTooltipComponents, pIsAdvanced);
        }
    }
}