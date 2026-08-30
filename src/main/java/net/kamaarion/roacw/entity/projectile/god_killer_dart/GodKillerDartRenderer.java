package net.kamaarion.roacw.entity.projectile.god_killer_dart;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.kamaarion.roacw.client.entity.projectile.GodKillerDartModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

import javax.annotation.Nullable;

public class GodKillerDartRenderer extends GeoEntityRenderer<GodKillerDartEntity> {

    public GodKillerDartRenderer(EntityRendererProvider.Context context) {
        super(context, new GodKillerDartModel());
        // Bump if the dart reads as too small/large at a distance — same
        // shape of tweak as the Earthen Paladin's 1.5x scale.
        this.shadowRadius = 0.25f;
    }

    /**
     * Derives yaw/pitch directly from the dart's real motion vector rather
     * than relying on GeoEntityRenderer's default rotation (which is tuned
     * for Mob body-rotation, not a bare homing projectile). Same approach
     * as PlagueRocketRenderer.
     */
    @Override
    public void preRender(PoseStack poseStack,
                          GodKillerDartEntity animatable,
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
                          float alpha) {

        Vec3 motion = animatable.getDeltaMovement();

        if (motion.lengthSqr() > 1.0E-6D) {

            float xRot = (float) (-(Mth.atan2(
                    motion.y,
                    motion.horizontalDistance()
            ) * Mth.RAD_TO_DEG));

            float yRot = -((float) (Mth.atan2(
                    motion.z,
                    motion.x
            ) * Mth.RAD_TO_DEG) + 90.0F) + 180.0F;

            poseStack.mulPose(Axis.YP.rotationDegrees(yRot));
            poseStack.mulPose(Axis.XP.rotationDegrees(xRot));
        }

        poseStack.mulPose(Axis.ZP.rotationDegrees(animatable.getBankAngle()));

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