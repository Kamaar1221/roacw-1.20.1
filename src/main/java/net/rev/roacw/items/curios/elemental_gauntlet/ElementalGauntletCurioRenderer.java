package net.rev.roacw.items.curios.elemental_gauntlet;

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
import top.theillusivec4.curios.api.SlotContext;
import top.theillusivec4.curios.api.client.ICurioRenderer;

@OnlyIn(Dist.CLIENT)
public class ElementalGauntletCurioRenderer implements ICurioRenderer {
    private final ItemRenderer itemRenderer = Minecraft.getInstance().getItemRenderer();

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

        if (renderLayerParent.getModel() instanceof HumanoidModel<?>) {
            @SuppressWarnings("unchecked")
            HumanoidModel<LivingEntity> humanoidModel = (HumanoidModel<LivingEntity>) renderLayerParent.getModel();

            // Check the slot index assigned by Curios (typically 0 for right hand, 1 for left hand)
            int slotIndex = slotContext.index();

            if (slotIndex == 0) {
                // --- RIGHT ARM RENDERING ---
                poseStack.pushPose();
                humanoidModel.rightArm.translateAndRotate(poseStack);
                poseStack.translate(0.0D, 0.5D, 0.0D);
                poseStack.scale(1.1f, 1.1f, 1.1f);
                poseStack.mulPose(Axis.YP.rotationDegrees(90.0F));

                itemRenderer.renderStatic(stack, ItemDisplayContext.FIXED, light, OverlayTexture.NO_OVERLAY, poseStack, renderTypeBuffer, null, 0);
                poseStack.popPose();

            } else if (slotIndex == 1) {
                // --- LEFT ARM RENDERING (ROTATED WORKAROUND) ---
                poseStack.pushPose();
                humanoidModel.leftArm.translateAndRotate(poseStack);

                // Keep the scale entirely positive so textures do not flip inside-out
                poseStack.translate(0.0D, 0.5D, 0.0D);
                poseStack.scale(1.1f, 1.1f, 1.1f);

                // Spin it to face outward on the left arm without turning polygons inside-out
                poseStack.mulPose(Axis.YP.rotationDegrees(-90.0F));
                poseStack.mulPose(Axis.ZP.rotationDegrees(180.0F));

                itemRenderer.renderStatic(stack, ItemDisplayContext.FIXED, light, OverlayTexture.NO_OVERLAY, poseStack, renderTypeBuffer, null, 0);
                poseStack.popPose();
            }
        }
    }
}
