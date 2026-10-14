package net.kamaarion.roacw.entity.projectile.endo_hydra_beam;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

public class EndoHydraBeamRenderer
        extends GeoEntityRenderer<EndoHydraBeamEntity> {

    private static final Vec3 MODEL_FORWARD =
            new Vec3(
                    0.0D,
                    0.0D,
                    1.0D
            );

    public EndoHydraBeamRenderer(
            EntityRendererProvider.Context context
    ) {
        super(
                context,
                new EndoHydraBeamModel()
        );

        this.shadowRadius = 0.0F;
        this.shadowStrength = 0.0F;

        this.addRenderLayer(
                new EndoHydraBeamGlowLayer(this)
        );
    }

    @Override
    public boolean shouldRender(
            EndoHydraBeamEntity entity,
            Frustum frustum,
            double camX,
            double camY,
            double camZ
    ) {
        return true;
    }

    @Override
    public void render(
            EndoHydraBeamEntity entity,
            float entityYaw,
            float partialTick,
            PoseStack poseStack,
            MultiBufferSource bufferSource,
            int packedLight
    ) {

        entity.updateClientVisualPosition(
                partialTick
        );

        if (!entity.isClientVisualReady()) {
            return;
        }

        Vec3 beamStart =
                entity.getBeamStart();

        Vec3 beamDirection =
                entity.getBeamDirection();

        double beamLength =
                entity.getBeamLength();

        if (beamStart == null
                || beamDirection == null
                || beamDirection.lengthSqr() <= 0.000001D
                || beamLength <= 0.0001D) {
            return;
        }

        beamDirection =
                beamDirection.normalize();

        Vec3 entityPosition =
                entity.position();

        Vec3 offset =
                beamStart.subtract(
                        entityPosition
                );

        poseStack.pushPose();

        poseStack.translate(
                offset.x,
                offset.y,
                offset.z
        );

        Quaternionf rotation =
                rotationFromTo(
                        MODEL_FORWARD,
                        beamDirection
                );

        poseStack.mulPose(
                rotation
        );

        double modelLength;

        if (beamLength <= 8.0D) {
            modelLength = 8.0D;
        } else if (beamLength <= 16.0D) {
            modelLength = 16.0D;
        } else {
            modelLength = 24.0D;
        }

        float lengthScale =
                (float) Mth.clamp(
                        beamLength / modelLength,
                        0.0D,
                        1.0D
                );

        poseStack.scale(
                1.0F,
                1.0F,
                lengthScale
        );

        super.render(
                entity,
                entityYaw,
                partialTick,
                poseStack,
                bufferSource,
                packedLight
        );

        poseStack.popPose();
    }

    private static Quaternionf rotationFromTo(
            Vec3 from,
            Vec3 to
    ) {

        Vec3 normalizedFrom =
                from.normalize();

        Vec3 normalizedTo =
                to.normalize();

        double dot =
                Mth.clamp(
                        normalizedFrom.dot(
                                normalizedTo
                        ),
                        -1.0D,
                        1.0D
                );

        if (dot > 0.999999D) {
            return new Quaternionf();
        }

        if (dot < -0.999999D) {

            Vec3 axis =
                    Math.abs(normalizedFrom.x) < 0.9D
                            ? new Vec3(
                            1.0D,
                            0.0D,
                            0.0D
                    )
                            : new Vec3(
                            0.0D,
                            1.0D,
                            0.0D
                    );

            axis =
                    normalizedFrom.cross(
                            axis
                    ).normalize();

            return new Quaternionf()
                    .rotationAxis(
                            (float) Math.PI,
                            (float) axis.x,
                            (float) axis.y,
                            (float) axis.z
                    );
        }

        Vec3 axis =
                normalizedFrom.cross(
                        normalizedTo
                ).normalize();

        double angle =
                Math.acos(dot);

        return new Quaternionf()
                .rotationAxis(
                        (float) angle,
                        new Vector3f(
                                (float) axis.x,
                                (float) axis.y,
                                (float) axis.z
                        )
                );
    }
}