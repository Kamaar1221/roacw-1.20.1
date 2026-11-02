package net.kamaarion.roacw.items.armor.distortedenvoyarmorset;

import io.redspace.ironsspellbooks.item.armor.ImbuableChestplateArmorItem;
import net.kamaarion.roacw.items.armor.ROACWArmorMaterials;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import software.bernie.geckolib.constant.DataTickets;
import software.bernie.geckolib.core.animatable.GeoAnimatable;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.AnimationState;
import software.bernie.geckolib.core.animation.RawAnimation;
import software.bernie.geckolib.core.object.PlayState;
import software.bernie.geckolib.renderer.GeoArmorRenderer;

import javax.annotation.Nullable;
import java.util.List;

public class DistortedEnvoyArmorItem extends ImbuableChestplateArmorItem {

    public static final String VARIANT_TAG = "EnvoyVariant";
    public static final String VARIANT_TIME_TAG = "EnvoyVariantTime";
    public static final int EYES_TO_TEETH_TICKS = 15;
    public static final int MAW_TRANSITION_TICKS = 26;
    public static final int MAW_BACK_TICKS = 23;
    public static final int VARIANT_EYES = 0;
    public static final int VARIANT_TEETH = 1;
    public static final int VARIANT_MAW = 2;

    private static final RawAnimation EYES_VARIANT = RawAnimation.begin().thenLoop("EyesVariant");
    private static final RawAnimation TEETH_VARIANT = RawAnimation.begin().thenLoop("TeethVariant");
    private static final RawAnimation MAW_IDLE = RawAnimation.begin().thenLoop("MawVariantIdle");
    private static final RawAnimation EYES_TO_TEETH = RawAnimation.begin().thenPlayAndHold("EyesToTeethTransition");
    private static final RawAnimation MAW_TRANSITION = RawAnimation.begin().thenPlayAndHold("MawTransition");
    private static final RawAnimation MAW_BACK = RawAnimation.begin().thenPlayAndHold("MawBackTransition");

    private static final Style DISTORTED_PURPLE = Style.EMPTY
            .withColor(TextColor.parseColor("#CE84FF"));

    public DistortedEnvoyArmorItem(Type type, Properties settings) {
        super(
                ROACWArmorMaterials.DISTORTED_ENVOY_ARMOR,
                type,
                settings,
                withManaAndSpellPowerAttribute(200, 0.05)
        );
    }

    public static int getVariant(ItemStack stack) {
        if (stack.isEmpty() || !stack.hasTag()) {
            return VARIANT_EYES;
        }
        int variant = stack.getTag().getInt(VARIANT_TAG);
        return variant < VARIANT_EYES || variant > VARIANT_MAW ? VARIANT_EYES : variant;
    }

    public static void setVariant(ItemStack stack, int variant, long gameTime) {
        stack.getOrCreateTag().putInt(VARIANT_TAG, Math.max(VARIANT_EYES, Math.min(VARIANT_MAW, variant)));
        stack.getOrCreateTag().putLong(VARIANT_TIME_TAG, gameTime);
    }

    private static boolean isTransitioning(ItemStack stack, Entity entity, int durationTicks) {
        if (stack == null || entity == null || !stack.hasTag() || !stack.getTag().contains(VARIANT_TIME_TAG)) {
            return false;
        }

        long elapsed = entity.level().getGameTime() - stack.getTag().getLong(VARIANT_TIME_TAG);
        return elapsed >= 0 && elapsed < durationTicks;
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "variant_controller", 0, this::variantPredicate));
    }

    private PlayState variantPredicate(AnimationState<GeoAnimatable> state) {
        ItemStack stack = state.getData(DataTickets.ITEMSTACK);
        Entity entity = state.getData(DataTickets.ENTITY);
        int variant = stack == null ? VARIANT_EYES : getVariant(stack);

        switch (variant) {
            case VARIANT_TEETH:
                if (isTransitioning(stack, entity, EYES_TO_TEETH_TICKS)) {
                    return state.setAndContinue(EYES_TO_TEETH);
                }
                return state.setAndContinue(TEETH_VARIANT);
            case VARIANT_MAW:
                if (isTransitioning(stack, entity, MAW_TRANSITION_TICKS)) {
                    return state.setAndContinue(MAW_TRANSITION);
                }
                return state.setAndContinue(MAW_IDLE);
            default:
                if (isTransitioning(stack, entity, MAW_BACK_TICKS)) {
                    return state.setAndContinue(MAW_BACK);
                }
                return state.setAndContinue(EYES_VARIANT);
        }
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public GeoArmorRenderer<?> supplyRenderer() {
        return new DistortedEnvoyArmorRenderer(new DistortedEnvoyArmorModel());
    }

    @Override
    public int getEnchantmentValue() {
        return 15;
    }

    @Override
    public boolean isEnchantable(ItemStack stack) {
        return true;
    }

    @Override
    public void appendHoverText(
            ItemStack stack,
            @Nullable Level level,
            List<Component> tooltip,
            TooltipFlag tooltipFlag
    ) {
        if (level != null && level.isClientSide()) {
            net.minecraft.client.gui.Font font =
                    net.minecraft.client.Minecraft.getInstance().font;

            int maxTooltipWidth = 240;

            Component loreText = Component
                    .translatable("item.roacw.distorted_envoy_armor.desc")
                    .withStyle(DISTORTED_PURPLE);

            font.getSplitter()
                    .splitLines(loreText, maxTooltipWidth, loreText.getStyle())
                    .forEach(formattedText ->
                            tooltip.add(
                                    Component.literal(formattedText.getString())
                                            .withStyle(loreText.getStyle())
                            )
                    );

            tooltip.add(Component.empty());
        } else {
            super.appendHoverText(stack, level, tooltip, tooltipFlag);
        }
    }

    @Override
    public boolean isDamageable(ItemStack stack) {
        return false;
    }
}