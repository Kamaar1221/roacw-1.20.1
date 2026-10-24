package net.kamaarion.roacw.items.weapons.singularity;

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

public class SingularityGlowLayer extends GeoRenderLayer<Singularity> {

    public SingularityGlowLayer(GeoRenderer<Singularity> renderer) {
        super(renderer);
    }

    @Override
    public void render(
            PoseStack poseStack,
            Singularity animatable,
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