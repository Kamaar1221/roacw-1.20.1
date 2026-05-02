package net.rev.marsarmor.marsarmorset;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.rev.marsarmor.MarsArmor;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.renderer.GeoRenderer;
import software.bernie.geckolib.renderer.layer.GeoRenderLayer;

public class MarsArmorLayer extends GeoRenderLayer<MarsArmorItem> {
    private static final ResourceLocation LAYER = ResourceLocation.fromNamespaceAndPath(MarsArmor.MODID, "textures/item/armor/mars_armor_glowmask.png");

    public MarsArmorLayer(GeoRenderer<MarsArmorItem> renderer) {
        super(renderer);
    }

    @Override
    public void render(PoseStack poseStack, MarsArmorItem animatable, BakedGeoModel bakedModel,
                       RenderType renderType, MultiBufferSource bufferSource, VertexConsumer buffer,
                       float partialTick, int packedLight, int packedOverlay) {

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
