package net.kamaarion.roacw.items.curios.evasion_scarf;

import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.Multimap;
import dev.shadowsoffire.attributeslib.api.ALObjects;
import io.redspace.ironsspellbooks.item.curios.CurioBaseItem;
import net.kamaarion.roacw.client.others.ModKeyMappings;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;
import software.bernie.geckolib.animatable.GeoItem;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.AnimationState;
import software.bernie.geckolib.core.animation.RawAnimation;
import software.bernie.geckolib.core.object.PlayState;
import software.bernie.geckolib.util.GeckoLibUtil;
import top.theillusivec4.curios.api.SlotContext;
import top.theillusivec4.curios.api.type.capability.ICurioItem;

import javax.annotation.Nullable;
import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;

public class EvasionScarf extends CurioBaseItem implements ICurioItem, GeoItem {
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    public EvasionScarf(Properties properties) {
        super(properties
                .stacksTo(1)
                .fireResistant()
                .rarity(Rarity.EPIC)
        );
    }

    @Override
    public void initializeClient(Consumer<IClientItemExtensions> consumer) {
        consumer.accept(new IClientItemExtensions() {
            private EvasionScarfRenderer renderer;

            @Override
            public BlockEntityWithoutLevelRenderer getCustomRenderer() {
                if (renderer == null) {
                    renderer = new EvasionScarfRenderer();
                }

                return renderer;
            }
        });
    }

    @Override
    public void curioTick(SlotContext slotContext, ItemStack stack) {
    }

    @Override
    public boolean canEquip(SlotContext slotContext, ItemStack stack) {
        return true;
    }

    @Override
    public boolean canUnequip(SlotContext slotContext, ItemStack stack) {
        return true;
    }

    private PlayState idlePredicate(AnimationState<EvasionScarf> event) {
        event.getController().setAnimation(RawAnimation.begin().thenLoop("idle"));
        return PlayState.CONTINUE;
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "controller", 20, this::idlePredicate));
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return cache;
    }

    @Override
    public double getTick(Object object) {
        return 0;
    }

    @Override
    public Multimap<Attribute, AttributeModifier> getAttributeModifiers(
            SlotContext slotContext,
            UUID uuid,
            ItemStack stack
    ) {
        Multimap<Attribute, AttributeModifier> attributes = LinkedHashMultimap.create();

        attributes.put(
                Attributes.MOVEMENT_SPEED,
                new AttributeModifier(
                        uuid,
                        "Movement Speed",
                        0.15D,
                        AttributeModifier.Operation.MULTIPLY_BASE
                )
        );

        attributes.put(
                ALObjects.Attributes.DODGE_CHANCE.get(),
                new AttributeModifier(
                        uuid,
                        "Dodge Chance",
                        0.075D,
                        AttributeModifier.Operation.MULTIPLY_BASE
                )
        );

        return attributes;
    }

    @Override
    public void appendHoverText(
            ItemStack stack,
            @Nullable Level level,
            List<Component> tooltipComponents,
            TooltipFlag tooltipFlag
    ) {
        if (level != null && level.isClientSide()) {
            Font font = Minecraft.getInstance().font;
            int maxTooltipWidth = 240;

            Component currentBoundKey = ModKeyMappings.DASH_KEY
                    .getTranslatedKeyMessage()
                    .copy()
                    .withStyle(ChatFormatting.AQUA, ChatFormatting.BOLD);

            Component scarfTooltip = Component.translatable(
                    "item.roacw.evasion_scarf.desc",
                    currentBoundKey
            ).withStyle(ChatFormatting.GRAY);

            font.getSplitter()
                    .splitLines(scarfTooltip, maxTooltipWidth, scarfTooltip.getStyle())
                    .forEach(formattedText ->
                            tooltipComponents.add(
                                    Component.literal(formattedText.getString())
                                            .withStyle(scarfTooltip.getStyle())
                            )
                    );
        } else {
            super.appendHoverText(stack, level, tooltipComponents, tooltipFlag);
        }
    }
}