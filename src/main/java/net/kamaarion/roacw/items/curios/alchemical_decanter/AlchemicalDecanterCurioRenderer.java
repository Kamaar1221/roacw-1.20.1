package net.kamaarion.roacw.items.curios.alchemical_decanter;

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
public class AlchemicalDecanterCurioRenderer implements ICurioRenderer {
    ItemRenderer itemRenderer = Minecraft.getInstance().getItemRenderer();

    @Override
    public <T extends LivingEntity, M extends EntityModel<T>> void render(ItemStack stack, SlotContext slotContext, PoseStack poseStack, RenderLayerParent<T, M> renderLayerParent, MultiBufferSource renderTypeBuffer, int light, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch) {
        if (renderLayerParent.getModel() instanceof HumanoidModel<?>)
        {
            var humanoidModel = (HumanoidModel<LivingEntity>) renderLayerParent.getModel();

            poseStack.pushPose();

            humanoidModel.body.translateAndRotate(poseStack);

            // Left hip
            poseStack.translate(0.25D, 0.8D, -0.1D);

            poseStack.scale(0.75F, 0.75F, 0.75F);

            // Rotate so it hangs naturally at the hip
            poseStack.mulPose(Axis.ZP.rotationDegrees(180F));
            poseStack.mulPose(Axis.YP.rotationDegrees(210F));

            itemRenderer.renderStatic(
                    stack,
                    ItemDisplayContext.FIXED,
                    light,
                    OverlayTexture.NO_OVERLAY,
                    poseStack,
                    renderTypeBuffer,
                    null,
                    0
            );

            poseStack.popPose();
        }
    }
}