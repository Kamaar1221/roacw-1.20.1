package net.kamaarion.roacw.entity.projectile.energy_cannon_beam;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.kamaarion.roacw.client.shader_and_glowmask.ROACWRenderTypes;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.cache.object.GeoBone;
import software.bernie.geckolib.cache.object.GeoCube;
import software.bernie.geckolib.renderer.GeoRenderer;
import software.bernie.geckolib.renderer.layer.GeoRenderLayer;
import software.bernie.geckolib.util.RenderUtils;

import java.util.List;

public class EnergyCannonBeamOutlineLayer
        extends GeoRenderLayer<EnergyCannonBeamEntity> {

    public EnergyCannonBeamOutlineLayer(
            GeoRenderer<EnergyCannonBeamEntity> renderer
    ) {
        super(renderer);
    }

    @Override
    public void render(
            PoseStack poseStack,
            EnergyCannonBeamEntity animatable,
            BakedGeoModel bakedModel,
            RenderType renderType,
            MultiBufferSource bufferSource,
            VertexConsumer buffer,
            float partialTick,
            int packedLight,
            int packedOverlay
    ) {
        GeoBone shellBone =
                bakedModel
                        .getBone(EnergyCannonBeamRenderer.SHELL_BONE)
                        .orElse(null);

        if (shellBone == null || shellBone.isHidden()) {
            return;
        }

        List<GeoCube> cubes = shellBone.getCubes();

        RenderType outlineType =
                ROACWRenderTypes.entityTranslucentEmissiveOutline(
                        this.getTextureResource(animatable)
                );

        VertexConsumer outlineBuffer =
                bufferSource.getBuffer(outlineType);

        poseStack.pushPose();

        poseStack.mulPose(
                Axis.YP.rotationDegrees(180.0F)
        );

        RenderUtils.prepMatrixForBone(
                poseStack,
                shellBone
        );

        for (int index : EnergyCannonBeamRenderer.SHELL_CUBE_INDICES) {

            if (index < 0 || index >= cubes.size()) {
                continue;
            }

            poseStack.pushPose();

            this.getRenderer().renderCube(
                    poseStack,
                    cubes.get(index),
                    outlineBuffer,
                    packedLight,
                    packedOverlay,
                    1.0F,
                    1.0F,
                    1.0F,
                    1.0F
            );

            poseStack.popPose();
        }

        poseStack.popPose();
    }
}