package net.kamaarion.roacw.items.armor.auricteslaarmorset;

import io.redspace.ironsspellbooks.entity.armor.GenericCustomArmorRenderer;
import io.redspace.ironsspellbooks.item.armor.ImbuableChestplateArmorItem;
import net.kamaarion.roacw.client.others.ModKeyMappings;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.kamaarion.roacw.items.armor.ROACWArmorMaterials;
import software.bernie.geckolib.renderer.GeoArmorRenderer;

import javax.annotation.Nullable;
import java.util.List;

public class AuricTeslaArmorItem extends ImbuableChestplateArmorItem {
    public AuricTeslaArmorItem(Type type, Properties settings) {
        super(ROACWArmorMaterials.AURIC_TESLA_ARMOR, type, settings, withManaAndSpellPowerAttribute(125, 0.05));
    }

    private static final net.minecraft.network.chat.Style AURIC_GOLD = net.minecraft.network.chat.Style.EMPTY
            .withColor(net.minecraft.network.chat.TextColor.parseColor("#FFDC16"));

    @Override
    @OnlyIn(Dist.CLIENT)
    public GeoArmorRenderer<?> supplyRenderer() {
        return new AuricTeslaArmorRenderer(new AuricTeslaArmorModel());
    }

    @Override
    public void appendHoverText(ItemStack pStack, @Nullable Level pLevel, List<Component> pTooltipComponents, TooltipFlag pIsAdvanced) {
        if (pLevel != null && pLevel.isClientSide()) {
            net.minecraft.client.gui.Font font = net.minecraft.client.Minecraft.getInstance().font;
            int maxTooltipWidth = 240;

            Component loreText = Component.translatable("item.roacw.auric_tesla_cuirass.desc").withStyle(AURIC_GOLD);
            font.getSplitter().splitLines(loreText, maxTooltipWidth, loreText.getStyle()).forEach(formattedText -> {
                pTooltipComponents.add(Component.literal(formattedText.getString()).withStyle(loreText.getStyle()));
            });

            pTooltipComponents.add(Component.empty());

            Component tooltip1Name = Component.translatable("item.roacw.auric_tesla_cuirass.tooltip1.name")
                    .withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD);
            pTooltipComponents.add(tooltip1Name);

            Component tooltipText1 = Component.translatable("item.roacw.auric_tesla_cuirass.tooltip1.desc").withStyle(ChatFormatting.GRAY);
            font.getSplitter().splitLines(tooltipText1, maxTooltipWidth, tooltipText1.getStyle()).forEach(formattedText -> {
                pTooltipComponents.add(Component.literal(formattedText.getString()).withStyle(tooltipText1.getStyle()));
            });

            pTooltipComponents.add(Component.empty());

            Component currentBoundKey = ModKeyMappings.AURIC_DASH_KEY.getTranslatedKeyMessage()
                    .copy().withStyle(ChatFormatting.AQUA, ChatFormatting.BOLD);

            Component tooltip2Name = Component.translatable("item.roacw.auric_tesla_cuirass.tooltip2.name")
                    .withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD);
            pTooltipComponents.add(tooltip2Name);

            Component tooltipText2 = Component.translatable("item.roacw.auric_tesla_cuirass.tooltip2.desc", currentBoundKey).withStyle(ChatFormatting.GRAY);
            font.getSplitter().splitLines(tooltipText2, maxTooltipWidth, tooltipText2.getStyle()).forEach(formattedText -> {
                pTooltipComponents.add(Component.literal(formattedText.getString()).withStyle(tooltipText2.getStyle()));
            });

        } else {
            super.appendHoverText(pStack, pLevel, pTooltipComponents, pIsAdvanced);
        }
    }
    @Override
    public boolean isDamageable(ItemStack stack) {
        return false;
    }
}
