package net.kamaarion.roacw.entity.spells.impaling_column;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.kamaarion.roacw.client.entity.projectile.ImpalingColumnShardsModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

public class ImpalingColumnShardsRenderer extends GeoEntityRenderer<ImpalingColumnShards> {

    private static final float RISE_DEPTH = 4.0F; // how far underground at offset -1

    public ImpalingColumnShardsRenderer(EntityRendererProvider.Context renderManager) {
        super(renderManager, new ImpalingColumnShardsModel());
    }

    @Override
    public void render(ImpalingColumnShards entity, float entityYaw, float partialTicks, PoseStack poseStack, MultiBufferSource buffer, int packedLight) {

        // getPositionOffset returns -1 (underground) to 0 (fully risen)
        // multiply by RISE_DEPTH to get the actual Y translation
        float positionOffset = entity.getPositionOffset(partialTicks);
        float heightOffset = positionOffset * RISE_DEPTH;

        poseStack.pushPose();

        poseStack.translate(0.0D, heightOffset - 0.5D, 0.0D);

        if (entity.isTilted()) {
            poseStack.mulPose(Axis.YP.rotationDegrees(-entity.getYRot()));
            poseStack.mulPose(Axis.XP.rotationDegrees(35.0F));
            poseStack.mulPose(Axis.YP.rotationDegrees(entity.getYRot()));
        }

        float scale = entity.getScale();
        poseStack.scale(scale, scale, scale);

        super.render(entity, entityYaw, partialTicks, poseStack, buffer, packedLight);

        poseStack.popPose();
    }
}