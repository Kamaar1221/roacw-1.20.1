package net.kamaarion.roacw.entity.armorset_summon.railgun_cannon;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.kamaarion.roacw.ROACW;
import net.kamaarion.roacw.client.shader_and_glowmask.ROACWRenderTypes;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.cache.object.GeoBone;
import software.bernie.geckolib.cache.texture.AnimatableTexture;
import software.bernie.geckolib.renderer.GeoRenderer;
import software.bernie.geckolib.renderer.layer.GeoRenderLayer;

public class RailgunCannonOutlineLayer extends GeoRenderLayer<RailgunCannonEntity> {

    private static final ResourceLocation OUTLINE_TEXTURE =
            ResourceLocation.fromNamespaceAndPath(
                    ROACW.MODID,
                    "textures/entity/armorset_summon/mars_railgun_cannon_glowmask.png"
            );

    private static final int SHELL_CUBE_INDEX = 0;

    public RailgunCannonOutlineLayer(GeoRenderer<RailgunCannonEntity> renderer) {
        super(renderer);
    }

    public RenderType getOutlineRenderType() {
        return ROACWRenderTypes.entityTranslucentEmissiveOutline(
                OUTLINE_TEXTURE
        );
    }

    public void renderOutlineCubes(
            PoseStack poseStack,
            GeoBone bone,
            MultiBufferSource bufferSource,
            int packedLight,
            int packedOverlay
    ) {
        AnimatableTexture.setAndUpdate(OUTLINE_TEXTURE);

        RenderType outlineRenderType = getOutlineRenderType();

        VertexConsumer outlineBuffer = bufferSource.getBuffer(
                outlineRenderType
        );

        var cubes = bone.getCubes();

        for (int i = 0; i < cubes.size(); i++) {
            if (i != SHELL_CUBE_INDEX) {
                continue;
            }

            this.getRenderer().renderCube(
                    poseStack,
                    cubes.get(i),
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

    public void renderCoreCubes(
            PoseStack poseStack,
            GeoBone bone,
            VertexConsumer buffer
    ) {
        var cubes = bone.getCubes();

        for (int i = 0; i < cubes.size(); i++) {
            if (i == SHELL_CUBE_INDEX) {
                continue;
            }

            this.getRenderer().renderCube(
                    poseStack,
                    cubes.get(i),
                    buffer,
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
            RailgunCannonEntity animatable,
            BakedGeoModel bakedModel,
            RenderType renderType,
            MultiBufferSource bufferSource,
            VertexConsumer buffer,
            float partialTick,
            int packedLight,
            int packedOverlay
    ) {
    }
}