package net.kamaarion.roacw.items.weapons.phaseblade;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.renderer.GeoRenderer;
import software.bernie.geckolib.renderer.layer.GeoRenderLayer;

public class PhasebladeLayer extends GeoRenderLayer<Phaseblade> {

    private static final ResourceLocation GLOWMASK =
            ResourceLocation.fromNamespaceAndPath(
                    "roacw",
                    "textures/item/weapon/phaseblade_glowmask.png"
            );

    public PhasebladeLayer(GeoRenderer<Phaseblade> renderer) {
        super(renderer);
    }

    @Override
    public void render(
            PoseStack poseStack,
            Phaseblade animatable,
            BakedGeoModel bakedModel,
            RenderType renderType,
            MultiBufferSource bufferSource,
            VertexConsumer buffer,
            float partialTick,
            int packedLight,
            int packedOverlay
    ) {
        RenderType glowRenderType = RenderType.eyes(GLOWMASK);

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