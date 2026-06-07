package net.kamaarion.roacw.items.curios.elemental_gauntlet;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.HumanoidModel;
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
            float headPitch) {

        if (!slotContext.visible()) {
            return;
        }

        if (renderLayerParent.getModel() instanceof HumanoidModel<?>) {
            @SuppressWarnings("unchecked")
            HumanoidModel<LivingEntity> humanoidModel = (HumanoidModel<LivingEntity>) renderLayerParent.getModel();

            int slotIndex = slotContext.index();

            if (slotIndex == 0 && humanoidModel.rightArm.visible) {
                // --- RIGHT ARM RENDERING ---
                poseStack.pushPose();
                humanoidModel.rightArm.translateAndRotate(poseStack);
                poseStack.translate(0.0D, 0.5D, 0.0D);
                poseStack.scale(1.1f, 1.1f, 1.1f);
                poseStack.mulPose(Axis.YP.rotationDegrees(90.0F));
                itemRenderer.renderStatic(
                        slotContext.entity(),
                        stack,
                        ItemDisplayContext.FIXED,
                        false,
                        poseStack,
                        renderTypeBuffer,
                        slotContext.entity().level(),
                        light,
                        OverlayTexture.NO_OVERLAY,
                        0
                );
                poseStack.popPose();
            }
            else if (slotIndex == 1 && humanoidModel.leftArm.visible) {
                // --- LEFT ARM RENDERING ---
                poseStack.pushPose();
                humanoidModel.leftArm.translateAndRotate(poseStack);
                poseStack.translate(0.5D, 0.0D, 0.0D);
                poseStack.mulPoseMatrix(reflectionMatrix);
                poseStack.translate(0.5D, 0.0D, 0.0D);
                poseStack.translate(0.0D, 0.5D, 0.0D);
                poseStack.scale(1.1f, 1.1f, 1.1f);
                poseStack.mulPose(Axis.YP.rotationDegrees(90.0F));
                itemRenderer.renderStatic(
                        slotContext.entity(),
                        stack,
                        ItemDisplayContext.FIXED,
                        false,
                        poseStack,
                        renderTypeBuffer,
                        slotContext.entity().level(),
                        light,
                        OverlayTexture.NO_OVERLAY,
                        0
                );
                poseStack.popPose();
            }
        }
    }
}
