package net.kamaarion.roacw.items.armor.shrimparmorset;

import io.redspace.ironsspellbooks.item.armor.ImbuableChestplateArmorItem;
import net.kamaarion.roacw.items.armor.ROACWArmorMaterials;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.Font;
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

public class ShrimpArmorItem extends ImbuableChestplateArmorItem {

    private static final Style SHRIMP_CORAL = Style.EMPTY
            .withColor(TextColor.parseColor("#FF8A7A"));

    private static final int MAX_TOOLTIP_WIDTH = 240;

    public ShrimpArmorItem(Type type, Properties settings) {
        super(
                ROACWArmorMaterials.SHRIMP_ARMOR,
                type,
                settings,
                withManaAndSpellPowerAttribute(200, 0.05)
        );
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public GeoArmorRenderer<?> supplyRenderer() {
        return new ShrimpArmorRenderer(new ShrimpArmorModel());
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
            Font font = net.minecraft.client.Minecraft.getInstance().font;

            Component loreText = Component
                    .translatable("item.roacw.shrimp_armor.desc")
                    .withStyle(SHRIMP_CORAL);

            font.getSplitter()
                    .splitLines(loreText, MAX_TOOLTIP_WIDTH, loreText.getStyle())
                    .forEach(formattedText ->
                            tooltip.add(
                                    Component.literal(formattedText.getString())
                                            .withStyle(loreText.getStyle())
                            )
                    );

            tooltip.add(Component.empty());

            addAbility(
                    tooltip,
                    font,
                    "item.roacw.shrimp_armor.ability.name",
                    "item.roacw.shrimp_armor.ability.desc"
            );

            addAbility(
                    tooltip,
                    font,
                    "item.roacw.shrimp_armor.active.name",
                    "item.roacw.shrimp_armor.active.desc"
            );
        } else {
            super.appendHoverText(stack, level, tooltip, tooltipFlag);
        }
    }

    private static void addAbility(
            List<Component> tooltip,
            Font font,
            String nameKey,
            String descKey
    ) {
        tooltip.add(Component
                .translatable(nameKey)
                .withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD));

        Component abilityText = Component
                .translatable(descKey)
                .withStyle(ChatFormatting.GRAY);

        font.getSplitter()
                .splitLines(abilityText, MAX_TOOLTIP_WIDTH, abilityText.getStyle())
                .forEach(formattedText ->
                        tooltip.add(
                                Component.literal(formattedText.getString())
                                        .withStyle(abilityText.getStyle())
                        )
                );

        tooltip.add(Component.empty());
    }

    @Override
    public boolean isDamageable(ItemStack stack) {
        return false;
    }
}