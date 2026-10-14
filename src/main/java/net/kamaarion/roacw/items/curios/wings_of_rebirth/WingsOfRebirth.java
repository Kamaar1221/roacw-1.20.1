package net.kamaarion.roacw.items.curios.wings_of_rebirth;

import io.redspace.ironsspellbooks.item.curios.CurioBaseItem;
import net.kamaarion.roacw.items.curios.wings.FlightWings;
import net.kamaarion.roacw.items.curios.wings.FlightWingsHandler;
import net.kamaarion.roacw.registeries.ROACWItemRegistry;
import net.minecraft.ChatFormatting;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;
import software.bernie.geckolib.animatable.GeoItem;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.RawAnimation;
import software.bernie.geckolib.util.GeckoLibUtil;
import top.theillusivec4.curios.api.SlotContext;
import top.theillusivec4.curios.api.type.capability.ICurioItem;

import javax.annotation.Nullable;
import java.util.List;
import java.util.function.Consumer;

public class WingsOfRebirth extends CurioBaseItem implements ICurioItem, GeoItem, FlightWings {
    public static final String REVIVE_TAG = "roacw_revive_charge";

    private static final RawAnimation IDLE = RawAnimation.begin().thenLoop("idle_1");

    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    public WingsOfRebirth(Properties properties) {
        super(properties
                .stacksTo(1)
                .fireResistant()
                .rarity(ROACWItemRegistry.GOD_FORGED)
        );
    }

    public static boolean isCharged(ItemStack stack) {
        return stack.getItem() instanceof WingsOfRebirth && stack.hasTag() && stack.getTag().getBoolean(REVIVE_TAG);
    }

    public static void setCharged(ItemStack stack, boolean charged) {
        if (charged) {
            stack.getOrCreateTag().putBoolean(REVIVE_TAG, true);
        } else if (stack.hasTag()) {
            stack.getTag().remove(REVIVE_TAG);
            if (stack.getTag().isEmpty()) stack.setTag(null);
        }
    }

    @Override
    public int getMaxWingTime() {
        return 200;
    }

    @Override
    public double getMaxRiseSpeed() {
        return 0.30D;
    }

    @Override
    public double getThrustAccel() {
        return 0.15D;
    }

    @Override
    public float getHorizontalBoost() {
        return 0.08F;
    }

    @Override
    public boolean isFoil(ItemStack stack) {
        return isCharged(stack) || super.isFoil(stack);
    }

    @Override
    public void curioTick(SlotContext slotContext, ItemStack stack) {
        if (slotContext.entity() instanceof ServerPlayer player) {
            FlightWingsHandler.tick(player, this);
        }
    }

    @Override
    public void onEquip(SlotContext slotContext, ItemStack prevStack, ItemStack stack) {
        super.onEquip(slotContext, prevStack, stack);
        if (slotContext.entity() instanceof ServerPlayer player) {
            FlightWingsHandler.sync(player, this);
        }
    }

    @Override
    public void onUnequip(SlotContext slotContext, ItemStack newStack, ItemStack stack) {
        super.onUnequip(slotContext, newStack, stack);
        if (slotContext.entity() instanceof ServerPlayer player) {
            FlightWingsHandler.clear(player);
        }
    }

    @Override
    public boolean canEquip(SlotContext slotContext, ItemStack stack) {
        return true;
    }

    @Override
    public boolean canUnequip(SlotContext slotContext, ItemStack stack) {
        return true;
    }

    @Override
    public void initializeClient(Consumer<IClientItemExtensions> consumer) {
        consumer.accept(new IClientItemExtensions() {
            private WingsOfRebirthRenderer renderer;

            @Override
            public BlockEntityWithoutLevelRenderer getCustomRenderer() {
                if (this.renderer == null) {
                    this.renderer = new WingsOfRebirthRenderer();
                }

                return this.renderer;
            }
        });
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "controller", 0, state -> state.setAndContinue(IDLE)));
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.cache;
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("item.roacw.wings_of_rebirth.desc").withStyle(ChatFormatting.DARK_RED));
        if (isCharged(stack)) {
            tooltip.add(Component.translatable("item.roacw.wings_of_rebirth.charged").withStyle(ChatFormatting.GOLD));
        }
    }
}