package net.kamaarion.roacw.items.curios.high_ruler_shield;

import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.Multimap;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.ChatFormatting;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ShieldItem;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.Level;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.LazyOptional;
import software.bernie.geckolib.animatable.GeoItem;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.util.GeckoLibUtil;
import top.theillusivec4.curios.api.CuriosCapability;
import top.theillusivec4.curios.api.SlotContext;
import top.theillusivec4.curios.api.type.capability.ICurio;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;

public class HighRulerShield extends ShieldItem implements GeoItem {
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    public HighRulerShield(Properties properties) {
        super(properties);
    }

    @Override
    public UseAnim getUseAnimation(ItemStack stack) {
        return UseAnim.BLOCK;
    }

    @Override
    public int getUseDuration(ItemStack stack) {
        return 72000;
    }

    @Override
    public void initializeClient(Consumer<IClientItemExtensions> consumer) {
        consumer.accept(new IClientItemExtensions() {

            private HighRulerShieldRenderer renderer;

            @Override
            public BlockEntityWithoutLevelRenderer getCustomRenderer() {
                if (renderer == null) {
                    renderer = new HighRulerShieldRenderer();
                }

                return renderer;
            }

            @Override
            public boolean applyForgeHandTransform(
                    PoseStack poseStack,
                    LocalPlayer player,
                    HumanoidArm arm,
                    ItemStack stack,
                    float partialTick,
                    float equipProgress,
                    float swingProgress) {

                if (player.isUsingItem() && player.getUseItem() == stack) {
                    if (arm == HumanoidArm.LEFT) {
                        poseStack.translate(0.2f, -0.6f, 0.5f);
                        poseStack.translate(-0.2F, 0.3F, -1.0F);
                        poseStack.mulPose(Axis.XP.rotationDegrees(10F));
                        poseStack.mulPose(Axis.ZP.rotationDegrees(-20F));
                        poseStack.scale(0.6F, 0.6F, 0.6F);
                    } else {
                        poseStack.translate(-0.2f, -0.6f, 0.5f);
                        poseStack.translate(0.2F, 0.3F, -1.0F);
                        poseStack.mulPose(Axis.XP.rotationDegrees(10F));
                        poseStack.mulPose(Axis.ZP.rotationDegrees(20F));
                        poseStack.scale(0.6F, 0.6F, 0.6F);
                    }
                    return true;
                }

                return false;
            }
        });
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.cache;
    }

    @Override
    public ICapabilityProvider initCapabilities(ItemStack stack, @Nullable CompoundTag nbt) {
        return new CurioWrapper(stack);
    }

    private static class CurioWrapper implements ICapabilityProvider {
        private final ItemStack stack;
        private final LazyOptional<ICurio> curioOptional;

        public CurioWrapper(ItemStack stack) {
            this.stack = stack;
            this.curioOptional = LazyOptional.of(() -> new ICurio() {

                @Override
                public ItemStack getStack() {
                    return CurioWrapper.this.stack;
                }

                @Override
                public void curioTick(SlotContext slotContext) {
                }

                @Override
                public Multimap<Attribute, AttributeModifier> getAttributeModifiers(SlotContext slotContext, UUID uuid) {
                    Multimap<Attribute, AttributeModifier> modifiers = LinkedHashMultimap.create();
                    modifiers.put(
                            Attributes.ARMOR,
                            new AttributeModifier(
                                    uuid,
                                    "high_ruler_shield_armor",
                                    10.0,
                                    AttributeModifier.Operation.ADDITION
                            )
                    );
                    return modifiers;
                }

                @Override
                public SoundInfo getEquipSound(SlotContext slotContext) {
                    return new SoundInfo(SoundEvents.ARMOR_EQUIP_GENERIC, 1.0f, 1.0f);
                }
            });
        }

        @Nonnull
        @Override
        public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nullable Direction side) {
            if (cap == CuriosCapability.ITEM) {
                return curioOptional.cast();
            }
            return LazyOptional.empty();
        }
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        player.startUsingItem(hand);
        return InteractionResultHolder.consume(stack);
    }

    @Override
    public void appendHoverText(ItemStack pStack, @Nullable Level pLevel, List<Component> pTooltipComponents, TooltipFlag pIsAdvanced) {
        pTooltipComponents.add(Component.translatable("item.roacw.high_ruler_shield.desc").withStyle(ChatFormatting.AQUA));
    }
}