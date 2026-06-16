package net.kamaarion.roacw.items.armor.fearmongerarmorset;

import io.redspace.ironsspellbooks.entity.armor.GenericCustomArmorRenderer;
import io.redspace.ironsspellbooks.item.armor.ImbuableChestplateArmorItem;
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

public class FearmongerArmorItem extends ImbuableChestplateArmorItem {
    public FearmongerArmorItem(Type type, Properties settings) {
        super(ROACWArmorMaterials.FEARMONGER_ARMOR, type, settings, withManaAndSpellPowerAttribute(125, 0.05));
    }

    private static final net.minecraft.network.chat.Style NEON_PURPLE = net.minecraft.network.chat.Style.EMPTY
            .withColor(net.minecraft.network.chat.TextColor.parseColor("#CE84FF"));



    @Override
    @OnlyIn(Dist.CLIENT)
    public GeoArmorRenderer<?> supplyRenderer() {
        return new GenericCustomArmorRenderer<>(new FearmongerArmorModel());
    }

    @Override
    public void appendHoverText(ItemStack pStack, @Nullable Level pLevel, List<Component> pTooltipComponents, TooltipFlag pIsAdvanced) {
        if (pLevel != null && pLevel.isClientSide()) {
            net.minecraft.client.gui.Font font = net.minecraft.client.Minecraft.getInstance().font;
            int maxTooltipWidth = 240;

            Component loreText = Component.translatable("item.roacw.fearmonger_platemail.desc").withStyle(NEON_PURPLE);
            font.getSplitter().splitLines(loreText, maxTooltipWidth, loreText.getStyle()).forEach(formattedText -> {
                pTooltipComponents.add(Component.literal(formattedText.getString()).withStyle(loreText.getStyle()));
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
