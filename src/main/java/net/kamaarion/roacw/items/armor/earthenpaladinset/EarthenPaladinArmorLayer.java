package net.kamaarion.roacw.items.armor.earthenpaladinset;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.kamaarion.roacw.ROACW;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.renderer.GeoRenderer;
import software.bernie.geckolib.renderer.layer.GeoRenderLayer;

public class EarthenPaladinArmorLayer extends GeoRenderLayer<EarthenPaladinArmorItem> {

    private static final ResourceLocation LAYER =
            ResourceLocation.fromNamespaceAndPath(
                    ROACW.MODID,
                    "textures/item/armor/earthen_paladin_armor_glowmask.png"
            );

    public EarthenPaladinArmorLayer(GeoRenderer<EarthenPaladinArmorItem> renderer) {
        super(renderer);
    }

    @Override
    public void render(
            PoseStack poseStack,
            EarthenPaladinArmorItem animatable,
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