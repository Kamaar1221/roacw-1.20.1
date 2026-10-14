package net.kamaarion.roacw.items.curios.wings_of_rebirth;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import software.bernie.geckolib.core.animatable.GeoAnimatable;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.AnimationState;
import software.bernie.geckolib.core.animation.RawAnimation;
import software.bernie.geckolib.core.object.PlayState;
import software.bernie.geckolib.model.GeoModel;
import software.bernie.geckolib.renderer.GeoObjectRenderer;
import software.bernie.geckolib.util.GeckoLibUtil;
import software.bernie.geckolib.util.RenderUtils;
import top.theillusivec4.curios.api.SlotContext;
import top.theillusivec4.curios.api.client.ICurioRenderer;

import java.util.Map;
import java.util.WeakHashMap;

public class WingsOfRebirthCurioRenderer implements ICurioRenderer {
    private static final float BACK_OFFSET = -0.40F;
    private static final float SIDE_OFFSET = 0.50F;
    private static final float DROP_OFFSET = 0.6F;

    private final WornModel model = new WornModel();
    private final GeoObjectRenderer<Animatable> geoRenderer = new GeoObjectRenderer<>(model);
    private final Map<LivingEntity, Animatable> animatables = new WeakHashMap<>();

    @Override
    public <T extends LivingEntity, M extends EntityModel<T>> void render(
            ItemStack stack, SlotContext slotContext, PoseStack poseStack,
            RenderLayerParent<T, M> renderLayerParent, MultiBufferSource buffer, int light,
            float limbSwing, float limbSwingAmount, float partialTicks,
            float ageInTicks, float netHeadYaw, float headPitch) {

        LivingEntity wearer = slotContext.entity();
        Animatable animatable = animatables.computeIfAbsent(wearer, k -> new Animatable());
        animatable.wearer = wearer;

        poseStack.pushPose();
        try {
            if (renderLayerParent.getModel() instanceof HumanoidModel<?> humanoid) {
                humanoid.body.translateAndRotate(poseStack);
            } else {
                ICurioRenderer.translateIfSneaking(poseStack, wearer);
                ICurioRenderer.rotateIfSneaking(poseStack, wearer);
            }

            poseStack.translate(SIDE_OFFSET, 24.0D / 16.0D + DROP_OFFSET, BACK_OFFSET);
            poseStack.scale(-1.0F, -1.0F, 1.0F);

            RenderType renderType = RenderType.entityCutoutNoCull(model.getTextureResource(animatable));
            VertexConsumer vertexConsumer = buffer.getBuffer(renderType);
            geoRenderer.render(poseStack, animatable, buffer, renderType, vertexConsumer, light);
        } finally {
            poseStack.popPose();
            animatable.wearer = null;
        }
    }

    public static class WornModel extends GeoModel<Animatable> {
        @Override
        public ResourceLocation getModelResource(Animatable animatable) {
            return WingsOfRebirthModel.MODEL;
        }

        @Override
        public ResourceLocation getTextureResource(Animatable animatable) {
            return WingsOfRebirthModel.TEXTURE;
        }

        @Override
        public ResourceLocation getAnimationResource(Animatable animatable) {
            return WingsOfRebirthModel.ANIMATION;
        }
    }

    public static class Animatable implements GeoAnimatable {
        private static final RawAnimation IDLE = RawAnimation.begin().thenLoop("idle_1");
        private static final RawAnimation FLIGHT = RawAnimation.begin().thenLoop("flight");
        private static final RawAnimation DIVE = RawAnimation.begin().thenLoop("dive");

        private static final double DIVE_FALL_SPEED = 0.8D;
        private static final double DIVE_GLIDE_SPEED = 0.65D;
        private static final float DIVE_GLIDE_PITCH = 35.0F;
        private static final int TRANSITION_TICKS = 6;

        private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

        LivingEntity wearer;

        @Override
        public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
            controllers.add(new AnimationController<>(this, "main", TRANSITION_TICKS, this::predicate));
        }

        private PlayState predicate(AnimationState<Animatable> state) {
            LivingEntity e = this.wearer;
            if (e == null) return state.setAndContinue(IDLE);

            if (isDiving(e)) return state.setAndContinue(DIVE);
            if (isFlying(e)) return state.setAndContinue(FLIGHT);
            return state.setAndContinue(IDLE);
        }

        private static boolean isDiving(LivingEntity e) {
            if (e.isFallFlying()) return e.getDeltaMovement().length() > DIVE_GLIDE_SPEED || e.getXRot() > DIVE_GLIDE_PITCH;
            return isAirborne(e) && e.getDeltaMovement().y < -DIVE_FALL_SPEED;
        }

        private static boolean isFlying(LivingEntity e) {
            if (e.isFallFlying()) return true;
            if (e instanceof Player p && p.getAbilities().flying) return true;
            return isAirborne(e);
        }

        private static boolean isAirborne(LivingEntity e) {
            return !e.onGround() && !e.isInWater() && !e.isInLava() && !e.isPassenger() && !e.onClimbable();
        }

        @Override
        public AnimatableInstanceCache getAnimatableInstanceCache() {
            return cache;
        }

        @Override
        public double getTick(Object object) {
            return RenderUtils.getCurrentTick();
        }
    }
}