package net.kamaarion.roacw.items.curios.eldritch_soul_artifact;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.cache.texture.AutoGlowingTexture;
import software.bernie.geckolib.renderer.GeoRenderer;
import software.bernie.geckolib.renderer.layer.GeoRenderLayer;

public class EldritchSoulArtifactGlowLayer
        extends GeoRenderLayer<EldritchSoulArtifact> {

    public EldritchSoulArtifactGlowLayer(
            GeoRenderer<EldritchSoulArtifact> renderer) {

        super(renderer);
    }

    @Override
    public void render(
            PoseStack poseStack,
            EldritchSoulArtifact animatable,
            BakedGeoModel bakedModel,
            RenderType renderType,
            MultiBufferSource bufferSource,
            VertexConsumer buffer,
            float partialTick,
            int packedLight,
            int packedOverlay) {

        RenderType emissiveRenderType =
                AutoGlowingTexture.getRenderType(
                        getTextureResource(animatable)
                );

        getRenderer().reRender(
                bakedModel,
                poseStack,
                bufferSource,
                animatable,
                emissiveRenderType,
                bufferSource.getBuffer(emissiveRenderType),
                partialTick,
                LightTexture.FULL_BRIGHT,
                OverlayTexture.NO_OVERLAY,
                1.0F,
                1.0F,
                1.0F,
                1.0F
        );
    }
}