package net.kamaarion.roacw.entity.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import net.kamaarion.roacw.client.entity.EarthlyVirtueShardsModel;
import net.kamaarion.roacw.entity.spells.earthly_virtue.EarthlyVirtueShards;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

public class EarthlyVirtueShardsRenderer extends GeoEntityRenderer<EarthlyVirtueShards> {

    private static final float RISE_DEPTH = 4.0F; // how far underground at offset -1

    public EarthlyVirtueShardsRenderer(EntityRendererProvider.Context context) {
        super(context, new EarthlyVirtueShardsModel());
    }

    @Override
    public void render(EarthlyVirtueShards entity,
                       float entityYaw,
                       float partialTicks,
                       PoseStack poseStack,
                       MultiBufferSource buffer,
                       int packedLight) {

        float positionOffset = entity.getPositionOffset(partialTicks);
        float heightOffset = positionOffset * RISE_DEPTH;

        poseStack.pushPose();

        poseStack.translate(0.0D, heightOffset, 0.0D);
        poseStack.scale(1.35F, 1.35F, 1.35F);

        super.render(entity, entityYaw, partialTicks, poseStack, buffer, packedLight);

        poseStack.popPose();
    }
}