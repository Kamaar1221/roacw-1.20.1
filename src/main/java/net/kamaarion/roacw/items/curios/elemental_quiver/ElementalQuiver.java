package net.kamaarion.roacw.items.curios.elemental_quiver;

import com.gametechbc.spelllib.init.GSLAttributeRegistry;
import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.Multimap;
import dev.shadowsoffire.attributeslib.api.ALObjects;
import io.redspace.ironsspellbooks.api.registry.AttributeRegistry;
import io.redspace.ironsspellbooks.item.curios.CurioBaseItem;
import net.kamaarion.roacw.registeries.ROACWItemRegistry;
import net.minecraft.ChatFormatting;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;
import software.bernie.geckolib.animatable.GeoItem;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.AnimationState;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.core.animation.RawAnimation;
import software.bernie.geckolib.core.object.PlayState;
import software.bernie.geckolib.util.GeckoLibUtil;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.SlotContext;
import top.theillusivec4.curios.api.type.capability.ICurioItem;

import javax.annotation.Nullable;
import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;

public class ElementalQuiver extends CurioBaseItem implements ICurioItem, GeoItem {

    private final AnimatableInstanceCache cache =
            GeckoLibUtil.createInstanceCache(this);

    public ElementalQuiver(Properties properties) {
        super(properties
                .stacksTo(1)
                .fireResistant()
                .rarity(ROACWItemRegistry.OTHERWORLDLY)
        );
    }

    @Override
    public void initializeClient(Consumer<IClientItemExtensions> consumer) {
        consumer.accept(new IClientItemExtensions() {
            private ElementalQuiverRenderer renderer;

            @Override
            public BlockEntityWithoutLevelRenderer getCustomRenderer() {
                if (this.renderer == null) {
                    this.renderer = new ElementalQuiverRenderer();
                }

                return this.renderer;
            }
        });
    }

    @Override
    public void curioTick(SlotContext slotContext, ItemStack stack) {
        // Elemental Quiver tick behavior will go here later.
    }

    @Override
    public boolean canEquip(SlotContext slotContext, ItemStack stack) {
        return true;
    }

    @Override
    public boolean canUnequip(SlotContext slotContext, ItemStack stack) {
        return true;
    }

    public static boolean isEquipped(Player player) {
        return CuriosApi.getCuriosInventory(player)
                .map(handler -> handler.findFirstCurio(
                        stack -> stack.getItem() instanceof ElementalQuiver
                ).isPresent())
                .orElse(false);
    }

    private PlayState idlePredicate(AnimationState<ElementalQuiver> event) {
        event.getController().setAnimation(
                RawAnimation.begin().thenLoop("idle")
        );

        return PlayState.CONTINUE;
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar data) {
        data.add(new AnimationController<>(
                this,
                "controller",
                20,
                this::idlePredicate
        ));
    }

    @Override
    public Multimap<Attribute, AttributeModifier> getAttributeModifiers(SlotContext slotContext, UUID uuid, ItemStack stack) {
        Multimap<Attribute, AttributeModifier> attr = LinkedHashMultimap.create();
        attr.put(ALObjects.Attributes.ARROW_DAMAGE.get(),
                new AttributeModifier(uuid, "Arrow Damage", 0.30, AttributeModifier.Operation.MULTIPLY_BASE));
        attr.put(ALObjects.Attributes.ARROW_VELOCITY.get(),
                new AttributeModifier(uuid, "Spell Power", 1.0, AttributeModifier.Operation.MULTIPLY_BASE));
        attr.put(ALObjects.Attributes.DRAW_SPEED.get(),
                new AttributeModifier(uuid, "Draw Speed", 0.50, AttributeModifier.Operation.MULTIPLY_BASE));
        attr.put(ALObjects.Attributes.CRIT_CHANCE.get(),
                new AttributeModifier(uuid, "Draw Speed", 0.25, AttributeModifier.Operation.MULTIPLY_BASE));

        return attr;
    }

    @Override
    public void appendHoverText(ItemStack pStack, @Nullable Level pLevel, List<Component> pTooltipComponents, TooltipFlag pIsAdvanced) {
        if (pLevel != null && pLevel.isClientSide()) {
            net.minecraft.client.gui.Font font = net.minecraft.client.Minecraft.getInstance().font;
            int maxTooltipWidth = 240;

            Component loreText = Component.translatable("item.roacw.elemental_quiver.desc").withStyle(ChatFormatting.AQUA);
            font.getSplitter().splitLines(loreText, maxTooltipWidth, loreText.getStyle()).forEach(formattedText -> {
                pTooltipComponents.add(Component.literal(formattedText.getString()).withStyle(loreText.getStyle()));
            });

            pTooltipComponents.add(Component.empty());


        } else {
            super.appendHoverText(pStack, pLevel, pTooltipComponents, pIsAdvanced);
        }
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.cache;
    }

    @Override
    public double getTick(Object object) {
        return 0;
    }
}