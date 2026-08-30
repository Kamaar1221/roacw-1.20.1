package net.kamaarion.roacw.items.curios.earthen_paladin_oath;

import com.gametechbc.gtbcs_geomancy_plus.api.init.GGAttributes;
import io.redspace.ironsspellbooks.api.item.curios.AffinityData;
import io.redspace.ironsspellbooks.api.registry.AttributeRegistry;
import io.redspace.ironsspellbooks.item.SpellBook;
import io.redspace.ironsspellbooks.item.weapons.AttributeContainer;
import io.redspace.ironsspellbooks.util.TooltipsUtils;
import net.kamaarion.roacw.registeries.ROACWSpellRegistry;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Map;

public class EarthenPaladinOath extends SpellBook {
  public EarthenPaladinOath(Properties properties) {
    super(10, properties);

    withSpellbookAttributes(
            new AttributeContainer(AttributeRegistry.HOLY_SPELL_POWER, 0.15, AttributeModifier.Operation.MULTIPLY_BASE),
            new AttributeContainer(GGAttributes.GEO_SPELL_POWER, 0.15, AttributeModifier.Operation.MULTIPLY_BASE),
            new AttributeContainer(AttributeRegistry.MAX_MANA, 200, AttributeModifier.Operation.ADDITION)
            // add more AttributeContainer entries here as needed
    );
  }

  @Override
  public void initializeSpellContainer(ItemStack itemStack) {
    if (itemStack == null) {
      return;
    }
    super.initializeSpellContainer(itemStack);
    AffinityData.set(itemStack, new AffinityData(Map.of(
            ROACWSpellRegistry.IMPALING_COLUMN.get().getSpellResource(), 2,
            ROACWSpellRegistry.EARTHLY_VIRTUE.get().getSpellResource(), 2
    )));
  }

  @Override
  public Rarity getRarity(ItemStack stack) {
    return Rarity.EPIC; // adjust to match its intended tier
  }

  @Override
  public void appendHoverText(ItemStack pStack, @Nullable Level pLevel, List<Component> pTooltipComponents, TooltipFlag pIsAdvanced) {
    super.appendHoverText(pStack, pLevel, pTooltipComponents, pIsAdvanced);

    var affinityData = AffinityData.getAffinityData(pStack);
    if (!affinityData.affinityData().isEmpty()) {
      int i = TooltipsUtils.indexOfComponent(pTooltipComponents, "tooltip.irons_spellbooks.spellbook_spell_count");
      pTooltipComponents.addAll(i < 0 ? pTooltipComponents.size() : i + 1, affinityData.getDescriptionComponent());
    }

    if (pLevel != null && pLevel.isClientSide()) {
      net.minecraft.client.gui.Font font = net.minecraft.client.Minecraft.getInstance().font;
      int maxTooltipWidth = 240;

      Component loreText = Component.translatable("item.roacw.earthen_paladin_oath.desc").withStyle(ChatFormatting.GOLD);
      font.getSplitter().splitLines(loreText, maxTooltipWidth, loreText.getStyle()).forEach(formattedText -> {
        pTooltipComponents.add(Component.literal(formattedText.getString()).withStyle(loreText.getStyle()));
      });

      pTooltipComponents.add(Component.empty());

      Component tooltipText = Component.translatable("item.roacw.earthen_paladin_oath.tooltip.desc").withStyle(ChatFormatting.GRAY);
      font.getSplitter().splitLines(tooltipText, maxTooltipWidth, tooltipText.getStyle()).forEach(formattedText -> {
        pTooltipComponents.add(Component.literal(formattedText.getString()).withStyle(tooltipText.getStyle()));
      });
    } else {
      pTooltipComponents.add(Component.translatable("item.roacw.earthen_paladin_oath.desc").withStyle(ChatFormatting.GOLD));
    }
  }
}