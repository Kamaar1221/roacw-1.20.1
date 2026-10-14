package net.kamaarion.roacw.entity.armorset_summon.unstable_energy_cannon;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
import org.joml.Vector4f;
import software.bernie.geckolib.cache.object.GeoBone;
import software.bernie.geckolib.renderer.GeoEntityRenderer;
import software.bernie.geckolib.util.RenderUtils;

import java.util.HashMap;
import java.util.Map;

public class UnstableEnergyCannonRenderer
        extends GeoEntityRenderer<UnstableEnergyCannonEntity> {

    private static final double SIDE_OFFSET = -2.2D;
    private static final double HEIGHT_OFFSET = 0.95D;
    private static final double FORWARD_OFFSET = -0.67D;

    private static final double VISUAL_FOLLOW_SPEED = 0.03D;

    private static final double VISUAL_SNAP_DISTANCE = 4.0D;

    private final Map<Integer, Vec3> visualPositions =
            new HashMap<>();

    private static final float INWARD_ANGLE = 12.0F;

    private static final String OUTLINE_BONE =
            "EnergyCannonOutline";

    private static final String ROOT_BONE =
            "MarsUnstableEnergyCannon";

    private static final String MUZZLE_BONE =
            "LaserPivot";

    private static final Vec3 MUZZLE_BONE_OFFSET =
            Vec3.ZERO;

    private final Matrix4f entryPose = new Matrix4f();
    private double entryX;
    private double entryY;
    private double entryZ;

    private final UnstableEnergyCannonGlowLayer glowLayer;
    private final UnstableEnergyCannonOutlineLayer outlineLayer;

    public UnstableEnergyCannonRenderer(
            EntityRendererProvider.Context context
    ) {
        super(
                context,
                new UnstableEnergyCannonModel()
        );

        this.shadowRadius = 0.0F;

        this.glowLayer =
                new UnstableEnergyCannonGlowLayer(this);

        this.outlineLayer =
                new UnstableEnergyCannonOutlineLayer(this);

        this.addRenderLayer(
                glowLayer
        );
    }

    @Override
    public void render(
            UnstableEnergyCannonEntity entity,
            float entityYaw,
            float partialTick,
            PoseStack poseStack,
            MultiBufferSource bufferSource,
            int packedLight
    ) {

        this.entryPose.set(poseStack.last().pose());

        this.entryX = Mth.lerp(
                partialTick,
                entity.xo,
                entity.getX()
        );

        this.entryY = Mth.lerp(
                partialTick,
                entity.yo,
                entity.getY()
        );

        this.entryZ = Mth.lerp(
                partialTick,
                entity.zo,
                entity.getZ()
        );

        Player owner = entity.getOwner();

        if (owner != null && owner.isAlive()) {

            double playerX = Mth.lerp(
                    partialTick,
                    owner.xo,
                    owner.getX()
            );

            double playerY = Mth.lerp(
                    partialTick,
                    owner.yo,
                    owner.getY()
            );

            double playerZ = Mth.lerp(
                    partialTick,
                    owner.zo,
                    owner.getZ()
            );

            float bodyYaw = Mth.rotLerp(
                    partialTick,
                    owner.yBodyRotO,
                    owner.yBodyRot
            );

            double yawRadians =
                    Math.toRadians(bodyYaw);

            Vec3 forward = new Vec3(
                    -Math.sin(yawRadians),
                    0.0D,
                    Math.cos(yawRadians)
            );

            Vec3 right = new Vec3(
                    -Math.cos(yawRadians),
                    0.0D,
                    -Math.sin(yawRadians)
            );

            Vec3 desiredPosition =
                    new Vec3(
                            playerX,
                            playerY,
                            playerZ
                    )
                            .add(
                                    right.scale(
                                            Math.abs(SIDE_OFFSET)
                                    )
                            )
                            .add(
                                    forward.scale(
                                            FORWARD_OFFSET
                                    )
                            )
                            .add(
                                    0.0D,
                                    HEIGHT_OFFSET,
                                    0.0D
                            );

            int entityId = entity.getId();

            Vec3 visualPosition =
                    this.visualPositions.get(entityId);

            if (visualPosition == null) {

                visualPosition = desiredPosition;

            } else {

                double distance =
                        visualPosition.distanceTo(
                                desiredPosition
                        );

                if (distance > VISUAL_SNAP_DISTANCE) {

                    visualPosition = desiredPosition;

                } else {

                    visualPosition =
                            visualPosition.lerp(
                                    desiredPosition,
                                    VISUAL_FOLLOW_SPEED
                            );
                }
            }

            this.visualPositions.put(
                    entityId,
                    visualPosition
            );

            poseStack.pushPose();

            poseStack.translate(
                    visualPosition.x - this.entryX,
                    visualPosition.y - this.entryY,
                    visualPosition.z - this.entryZ
            );

            float desiredYawOffset = 0.0F;
            float desiredPitch = 0.0F;

            LivingEntity lockTarget =
                    entity.getClientLockTarget();

            if (lockTarget != null) {

                Vec3 aimOrigin =
                        desiredPosition.add(
                                0.0D,
                                UnstableEnergyCannonEntity.AIM_PIVOT_Y,
                                0.0D
                        );

                Vec3 targetPoint =
                        new Vec3(
                                Mth.lerp(
                                        partialTick,
                                        lockTarget.xo,
                                        lockTarget.getX()
                                ),
                                Mth.lerp(
                                        partialTick,
                                        lockTarget.yo,
                                        lockTarget.getY()
                                ) + lockTarget.getBbHeight() * 0.5D,
                                Mth.lerp(
                                        partialTick,
                                        lockTarget.zo,
                                        lockTarget.getZ()
                                )
                        );

                Vec3 delta =
                        targetPoint.subtract(
                                aimOrigin
                        );

                double horizontal =
                        Math.sqrt(
                                delta.x * delta.x
                                        + delta.z * delta.z
                        );

                float targetYaw =
                        (float) (
                                Mth.atan2(
                                        -delta.x,
                                        delta.z
                                )
                                        * (180.0D / Math.PI)
                        );

                float targetPitch =
                        (float) (
                                -(
                                        Mth.atan2(
                                                delta.y,
                                                horizontal
                                        )
                                                * (180.0D / Math.PI)
                                )
                        );

                float restYaw =
                        bodyYaw - INWARD_ANGLE;

                desiredYawOffset =
                        Mth.clamp(
                                Mth.wrapDegrees(
                                        targetYaw - restYaw
                                ),
                                -UnstableEnergyCannonEntity.AIM_MAX_YAW_DEGREES,
                                UnstableEnergyCannonEntity.AIM_MAX_YAW_DEGREES
                        );

                desiredPitch =
                        Mth.clamp(
                                targetPitch,
                                -UnstableEnergyCannonEntity.AIM_MAX_PITCH_DEGREES,
                                UnstableEnergyCannonEntity.AIM_MAX_PITCH_DEGREES
                        );
            }

            entity.updateVisualAim(
                    desiredYawOffset,
                    desiredPitch,
                    partialTick
            );

            float aimYawOffset =
                    entity.getVisualAimYawOffset();

            float aimPitch =
                    entity.getVisualAimPitch();

            poseStack.mulPose(
                    Axis.YP.rotationDegrees(
                            -bodyYaw
                                    + INWARD_ANGLE
                                    - aimYawOffset
                    )
            );

            poseStack.translate(
                    0.0D,
                    UnstableEnergyCannonEntity.AIM_PIVOT_Y,
                    0.0D
            );

            poseStack.mulPose(
                    Axis.XP.rotationDegrees(
                            aimPitch
                    )
            );

            poseStack.translate(
                    0.0D,
                    -UnstableEnergyCannonEntity.AIM_PIVOT_Y,
                    0.0D
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

            return;
        }

        this.visualPositions.remove(
                entity.getId()
        );

        super.render(
                entity,
                entityYaw,
                partialTick,
                poseStack,
                bufferSource,
                packedLight
        );
    }

    private void sampleModelPose(
            PoseStack poseStack,
            UnstableEnergyCannonEntity animatable
    ) {
        Matrix4f toEntitySpace =
                new Matrix4f(this.entryPose)
                        .invert()
                        .mul(poseStack.last().pose());

        animatable.setClientModelPose(
                toEntitySpace,
                this.entryX,
                this.entryY,
                this.entryZ
        );
    }

    private void sampleMuzzle(
            PoseStack poseStack,
            GeoBone bone,
            UnstableEnergyCannonEntity animatable
    ) {
        Vector4f local = new Vector4f(
                (float) (
                        (bone.getPivotX()
                                + MUZZLE_BONE_OFFSET.x)
                                / 16.0D
                ),
                (float) (
                        (bone.getPivotY()
                                + MUZZLE_BONE_OFFSET.y)
                                / 16.0D
                ),
                (float) (
                        (bone.getPivotZ()
                                + MUZZLE_BONE_OFFSET.z)
                                / 16.0D
                ),
                1.0F
        );

        Matrix4f toEntitySpace =
                new Matrix4f(this.entryPose)
                        .invert()
                        .mul(poseStack.last().pose());

        local.mul(toEntitySpace);

        animatable.setClientMuzzleSample(
                new Vec3(
                        this.entryX + local.x(),
                        this.entryY + local.y(),
                        this.entryZ + local.z()
                )
        );
    }

    @Override
    public void renderRecursively(
            PoseStack poseStack,
            UnstableEnergyCannonEntity animatable,
            GeoBone bone,
            RenderType renderType,
            MultiBufferSource bufferSource,
            VertexConsumer buffer,
            boolean isReRender,
            float partialTick,
            int packedLight,
            int packedOverlay,
            float red,
            float green,
            float blue,
            float alpha
    ) {
        poseStack.pushPose();

        RenderUtils.prepMatrixForBone(
                poseStack,
                bone
        );

        if (!isReRender
                && ROOT_BONE.equals(bone.getName())) {

            this.sampleModelPose(
                    poseStack,
                    animatable
            );
        }

        if (!isReRender
                && MUZZLE_BONE.equals(bone.getName())) {

            this.sampleMuzzle(
                    poseStack,
                    bone,
                    animatable
            );
        }

        if (!isReRender
                && OUTLINE_BONE.equals(bone.getName())) {

            this.outlineLayer.renderOutlineCubes(
                    poseStack,
                    bone,
                    bufferSource,
                    packedLight,
                    packedOverlay
            );

            poseStack.popPose();

            return;
        }

        if (!OUTLINE_BONE.equals(bone.getName())) {

            this.renderCubesOfBone(
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

            if (!isReRender) {

                this.applyRenderLayersForBone(
                        poseStack,
                        animatable,
                        bone,
                        renderType,
                        bufferSource,
                        buffer,
                        partialTick,
                        packedLight,
                        packedOverlay
                );
            }
        }

        this.renderChildBones(
                poseStack,
                animatable,
                bone,
                renderType,
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

        poseStack.popPose();
    }
}