package net.kamaarion.roacw.items.armor.marsarmorset;

import io.redspace.ironsspellbooks.entity.armor.GenericCustomArmorRenderer;
import io.redspace.ironsspellbooks.item.armor.ImbuableChestplateArmorItem;
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
import net.kamaarion.roacw.registeries.MarsRarityColorHelp;
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
        return new GenericCustomArmorRenderer<>(new MarsArmorModel());
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
            Component tooltipText = Component.translatable("item.roacw.mars_engine.tooltip").withStyle(ChatFormatting.GRAY);
            font.getSplitter().splitLines(tooltipText, maxTooltipWidth, tooltipText.getStyle()).forEach(formattedText -> {
                pTooltipComponents.add(Component.literal(formattedText.getString()).withStyle(tooltipText.getStyle()));
            });
            /*pTooltipComponents.add(Component.empty());
            Component setBonusText = Component.literal("Full Set Bonus: Grants Creative Flight").withStyle(ChatFormatting.GOLD);
            font.getSplitter().splitLines(setBonusText, maxTooltipWidth, setBonusText.getStyle()).forEach(formattedText -> {
                pTooltipComponents.add(Component.literal(formattedText.getString()).withStyle(setBonusText.getStyle()));
            });*/

        } else {
            super.appendHoverText(pStack, pLevel, pTooltipComponents, pIsAdvanced);
        }
    }


    @Override
    public Component getName(ItemStack stack) {
        return MarsRarityColorHelp.createRainbowWave(
                super.getName(stack).getString()
        );
    }

    @Override
    public boolean isDamageable(ItemStack stack) {
        return false;
    }
}

