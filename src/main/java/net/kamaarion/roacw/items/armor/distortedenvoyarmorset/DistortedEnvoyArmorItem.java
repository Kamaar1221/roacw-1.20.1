package net.kamaarion.roacw.items.armor.distortedenvoyarmorset;

import io.redspace.ironsspellbooks.entity.armor.GenericCustomArmorRenderer;
import io.redspace.ironsspellbooks.item.armor.ImbuableChestplateArmorItem;
import net.kamaarion.roacw.items.armor.ROACWArmorMaterials;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import software.bernie.geckolib.renderer.GeoArmorRenderer;

import javax.annotation.Nullable;
import java.util.List;

public class DistortedEnvoyArmorItem extends ImbuableChestplateArmorItem {

    private static final Style DISTORTED_PURPLE = Style.EMPTY
            .withColor(TextColor.parseColor("#CE84FF"));

    public DistortedEnvoyArmorItem(Type type, Properties settings) {
        super(
                ROACWArmorMaterials.DISTORTED_ENVOY_ARMOR,
                type,
                settings,
                withManaAndSpellPowerAttribute(200, 0.05)
        );
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public GeoArmorRenderer<?> supplyRenderer() {
        return new DistortedEnvoyArmorRenderer(new DistortedEnvoyArmorModel());
    }

    @Override
    public int getEnchantmentValue() {
        return 15;
    }

    @Override
    public boolean isEnchantable(ItemStack stack) {
        return true;
    }

    @Override
    public void appendHoverText(
            ItemStack stack,
            @Nullable Level level,
            List<Component> tooltip,
            TooltipFlag tooltipFlag
    ) {
        if (level != null && level.isClientSide()) {
            net.minecraft.client.gui.Font font =
                    net.minecraft.client.Minecraft.getInstance().font;

            int maxTooltipWidth = 240;

            Component loreText = Component
                    .translatable("item.roacw.distorted_envoy_armor.desc")
                    .withStyle(DISTORTED_PURPLE);

            font.getSplitter()
                    .splitLines(loreText, maxTooltipWidth, loreText.getStyle())
                    .forEach(formattedText ->
                            tooltip.add(
                                    Component.literal(formattedText.getString())
                                            .withStyle(loreText.getStyle())
                            )
                    );

            tooltip.add(Component.empty());
        } else {
            super.appendHoverText(stack, level, tooltip, tooltipFlag);
        }
    }

    @Override
    public boolean isDamageable(ItemStack stack) {
        return false;
    }
}