package net.kamaarion.roacw.entity.armorset_summon.unstable_energy_cannon;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.kamaarion.roacw.ROACW;
import net.kamaarion.roacw.client.shader_and_glowmask.ROACWRenderTypes;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.cache.object.GeoBone;
import software.bernie.geckolib.renderer.GeoRenderer;
import software.bernie.geckolib.renderer.layer.GeoRenderLayer;

public class UnstableEnergyCannonOutlineLayer
        extends GeoRenderLayer<UnstableEnergyCannonEntity> {

    private static final ResourceLocation OUTLINE_TEXTURE =
            ResourceLocation.fromNamespaceAndPath(
                    ROACW.MODID,
                    "textures/entity/armorset_summon/mars_unstable_energy_cannon_glowmask.png"
            );

    public UnstableEnergyCannonOutlineLayer(
            GeoRenderer<UnstableEnergyCannonEntity> renderer
    ) {
        super(renderer);
    }

    public void renderOutlineCubes(
            PoseStack poseStack,
            GeoBone bone,
            MultiBufferSource bufferSource,
            int packedLight,
            int packedOverlay
    ) {
        RenderType outlineRenderType =
                ROACWRenderTypes.entityTranslucentEmissiveOutlineNoCull(
                        OUTLINE_TEXTURE
                );

        VertexConsumer outlineBuffer =
                bufferSource.getBuffer(
                        outlineRenderType
                );

        for (var cube : bone.getCubes()) {

            this.getRenderer().renderCube(
                    poseStack,
                    cube,
                    outlineBuffer,
                    0xF000F0,
                    OverlayTexture.NO_OVERLAY,
                    1.0F,
                    1.0F,
                    1.0F,
                    1.0F
            );
        }
    }

    @Override
    public void render(
            PoseStack poseStack,
            UnstableEnergyCannonEntity animatable,
            software.bernie.geckolib.cache.object.BakedGeoModel bakedModel,
            RenderType renderType,
            MultiBufferSource bufferSource,
            VertexConsumer buffer,
            float partialTick,
            int packedLight,
            int packedOverlay
    ) {

    }
}