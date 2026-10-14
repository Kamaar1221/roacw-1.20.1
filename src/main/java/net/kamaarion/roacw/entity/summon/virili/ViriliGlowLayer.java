package net.kamaarion.roacw.entity.summon.virili;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.kamaarion.roacw.ROACW;
import net.kamaarion.roacw.client.shader_and_glowmask.ROACWRenderTypes;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.renderer.GeoRenderer;
import software.bernie.geckolib.renderer.layer.GeoRenderLayer;

public class ViriliGlowLayer extends GeoRenderLayer<ViriliEntity> {

    private static final ResourceLocation GLOWMASK =
            ResourceLocation.fromNamespaceAndPath(
                    ROACW.MODID,
                    "textures/entity/summon/virili_glowmask.png"
            );

    public ViriliGlowLayer(GeoRenderer<ViriliEntity> renderer) {
        super(renderer);
    }

    @Override
    public void render(
            PoseStack poseStack,
            ViriliEntity animatable,
            BakedGeoModel bakedModel,
            RenderType renderType,
            MultiBufferSource bufferSource,
            VertexConsumer bufferConsumer,
            float partialTick,
            int packedLight,
            int packedOverlay
    ) {
        RenderType glowRenderType =
                ROACWRenderTypes.entityTranslucentEmissiveDepthWrite(GLOWMASK);

        VertexConsumer glowBuffer = bufferSource.getBuffer(glowRenderType);

        getRenderer().actuallyRender(
                poseStack,
                animatable,
                bakedModel,
                glowRenderType,
                bufferSource,
                glowBuffer,
                false,
                partialTick,
                15728880,
                OverlayTexture.NO_OVERLAY,
                1.0F,
                1.0F,
                1.0F,
                1.0F
        );
    }
}