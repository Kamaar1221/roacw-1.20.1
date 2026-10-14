package net.kamaarion.roacw.entity.armorset_summon.railgun_cannon;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.kamaarion.roacw.ROACW;
import net.kamaarion.roacw.client.shader_and_glowmask.ROACWRenderTypes;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.cache.texture.AnimatableTexture;
import software.bernie.geckolib.renderer.GeoRenderer;
import software.bernie.geckolib.renderer.layer.GeoRenderLayer;

public class RailgunCannonGlowLayer
        extends GeoRenderLayer<RailgunCannonEntity> {

    public static final ResourceLocation GLOWMASK =
            ResourceLocation.fromNamespaceAndPath(
                    ROACW.MODID,
                    "textures/entity/armorset_summon/mars_railgun_cannon_glowmask.png"
            );

    public RailgunCannonGlowLayer(
            GeoRenderer<RailgunCannonEntity> renderer
    ) {
        super(renderer);
    }

    @Override
    public void render(
            PoseStack poseStack,
            RailgunCannonEntity animatable,
            BakedGeoModel bakedModel,
            RenderType renderType,
            MultiBufferSource bufferSource,
            VertexConsumer buffer,
            float partialTick,
            int packedLight,
            int packedOverlay
    ) {

        AnimatableTexture.setAndUpdate(GLOWMASK);

        RenderType glowRenderType =
                ROACWRenderTypes.entityTranslucentEmissiveColorOnly(
                        GLOWMASK
                );

        VertexConsumer glowBuffer =
                bufferSource.getBuffer(glowRenderType);

        this.getRenderer().reRender(
                bakedModel,
                poseStack,
                bufferSource,
                animatable,
                glowRenderType,
                glowBuffer,
                partialTick,
                0xF000F0,
                packedOverlay,
                1.0F,
                1.0F,
                1.0F,
                1.0F
        );

        if (this.getRenderer()
                instanceof RailgunCannonRenderer railgunRenderer) {

            railgunRenderer.renderCoreCubes(
                    poseStack,
                    animatable,
                    bakedModel,
                    bufferSource,
                    partialTick,
                    packedOverlay
            );
        }
    }
}