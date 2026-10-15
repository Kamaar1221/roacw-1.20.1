package net.kamaarion.roacw.items.curios.nightmare_tome;

import io.redspace.ironsspellbooks.api.registry.AttributeRegistry;
import io.redspace.ironsspellbooks.item.SpellBook;
import io.redspace.ironsspellbooks.item.weapons.AttributeContainer;
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

public class NightmareTome extends SpellBook {
  public NightmareTome(Properties properties) {
    super(12, properties.stacksTo(1));

    withSpellbookAttributes(
            new AttributeContainer(AttributeRegistry.ICE_SPELL_POWER, 0.2, AttributeModifier.Operation.MULTIPLY_BASE),
            new AttributeContainer(AttributeRegistry.FIRE_SPELL_POWER, 0.2, AttributeModifier.Operation.MULTIPLY_BASE),
            new AttributeContainer(AttributeRegistry.ELDRITCH_SPELL_POWER, 0.1, AttributeModifier.Operation.MULTIPLY_BASE),
            new AttributeContainer(AttributeRegistry.SUMMON_DAMAGE, 0.1, AttributeModifier.Operation.MULTIPLY_BASE),
            new AttributeContainer(AttributeRegistry.MAX_MANA, 300, AttributeModifier.Operation.ADDITION)
    );
  }

  @Override
  public Rarity getRarity(ItemStack stack) {
    return ROACWItemRegistry.OTHERWORLDLY;
  }

  @Override
  public void appendHoverText(ItemStack pStack, @Nullable Level pLevel, List<Component> pTooltipComponents, TooltipFlag pIsAdvanced) {
    super.appendHoverText(pStack, pLevel, pTooltipComponents, pIsAdvanced);

    if (pLevel != null && pLevel.isClientSide()) {
      Font font = Minecraft.getInstance().font;
      int maxTooltipWidth = 240;

      Component loreText = Component.translatable("item.roacw.nightmare_tome.desc")
              .withStyle(ChatFormatting.DARK_PURPLE);

      font.getSplitter().splitLines(loreText, maxTooltipWidth, loreText.getStyle()).forEach(formattedText ->
              pTooltipComponents.add(Component.literal(formattedText.getString()).withStyle(loreText.getStyle()))
      );

      pTooltipComponents.add(Component.empty());

      Component tooltipText = Component.translatable("item.roacw.nightmare_tome.tooltip.desc")
              .withStyle(ChatFormatting.GRAY);

      font.getSplitter().splitLines(tooltipText, maxTooltipWidth, tooltipText.getStyle()).forEach(formattedText ->
              pTooltipComponents.add(Component.literal(formattedText.getString()).withStyle(tooltipText.getStyle()))
      );
    } else {
      pTooltipComponents.add(
              Component.translatable("item.roacw.nightmare_tome.desc").withStyle(ChatFormatting.DARK_PURPLE)
      );
    }
  }
}