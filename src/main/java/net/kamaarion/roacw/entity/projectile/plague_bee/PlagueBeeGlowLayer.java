package net.kamaarion.roacw.entity.projectile.plague_bee;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.kamaarion.roacw.ROACW;
import net.kamaarion.roacw.client.shader_and_glowmask.ROACWRenderTypes;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.renderer.GeoRenderer;
import software.bernie.geckolib.renderer.layer.GeoRenderLayer;

public class PlagueBeeGlowLayer
        extends GeoRenderLayer<PlagueBeeEntity> {

    private static final ResourceLocation GLOWMASK =
            ResourceLocation.fromNamespaceAndPath(
                    ROACW.MODID,
                    "textures/entity/projectile/plague_bee_glowmask.png"
            );

    public PlagueBeeGlowLayer(
            GeoRenderer<PlagueBeeEntity> renderer
    ) {
        super(renderer);
    }

    @Override
    public void render(
            PoseStack poseStack,
            PlagueBeeEntity animatable,
            BakedGeoModel bakedModel,
            RenderType renderType,
            MultiBufferSource bufferSource,
            VertexConsumer buffer,
            float partialTick,
            int packedLight,
            int packedOverlay
    ) {

        RenderType glowRenderType =
                ROACWRenderTypes.entityTranslucentEmissiveColorOnly(
                        GLOWMASK
                );

        VertexConsumer glowBuffer =
                bufferSource.getBuffer(glowRenderType);

        getRenderer().reRender(
                bakedModel,
                poseStack,
                bufferSource,
                animatable,
                glowRenderType,
                glowBuffer,
                partialTick,
                packedLight,
                packedOverlay,
                1.0F,
                1.0F,
                1.0F,
                1.0F
        );
    }
}