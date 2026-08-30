package net.kamaarion.roacw.items.armor.arsenalt1armorset;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.kamaarion.roacw.ROACW;
import net.kamaarion.roacw.items.armor.fearmongerarmorset.FearmongerArmorItem;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.renderer.GeoRenderer;
import software.bernie.geckolib.renderer.layer.GeoRenderLayer;

public class ArsenalT1ArmorLayer extends GeoRenderLayer<ArsenalT1ArmorItem> {

    private static final ResourceLocation LAYER =
            ResourceLocation.fromNamespaceAndPath(
                    ROACW.MODID,
                    "textures/item/armor/arsenal_t1_armor_glowmask.png"
            );

    public ArsenalT1ArmorLayer(GeoRenderer<ArsenalT1ArmorItem> renderer) {
        super(renderer);
    }

    @Override
    public void render(
            PoseStack poseStack,
            ArsenalT1ArmorItem animatable,
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