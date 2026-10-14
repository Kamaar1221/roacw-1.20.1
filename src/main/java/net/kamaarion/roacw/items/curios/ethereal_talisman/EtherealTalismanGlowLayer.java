package net.kamaarion.roacw.items.curios.ethereal_talisman;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.kamaarion.roacw.client.shader_and_glowmask.ROACWRenderTypes;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.renderer.GeoRenderer;
import software.bernie.geckolib.renderer.layer.GeoRenderLayer;

public class EtherealTalismanGlowLayer extends GeoRenderLayer<EtherealTalisman> {

    private static final ResourceLocation GLOW_TEXTURE =
            ResourceLocation.fromNamespaceAndPath(
                    "roacw",
                    "textures/item/curio/ethereal_talisman_glowmask.png"
            );

    public EtherealTalismanGlowLayer(GeoRenderer<EtherealTalisman> renderer) {
        super(renderer);
    }

    @Override
    public void render(
            PoseStack poseStack,
            EtherealTalisman animatable,
            BakedGeoModel bakedModel,
            RenderType renderType,
            MultiBufferSource bufferSource,
            VertexConsumer buffer,
            float partialTick,
            int packedLight,
            int packedOverlay
    ) {
        RenderType glowRenderType =
                ROACWRenderTypes.entityTranslucentEmissiveOutlineNoCull(
                        GLOW_TEXTURE
                );

        VertexConsumer glowBuffer =
                bufferSource.getBuffer(glowRenderType);

        poseStack.pushPose();

        getRenderer().reRender(
                bakedModel,
                poseStack,
                bufferSource,
                animatable,
                glowRenderType,
                glowBuffer,
                partialTick,
                15728880,
                OverlayTexture.NO_OVERLAY,
                1.0F,
                1.0F,
                1.0F,
                1.0F
        );

        poseStack.popPose();
    }
}