package net.kamaarion.roacw.client.shader_and_glowmask;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.cache.texture.AnimatableTexture;
import software.bernie.geckolib.core.animatable.GeoAnimatable;
import software.bernie.geckolib.renderer.GeoRenderer;
import software.bernie.geckolib.renderer.layer.GeoRenderLayer;

public class ROACWGlowLayer<T extends GeoAnimatable> extends GeoRenderLayer<T> {

    public ROACWGlowLayer(GeoRenderer<T> renderer) {
        super(renderer);
    }

    private static ResourceLocation glowTexture(ResourceLocation baseTexture) {
        return ResourceLocation.fromNamespaceAndPath(
                baseTexture.getNamespace(),
                baseTexture.getPath().replace(".png", "_glowmask.png")
        );
    }

    @Override
    public void render(
            PoseStack poseStack,
            T animatable,
            BakedGeoModel bakedModel,
            RenderType renderType,
            MultiBufferSource bufferSource,
            VertexConsumer buffer,
            float partialTick,
            int packedLight,
            int packedOverlay
    ) {
        if (animatable instanceof Entity entity && entity.isInvisible()) {
            return;
        }

        ResourceLocation glow = glowTexture(getTextureResource(animatable));

        AnimatableTexture.setAndUpdate(glow);

        RenderType glowRenderType = ROACWRenderTypes.eyesNoCull(glow);

        this.getRenderer().reRender(
                this.getDefaultBakedModel(animatable),
                poseStack,
                bufferSource,
                animatable,
                glowRenderType,
                bufferSource.getBuffer(glowRenderType),
                partialTick,
                packedLight,
                OverlayTexture.NO_OVERLAY,
                1.0F,
                1.0F,
                1.0F,
                1.0F
        );
    }
}