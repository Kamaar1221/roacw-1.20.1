package net.kamaarion.roacw.entity.spells.burning_meteor;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.kamaarion.roacw.client.entity.projectile.BurningMeteorModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

import javax.annotation.Nullable;

public class BurningMeteorRenderer
        extends GeoEntityRenderer<BurningMeteorEntity> {

    /*
     * The geometry in burning_meteor.geo.json is offset from the
     * entity origin.
     *
     * Based on the cube bounds:
     *
     * X: -6.5 -> +6.5
     * Y: -2   -> +14
     * Z: -9   -> +11
     *
     * The approximate visual center is therefore:
     *
     * X = 0 pixels
     * Y = 6 pixels
     * Z = 1 pixel
     *
     * 16 model pixels = 1 Minecraft block.
     */
    private static final double MODEL_OFFSET_X = 0.0D;
    private static final double MODEL_OFFSET_Y = -6.0D / 16.0D;
    private static final double MODEL_OFFSET_Z = -1.0D / 16.0D;

    /*
     * Additional rotation correction for the model's own orientation.
     *
     * Leave at zero initially.
     */
    private static final float MODEL_CORRECTION_DEGREES = 0.0F;

    public BurningMeteorRenderer(
            EntityRendererProvider.Context context
    ) {
        super(
                context,
                new BurningMeteorModel()
        );
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

        Vec3 motion = animatable.getDeltaMovement();

        if (motion.lengthSqr() > 1.0E-6D) {

            Vec3 direction = motion.normalize();

            /*
             * Horizontal rotation.
             */
            float yaw = (float) (
                    Mth.atan2(
                            direction.x,
                            direction.z
                    ) * Mth.RAD_TO_DEG
            );

            /*
             * Vertical rotation.
             */
            float pitch = (float) (
                    -Mth.atan2(
                            direction.y,
                            direction.horizontalDistance()
                    ) * Mth.RAD_TO_DEG
            );

            poseStack.mulPose(
                    Axis.YP.rotationDegrees(yaw)
            );

            poseStack.mulPose(
                    Axis.XP.rotationDegrees(pitch)
            );
        }

        /*
         * Correct the model's own orientation, if necessary.
         */
        if (MODEL_CORRECTION_DEGREES != 0.0F) {
            poseStack.mulPose(
                    Axis.ZP.rotationDegrees(
                            MODEL_CORRECTION_DEGREES
                    )
            );
        }

        /*
         * Move the actual GeckoLib geometry so that its visual center
         * sits on the entity's origin.
         *
         * This happens AFTER the directional rotations, meaning the
         * offset follows the meteor as it travels.
         */
        poseStack.translate(
                MODEL_OFFSET_X,
                MODEL_OFFSET_Y,
                MODEL_OFFSET_Z
        );

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