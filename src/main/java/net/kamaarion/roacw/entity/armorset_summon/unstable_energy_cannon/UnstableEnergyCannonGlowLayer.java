package net.kamaarion.roacw.entity.armorset_summon.unstable_energy_cannon;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.kamaarion.roacw.ROACW;
import net.kamaarion.roacw.client.shader_and_glowmask.ROACWRenderTypes;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.renderer.GeoRenderer;
import software.bernie.geckolib.renderer.layer.GeoRenderLayer;

public class UnstableEnergyCannonGlowLayer
        extends GeoRenderLayer<UnstableEnergyCannonEntity> {

    private static final ResourceLocation GLOWMASK =
            ResourceLocation.fromNamespaceAndPath(
                    ROACW.MODID,
                    "textures/entity/armorset_summon/mars_unstable_energy_cannon_glowmask.png"
            );

    public UnstableEnergyCannonGlowLayer(
            GeoRenderer<UnstableEnergyCannonEntity> renderer
    ) {
        super(renderer);
    }

    @Override
    public void render(
            PoseStack poseStack,
            UnstableEnergyCannonEntity animatable,
            BakedGeoModel bakedModel,
            RenderType renderType,
            MultiBufferSource bufferSource,
            VertexConsumer buffer,
            float partialTick,
            int packedLight,
            int packedOverlay
    ) {
        RenderType glow =
                ROACWRenderTypes.entityTranslucentEmissiveColorOnly(
                        GLOWMASK
                );

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
                1.0F,
                1.0F,
                1.0F,
                1.0F
        );
    }
}