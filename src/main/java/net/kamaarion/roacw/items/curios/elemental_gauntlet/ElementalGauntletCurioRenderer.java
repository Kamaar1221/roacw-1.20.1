package net.kamaarion.roacw.items.curios.elemental_gauntlet;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.joml.Matrix4f;
import top.theillusivec4.curios.api.SlotContext;
import top.theillusivec4.curios.api.client.ICurioRenderer;

@OnlyIn(Dist.CLIENT)
public class ElementalGauntletCurioRenderer implements ICurioRenderer {
    private final ItemRenderer itemRenderer = Minecraft.getInstance().getItemRenderer();

    private final Matrix4f reflectionMatrix = new Matrix4f(
            -1, 0, 0, 0,
            0, 1, 0, 0,
            0, 0, 1, 0,
            0, 0, 0, 1
    );

    @Override
    public <T extends LivingEntity, M extends EntityModel<T>> void render(
            ItemStack stack,
            SlotContext slotContext,
            PoseStack poseStack,
            RenderLayerParent<T, M> renderLayerParent,
            MultiBufferSource renderTypeBuffer,
            int light,
            float limbSwing,
            float limbSwingAmount,
            float partialTicks,
            float ageInTicks,
            float netHeadYaw,
            float headPitch
    ) {
        if (!slotContext.visible()) {
            return;
        }

        if (!(renderLayerParent.getModel() instanceof HumanoidModel<?>)) {
            return;
        }

        @SuppressWarnings("unchecked")
        HumanoidModel<LivingEntity> humanoidModel =
                (HumanoidModel<LivingEntity>) renderLayerParent.getModel();

        int slotIndex = slotContext.index();
        LivingEntity wearer = slotContext.entity();

        boolean isSlim = wearer instanceof AbstractClientPlayer clientPlayer
                && clientPlayer.getModelName().equals("slim");

        if (slotIndex == 0 && humanoidModel.rightArm.visible) {
            poseStack.pushPose();
            humanoidModel.rightArm.translateAndRotate(poseStack);

            double xOffset = isSlim ? -0.04875D : -0.08D;
            poseStack.translate(xOffset, 0.5D, 0.0D);

            float scaleX = isSlim ? 0.95F : 1.1F;
            poseStack.scale(scaleX, 1.1F, 1.1F);

            poseStack.mulPose(Axis.YP.rotationDegrees(90.0F));

            itemRenderer.renderStatic(
                    wearer,
                    stack,
                    ItemDisplayContext.FIXED,
                    false,
                    poseStack,
                    renderTypeBuffer,
                    wearer.level(),
                    light,
                    OverlayTexture.NO_OVERLAY,
                    0
            );

            poseStack.popPose();
        } else if (slotIndex == 1 && humanoidModel.leftArm.visible) {
            poseStack.pushPose();
            humanoidModel.leftArm.translateAndRotate(poseStack);

            poseStack.translate(0.5D, 0.0D, 0.0D);
            poseStack.mulPoseMatrix(reflectionMatrix);
            poseStack.translate(0.5D, 0.0D, 0.0D);

            double xOffset = isSlim ? -0.065D : -0.08D;
            poseStack.translate(xOffset, 0.5D, 0.0D);

            float scaleX = isSlim ? 0.95F : 1.1F;
            poseStack.scale(scaleX, 1.1F, 1.1F);

            poseStack.mulPose(Axis.YP.rotationDegrees(90.0F));

            itemRenderer.renderStatic(
                    wearer,
                    stack,
                    ItemDisplayContext.FIXED,
                    false,
                    poseStack,
                    renderTypeBuffer,
                    wearer.level(),
                    light,
                    OverlayTexture.NO_OVERLAY,
                    0
            );

            poseStack.popPose();
        }
    }
}