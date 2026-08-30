package net.kamaarion.roacw.items.armor.arsenalt1armorset;

import io.redspace.ironsspellbooks.item.armor.ImbuableChestplateArmorItem;
import net.kamaarion.roacw.items.armor.ROACWArmorMaterials;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import software.bernie.geckolib.renderer.GeoArmorRenderer;

import javax.annotation.Nullable;
import java.util.List;

public class ArsenalT1ArmorItem extends ImbuableChestplateArmorItem {
    public ArsenalT1ArmorItem(Type type, Properties settings) {
        super(ROACWArmorMaterials.ARSENAL_T1_ARMOR, type, settings, withManaAndSpellPowerAttribute(125, 0.05));
    }

    private static final net.minecraft.network.chat.Style DARK_ORANGE = net.minecraft.network.chat.Style.EMPTY
            .withColor(net.minecraft.network.chat.TextColor.parseColor("#CC4723"));



    @Override
    @OnlyIn(Dist.CLIENT)
    public GeoArmorRenderer<?> supplyRenderer() {
        return new ArsenalT1ArmorRenderer(new ArsenalT1ArmorModel());
    }

    @Override
    public void appendHoverText(ItemStack pStack, @Nullable Level pLevel, List<Component> pTooltipComponents, TooltipFlag pIsAdvanced) {
        if (pLevel != null && pLevel.isClientSide()) {
            net.minecraft.client.gui.Font font = net.minecraft.client.Minecraft.getInstance().font;
            int maxTooltipWidth = 240;

            Component loreText = Component.translatable("item.roacw.arsenal_cloak.desc").withStyle(DARK_ORANGE);
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
