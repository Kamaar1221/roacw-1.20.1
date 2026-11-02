package net.kamaarion.roacw.items.armor.distortedenvoyarmorset;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.kamaarion.roacw.ROACW;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.renderer.GeoRenderer;
import software.bernie.geckolib.renderer.layer.GeoRenderLayer;

public class DistortedEnvoyArmorLayer extends GeoRenderLayer<DistortedEnvoyArmorItem> {

    private static final ResourceLocation LAYER =
            ResourceLocation.fromNamespaceAndPath(
                    ROACW.MODID,
                    "textures/item/armor/distorted_envoy_armor_glowmask.png"
            );

    public DistortedEnvoyArmorLayer(GeoRenderer<DistortedEnvoyArmorItem> renderer) {
        super(renderer);
    }

    @Override
    public void render(
            PoseStack poseStack,
            DistortedEnvoyArmorItem animatable,
            BakedGeoModel bakedModel,
            RenderType renderType,
            MultiBufferSource bufferSource,
            VertexConsumer buffer,
            float partialTick,
            int packedLight,
            int packedOverlay
    ) {
        RenderType glow = RenderType.eyes(LAYER);

        int fullBright = 0xF000F0;

        this.getRenderer().reRender(
                this.getDefaultBakedModel(animatable),
                poseStack,
                bufferSource,
                animatable,
                glow,
                bufferSource.getBuffer(glow),
                partialTick,
                fullBright,
                OverlayTexture.NO_OVERLAY,
                1f, 1f, 1f, 1f
        );
    }
}