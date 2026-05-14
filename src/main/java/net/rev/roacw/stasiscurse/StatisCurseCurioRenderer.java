package net.rev.roacw.stasiscurse;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import top.theillusivec4.curios.api.SlotContext;
import top.theillusivec4.curios.api.client.ICurioRenderer;


public class StatisCurseCurioRenderer implements ICurioRenderer {
    public StatisCurseCurioRenderer() {
        net.minecraft.client.renderer.entity.ItemRenderer itemRenderer = Minecraft.getInstance().getItemRenderer();
    }

    @Override
    public <T extends LivingEntity, M extends EntityModel<T>> void render(
            ItemStack stack,
            SlotContext slotContext,
            PoseStack poseStack,
            RenderLayerParent<T, M> renderLayerParent,
            MultiBufferSource bufferSource,
            int light,
            float limbSwing,
            float limbSwingAmount,
            float partialTicks,
            float ageInTicks,
            float netHeadYaw,
            float headPitch) {

        poseStack.pushPose();

        if (renderLayerParent.getModel() instanceof HumanoidModel<?> humanoidModel) {
            ICurioRenderer.followBodyRotations(
                    slotContext.entity(),
                    (HumanoidModel<LivingEntity>) humanoidModel
            );
        }

        // Move scarf position
        poseStack.translate(0.0D, 0.25D, 0.0D);

        // Scale
        poseStack.scale(1.0F, 1.0F, 1.0F);

        // YOUR GECKOLIB MODEL RENDERING GOES HERE

        poseStack.popPose();
    }
}
