package net.kamaarion.roacw.items.armor.marsarmorset;

import io.redspace.ironsspellbooks.item.armor.ImbuableChestplateArmorItem;
import net.kamaarion.roacw.client.others.ModKeyMappings;
import net.minecraft.ChatFormatting;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;
import net.kamaarion.roacw.items.armor.ROACWArmorMaterials;
import net.kamaarion.roacw.client.others.MarsRarityColorHelp;
import org.jetbrains.annotations.NotNull;
import software.bernie.geckolib.renderer.GeoArmorRenderer;

import javax.annotation.Nullable;
import java.util.List;
import java.util.function.Consumer;

public class MarsArmorItem extends ImbuableChestplateArmorItem {

    public MarsArmorItem(Type type, Properties settings) {
        super(ROACWArmorMaterials.MARS_ARMOR, type, settings, withManaAndSpellPowerAttribute(125, 0.05));
    }

    @Override
    public void initializeClient(Consumer<IClientItemExtensions> consumer) {
        consumer.accept(new IClientItemExtensions() {
            private MarsArmorRenderer renderer;

            @Override
            public @NotNull HumanoidModel<?> getHumanoidArmorModel(
                    LivingEntity livingEntity, ItemStack itemStack, EquipmentSlot slot, HumanoidModel<?> original) {
                if (renderer == null) {
                    renderer = new MarsArmorRenderer(new MarsArmorModel());
                }
                renderer.prepForRender(livingEntity, itemStack, slot, original);
                return renderer;
            }
        });
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public GeoArmorRenderer<?> supplyRenderer() {
        return new MarsArmorRenderer(new MarsArmorModel());
    }

    @Override
    public int getEnchantmentValue() {
        return 15;
    }

    @Override
    public void appendHoverText(ItemStack pStack, @Nullable Level pLevel, List<Component> pTooltipComponents, TooltipFlag pIsAdvanced) {
        if (pLevel != null && pLevel.isClientSide()) {
            net.minecraft.client.gui.Font font = net.minecraft.client.Minecraft.getInstance().font;
            int maxTooltipWidth = 240;

            Component loreText = Component.translatable("item.roacw.mars_engine.desc").withStyle(ChatFormatting.DARK_RED);
            font.getSplitter().splitLines(loreText, maxTooltipWidth, loreText.getStyle()).forEach(formattedText -> {
                pTooltipComponents.add(Component.literal(formattedText.getString()).withStyle(loreText.getStyle()));
            });

            pTooltipComponents.add(Component.empty());

            Component tooltip1Name = Component.translatable("item.roacw.mars_engine.tooltip1.name")
                    .withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD);
            pTooltipComponents.add(tooltip1Name);

            Component tooltipText1 = Component.translatable("item.roacw.mars_engine.tooltip1.desc").withStyle(ChatFormatting.GRAY);
            font.getSplitter().splitLines(tooltipText1, maxTooltipWidth, tooltipText1.getStyle()).forEach(formattedText -> {
                pTooltipComponents.add(Component.literal(formattedText.getString()).withStyle(tooltipText1.getStyle()));
            });

            pTooltipComponents.add(Component.empty());

            Component currentBoundKey = ModKeyMappings.MARS_CANNONS_KEY.getTranslatedKeyMessage()
                    .copy().withStyle(ChatFormatting.AQUA, ChatFormatting.BOLD);

            Component tooltip2Name = Component.translatable("item.roacw.mars_engine.tooltip2.name")
                    .withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD);
            pTooltipComponents.add(tooltip2Name);

            Component tooltipText2 = Component.translatable("item.roacw.mars_engine.tooltip2.desc", currentBoundKey).withStyle(ChatFormatting.GRAY);
            font.getSplitter().splitLines(tooltipText2, maxTooltipWidth, tooltipText2.getStyle()).forEach(formattedText -> {
                pTooltipComponents.add(Component.literal(formattedText.getString()).withStyle(tooltipText2.getStyle()));
            });

            pTooltipComponents.add(Component.empty());

        } else {
            super.appendHoverText(pStack, pLevel, pTooltipComponents, pIsAdvanced);
        }
    }


    @Override
    public Component getName(ItemStack stack) {
        return MarsRarityColorHelp.createRainbowWave(
                super.getName(stack)
        );
    }

    @Override
    public boolean isDamageable(ItemStack stack) {
        return false;
    }
}