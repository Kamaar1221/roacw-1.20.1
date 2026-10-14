package net.kamaarion.roacw.items.curios.auric_codex;

import io.redspace.ironsspellbooks.api.registry.AttributeRegistry;
import io.redspace.ironsspellbooks.item.SpellBook;
import io.redspace.ironsspellbooks.item.weapons.AttributeContainer;
import net.acetheeldritchking.cataclysm_spellbooks.registries.CSAttributeRegistry;
import net.kamaarion.roacw.registeries.ROACWItemRegistry;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;
import java.util.List;

public class AuricCodex extends SpellBook {
    public AuricCodex(Properties properties) {
        super(15, properties);

        withSpellbookAttributes(
                new AttributeContainer(AttributeRegistry.FIRE_SPELL_POWER, 0.15, AttributeModifier.Operation.MULTIPLY_BASE),
                new AttributeContainer(AttributeRegistry.LIGHTNING_SPELL_POWER, 0.15, AttributeModifier.Operation.MULTIPLY_BASE),
                new AttributeContainer(CSAttributeRegistry.TECHNOMANCY_MAGIC_POWER, 0.15, AttributeModifier.Operation.MULTIPLY_BASE),
                new AttributeContainer(AttributeRegistry.MAX_MANA, 500, AttributeModifier.Operation.ADDITION)
        );
    }

    @Override
    public Rarity getRarity(ItemStack stack) {
        return ROACWItemRegistry.GOD_FORGED;
    }

    @Override
    public void appendHoverText(ItemStack pStack, @Nullable Level pLevel, List<Component> pTooltipComponents, TooltipFlag pIsAdvanced) {
        super.appendHoverText(pStack, pLevel, pTooltipComponents, pIsAdvanced);

        if (pLevel != null && pLevel.isClientSide()) {
            Font font = Minecraft.getInstance().font;
            int maxTooltipWidth = 240;

            Component loreText = Component.translatable("item.roacw.auric_codex.desc")
                    .withStyle(ChatFormatting.GOLD);

            font.getSplitter().splitLines(loreText, maxTooltipWidth, loreText.getStyle()).forEach(formattedText ->
                    pTooltipComponents.add(Component.literal(formattedText.getString()).withStyle(loreText.getStyle()))
            );

            pTooltipComponents.add(Component.empty());

            Component tooltipText = Component.translatable("item.roacw.auric_codex.tooltip.desc")
                    .withStyle(ChatFormatting.GRAY);

            font.getSplitter().splitLines(tooltipText, maxTooltipWidth, tooltipText.getStyle()).forEach(formattedText ->
                    pTooltipComponents.add(Component.literal(formattedText.getString()).withStyle(tooltipText.getStyle()))
            );
        } else {
            pTooltipComponents.add(
                    Component.translatable("item.roacw.auric_codex.desc").withStyle(ChatFormatting.GOLD)
            );
        }
    }
}