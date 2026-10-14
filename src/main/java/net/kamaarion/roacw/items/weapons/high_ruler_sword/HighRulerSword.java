package net.kamaarion.roacw.items.weapons.high_ruler_sword;

import net.kamaarion.roacw.items.weapons.ROACWVanillaWeaponTiers;
import net.minecraft.ChatFormatting;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;
import software.bernie.geckolib.animatable.GeoItem;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.util.GeckoLibUtil;

import javax.annotation.Nullable;
import java.util.List;
import java.util.function.Consumer;

public class HighRulerSword extends SwordItem implements GeoItem {
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    public HighRulerSword(Properties properties) {
        super(
                ROACWVanillaWeaponTiers.HIGH_RULER_SWORD,
                3,
                -2.4F,
                properties.stacksTo(1).rarity(Rarity.EPIC)
        );
    }

    @Override
    public void initializeClient(Consumer<IClientItemExtensions> consumer) {
        consumer.accept(new IClientItemExtensions() {
            private HighRulerSwordRenderer renderer;

            @Override
            public BlockEntityWithoutLevelRenderer getCustomRenderer() {
                if (renderer == null) {
                    renderer = new HighRulerSwordRenderer();
                }

                return renderer;
            }
        });
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return cache;
    }

    @Override
    public void appendHoverText(
            ItemStack stack,
            @Nullable Level level,
            List<Component> tooltipComponents,
            TooltipFlag tooltipFlag
    ) {
        if (level != null && level.isClientSide()) {
            net.minecraft.client.gui.Font font = net.minecraft.client.Minecraft.getInstance().font;
            int maxTooltipWidth = 240;

            Component loreText = Component.translatable("item.roacw.high_ruler_sword.desc")
                    .withStyle(ChatFormatting.GOLD);

            font.getSplitter()
                    .splitLines(loreText, maxTooltipWidth, loreText.getStyle())
                    .forEach(formattedText ->
                            tooltipComponents.add(
                                    Component.literal(formattedText.getString())
                                            .withStyle(loreText.getStyle())
                            )
                    );
        } else {
            super.appendHoverText(stack, level, tooltipComponents, tooltipFlag);
        }
    }
}