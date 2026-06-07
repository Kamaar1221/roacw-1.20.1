package net.kamaarion.roacw.items.curios.high_ruler_shield;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import top.theillusivec4.curios.api.SlotContext;
import top.theillusivec4.curios.api.client.ICurioRenderer;

public class HighRulerShieldCurioRenderer implements ICurioRenderer {

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

        LivingEntity entity = slotContext.entity();

        poseStack.pushPose();

        ICurioRenderer.translateIfSneaking(poseStack, entity);
        ICurioRenderer.rotateIfSneaking(poseStack, entity);

        poseStack.translate(0.0D, 0.45D, 0.22D);

        poseStack.mulPose(Axis.XP.rotationDegrees(-90.0F));

        poseStack.mulPose(Axis.YP.rotationDegrees(-45.0F));

        poseStack.scale(0.55F, 0.55F, 0.55F);

        Minecraft.getInstance().getItemRenderer().renderStatic(
                entity,
                stack,
                ItemDisplayContext.NONE,
                false,
                poseStack,
                renderTypeBuffer,
                entity.level(),
                light,
                OverlayTexture.NO_OVERLAY,
                entity.getId() + slotContext.index()
        );

        poseStack.popPose();
    }
}
