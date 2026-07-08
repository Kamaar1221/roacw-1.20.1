package net.kamaarion.roacw.items.weapons.high_ruler_sword;

import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import net.kamaarion.roacw.items.weapons.ROACWVanillaWeaponTiers;
import net.kamaarion.roacw.registeries.ROACWItemRegistry;
import net.minecraft.ChatFormatting;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;
import software.bernie.geckolib.animatable.GeoItem;
import software.bernie.geckolib.core.animatable.GeoAnimatable;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.util.GeckoLibUtil;

import javax.annotation.Nullable;
import java.util.List;
import java.util.function.Consumer;

public class HighRulerSwordItem extends SwordItem implements GeoItem, GeoAnimatable {
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    public HighRulerSwordItem(Properties properties) {
        // Pointing to our new isolated vanilla weapon tier file
        super(ROACWVanillaWeaponTiers.HIGH_RULER_SWORD, 3, -2.4F, properties.stacksTo(1).rarity(ROACWItemRegistry.GOD_FORGED));
    }

    @Override
    public Multimap<Attribute, AttributeModifier> getDefaultAttributeModifiers(EquipmentSlot slot) {
        Multimap<Attribute, AttributeModifier> baseModifiers = super.getDefaultAttributeModifiers(slot);

        // This injects your Geomancy Spell Power onto the sword perfectly without Iron's Spells hooks!
        if (slot == EquipmentSlot.MAINHAND) {
            ImmutableMultimap.Builder<Attribute, AttributeModifier> builder = ImmutableMultimap.builder();
            builder.putAll(baseModifiers);

            var tier = ROACWVanillaWeaponTiers.HIGH_RULER_SWORD;
            builder.put(tier.getExtraAttribute().get(), new AttributeModifier(
                    tier.getAttributeUuid(),
                    "High Ruler Geomancy Modifier",
                    tier.getAttributeAmount(),
                    tier.getAttributeOperation()
            ));

            return builder.build();
        }
        return baseModifiers;
    }

    @Override
    public void initializeClient(Consumer<IClientItemExtensions> consumer) {
        consumer.accept(new IClientItemExtensions() {
            private HighRulerSwordRenderer renderer = null;

            @Override
            public BlockEntityWithoutLevelRenderer getCustomRenderer() {
                if (this.renderer == null) {
                    this.renderer = new HighRulerSwordRenderer();
                }
                return this.renderer;
            }
        });
    }

    @Override public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {}
    @Override public AnimatableInstanceCache getAnimatableInstanceCache() { return cache; }

    @Override
    public void appendHoverText(ItemStack pStack, @Nullable Level pLevel, List<Component> pTooltipComponents, TooltipFlag pIsAdvanced) {
        pTooltipComponents.add(Component.translatable("item.roacw.earth_splitter.desc").withStyle(ChatFormatting.GOLD));
    }
}
