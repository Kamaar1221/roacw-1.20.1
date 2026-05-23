package net.kamaarion.roacw.items.armor.auricteslaarmorset;

import io.redspace.ironsspellbooks.entity.armor.GenericCustomArmorRenderer;
import io.redspace.ironsspellbooks.item.armor.ImbuableChestplateArmorItem;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.kamaarion.roacw.registeries.ROACWArmorMaterials;
import software.bernie.geckolib.renderer.GeoArmorRenderer;

import javax.annotation.Nullable;
import java.util.List;

public class AuricTeslaArmorItem extends ImbuableChestplateArmorItem {
    public AuricTeslaArmorItem(Type type, Properties settings) {
        super(ROACWArmorMaterials.AURIC_TESLA_ARMOR, type, settings, withManaAndSpellPowerAttribute(125, 0.05));
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public GeoArmorRenderer<?> supplyRenderer() {
        return new GenericCustomArmorRenderer<>(new AuricTeslaArmorModel());
    }

    @Override
    public void appendHoverText(ItemStack pStack, @Nullable Level pLevel, List<Component> pTooltipComponents, TooltipFlag pIsAdvanced) {
        pTooltipComponents.add(Component.translatable("item.roacw.auric_tesla_cuirass.desc").withStyle(ChatFormatting.GRAY));
    }
    @Override
    public boolean isDamageable(ItemStack stack) {
        return false;
    }
}
