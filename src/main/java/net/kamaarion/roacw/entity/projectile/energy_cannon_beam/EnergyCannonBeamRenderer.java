package net.kamaarion.roacw.entity.projectile.energy_cannon_beam;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.world.phys.Vec3;

import java.util.List;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import software.bernie.geckolib.cache.object.GeoBone;
import software.bernie.geckolib.cache.object.GeoCube;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

public class EnergyCannonBeamRenderer
        extends GeoEntityRenderer<EnergyCannonBeamEntity> {

    private static final Vec3 MODEL_FORWARD =
            new Vec3(
                    0.0D,
                    0.0D,
                    1.0D
            );

    public static final String SHELL_BONE = "Laser";

    public static final int[] SHELL_CUBE_INDICES = {0, 3, 4};

    private static final Vec3 MODEL_LOCAL_OFFSET_PIXELS =
            new Vec3(
                    0.0D,
                    -7.0D,
                    0.0D
            );

    public EnergyCannonBeamRenderer(
            EntityRendererProvider.Context context
    ) {
        super(
                context,
                new EnergyCannonBeamModel()
        );

        this.shadowRadius = 0.0F;
        this.shadowStrength = 0.0F;

        this.addRenderLayer(
                new EnergyCannonBeamOutlineLayer(this)
        );
    }

    @Override
    public void renderCubesOfBone(
            PoseStack poseStack,
            GeoBone bone,
            VertexConsumer buffer,
            int packedLight,
            int packedOverlay,
            float red,
            float green,
            float blue,
            float alpha
    ) {
        if (!SHELL_BONE.equals(bone.getName())) {

            super.renderCubesOfBone(
                    poseStack,
                    bone,
                    buffer,
                    packedLight,
                    packedOverlay,
                    red,
                    green,
                    blue,
                    alpha
            );

            return;
        }

        if (bone.isHidden()) {
            return;
        }

        List<GeoCube> cubes = bone.getCubes();

        for (int i = 0; i < cubes.size(); i++) {

            if (isShellCube(i)) {
                continue;
            }

            poseStack.pushPose();

            this.renderCube(
                    poseStack,
                    cubes.get(i),
                    buffer,
                    packedLight,
                    packedOverlay,
                    red,
                    green,
                    blue,
                    alpha
            );

            poseStack.popPose();
        }
    }

    private static boolean isShellCube(int index) {

        for (int shellIndex : SHELL_CUBE_INDICES) {

            if (shellIndex == index) {
                return true;
            }
        }

        return false;
    }

    @Override
    public boolean shouldRender(
            EnergyCannonBeamEntity entity,
            Frustum frustum,
            double camX,
            double camY,
            double camZ
    ) {
        return true;
    }

    @Override
    public void render(
            EnergyCannonBeamEntity entity,
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

        if (entity.isBeamAnimationFinished()) {
            return;
        }

        Vec3 beamStart =
                entity.getBeamStart();

        Vec3 beamDirection =
                entity.getBeamDirection();

        if (beamStart == null
                || beamDirection == null
                || beamDirection.lengthSqr() <= 0.000001D) {
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

        poseStack.translate(
                MODEL_LOCAL_OFFSET_PIXELS.x / 16.0D,
                MODEL_LOCAL_OFFSET_PIXELS.y / 16.0D,
                MODEL_LOCAL_OFFSET_PIXELS.z / 16.0D
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
                Math.max(
                        -1.0D,
                        Math.min(
                                1.0D,
                                normalizedFrom.dot(
                                        normalizedTo
                                )
                        )
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