package net.kamaarion.roacw.entity.spells.burning_meteor;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.kamaarion.roacw.client.shader_and_glowmask.ROACWGlowLayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

import javax.annotation.Nullable;

public class BurningMeteorRenderer extends GeoEntityRenderer<BurningMeteorEntity> {
    private static final double MODEL_OFFSET_X = 0.0D;
    private static final double MODEL_OFFSET_Y = -6.0D / 16.0D;
    private static final double MODEL_OFFSET_Z = -1.0D / 16.0D;
    private static final float MODEL_CORRECTION_DEGREES = 0.0F;

    public BurningMeteorRenderer(EntityRendererProvider.Context context) {
        super(context, new BurningMeteorModel());
        addRenderLayer(new ROACWGlowLayer<>(this));
    }

    @Override
    public void preRender(
            PoseStack poseStack,
            BurningMeteorEntity animatable,
            BakedGeoModel model,
            @Nullable MultiBufferSource bufferSource,
            @Nullable VertexConsumer buffer,
            boolean isReRender,
            float partialTick,
            int packedLight,
            int packedOverlay,
            float red,
            float green,
            float blue,
            float alpha
    ) {
        if (!isReRender) {
            Vec3 motion = animatable.getDeltaMovement();

            if (motion.lengthSqr() > 1.0E-6D) {
                Vec3 direction = motion.normalize();

                float yaw = (float) (
                        Mth.atan2(direction.x, direction.z) * Mth.RAD_TO_DEG
                );

                float pitch = (float) (
                        -Mth.atan2(direction.y, direction.horizontalDistance()) * Mth.RAD_TO_DEG
                );

                poseStack.mulPose(Axis.YP.rotationDegrees(yaw));
                poseStack.mulPose(Axis.XP.rotationDegrees(pitch));
            }

            if (MODEL_CORRECTION_DEGREES != 0.0F) {
                poseStack.mulPose(
                        Axis.ZP.rotationDegrees(MODEL_CORRECTION_DEGREES)
                );
            }

            poseStack.translate(
                    MODEL_OFFSET_X,
                    MODEL_OFFSET_Y,
                    MODEL_OFFSET_Z
            );
        }

        super.preRender(
                poseStack,
                animatable,
                model,
                bufferSource,
                buffer,
                isReRender,
                partialTick,
                packedLight,
                packedOverlay,
                red,
                green,
                blue,
                alpha
        );
    }
}