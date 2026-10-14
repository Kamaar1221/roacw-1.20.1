package net.kamaarion.roacw.entity.armorset_summon.railgun_cannon;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.cache.object.GeoBone;
import software.bernie.geckolib.renderer.GeoEntityRenderer;
import software.bernie.geckolib.util.RenderUtils;

import java.util.HashMap;
import java.util.Map;

public class RailgunCannonRenderer
        extends GeoEntityRenderer<RailgunCannonEntity> {

    private static final double VISUAL_FOLLOW_SPEED = 0.03D;

    private static final double VISUAL_SNAP_DISTANCE = 4.0D;

    private final Map<Integer, Vec3> visualPositions =
            new HashMap<>();

    private static final double SIDE_OFFSET = 2.1D;
    private static final double HEIGHT_OFFSET = 0.95D;
    private static final double FORWARD_OFFSET = 0.4D;

    private static final float INWARD_ANGLE = 12.0F;

    private static final String OUTLINE_BONE = "RailgunOutline";

    private static final String ROOT_BONE = "MarsRailgunCannon";

    private final Matrix4f entryPose = new Matrix4f();
    private double entryX;
    private double entryY;
    private double entryZ;

    private final RailgunCannonGlowLayer glowLayer;
    private final RailgunCannonOutlineLayer outlineLayer;

    private boolean coreCubePass = false;

    public RailgunCannonRenderer(
            EntityRendererProvider.Context context
    ) {
        super(
                context,
                new RailgunCannonModel()
        );

        this.shadowRadius = 0.0F;

        this.glowLayer =
                new RailgunCannonGlowLayer(this);

        this.outlineLayer =
                new RailgunCannonOutlineLayer(this);

        this.addRenderLayer(
                glowLayer
        );

    }

    @Override
    public void render(
            RailgunCannonEntity entity,
            float entityYaw,
            float partialTick,
            PoseStack poseStack,
            MultiBufferSource bufferSource,
            int packedLight
    ) {
        this.entryPose.set(poseStack.last().pose());

        this.entryX =
                Mth.lerp(
                        partialTick,
                        entity.xo,
                        entity.getX()
                );

        this.entryY =
                Mth.lerp(
                        partialTick,
                        entity.yo,
                        entity.getY()
                );

        this.entryZ =
                Mth.lerp(
                        partialTick,
                        entity.zo,
                        entity.getZ()
                );

        Player owner =
                entity.getOwner();

        if (owner == null
                || !owner.isAlive()) {

            visualPositions.remove(
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

            return;
        }

        double ownerX =
                Mth.lerp(
                        partialTick,
                        owner.xo,
                        owner.getX()
                );

        double ownerY =
                Mth.lerp(
                        partialTick,
                        owner.yo,
                        owner.getY()
                );

        double ownerZ =
                Mth.lerp(
                        partialTick,
                        owner.zo,
                        owner.getZ()
                );

        float bodyYaw =
                Mth.rotLerp(
                        partialTick,
                        owner.yBodyRotO,
                        owner.yBodyRot
                );

        double yawRadians =
                Math.toRadians(
                        bodyYaw
                );

        Vec3 forward =
                new Vec3(
                        -Math.sin(yawRadians),
                        0.0D,
                        Math.cos(yawRadians)
                );

        Vec3 left =
                new Vec3(
                        Math.cos(yawRadians),
                        0.0D,
                        Math.sin(yawRadians)
                );

        Vec3 targetPosition =
                new Vec3(
                        ownerX,
                        ownerY,
                        ownerZ
                )
                        .add(
                                left.scale(
                                        SIDE_OFFSET
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

        int entityId =
                entity.getId();

        Vec3 visualPosition =
                visualPositions.get(
                        entityId
                );

        if (visualPosition == null) {

            visualPosition =
                    targetPosition;

            visualPositions.put(
                    entityId,
                    visualPosition
            );
        }

        else if (
                visualPosition.distanceToSqr(
                        targetPosition
                )
                        >=
                        VISUAL_SNAP_DISTANCE
                                * VISUAL_SNAP_DISTANCE
        ) {

            visualPosition =
                    targetPosition;

            visualPositions.put(
                    entityId,
                    visualPosition
            );
        }

        else {

            visualPosition =
                    visualPosition.lerp(
                            targetPosition,
                            VISUAL_FOLLOW_SPEED
                    );

            visualPositions.put(
                    entityId,
                    visualPosition
            );
        }

        double entityX =
                Mth.lerp(
                        partialTick,
                        entity.xo,
                        entity.getX()
                );

        double entityY =
                Mth.lerp(
                        partialTick,
                        entity.yo,
                        entity.getY()
                );

        double entityZ =
                Mth.lerp(
                        partialTick,
                        entity.zo,
                        entity.getZ()
                );

        poseStack.pushPose();

        poseStack.translate(
                visualPosition.x - entityX,
                visualPosition.y - entityY,
                visualPosition.z - entityZ
        );

        poseStack.mulPose(
                Axis.YP.rotationDegrees(
                        -bodyYaw
                                - INWARD_ANGLE
                )
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

    public void renderCoreCubes(
            PoseStack poseStack,
            RailgunCannonEntity animatable,
            BakedGeoModel model,
            MultiBufferSource bufferSource,
            float partialTick,
            int packedOverlay
    ) {
        RenderType outlineRenderType =
                outlineLayer.getOutlineRenderType();

        VertexConsumer outlineBuffer =
                bufferSource.getBuffer(
                        outlineRenderType
                );

        this.coreCubePass = true;

        try {

            this.reRender(
                    model,
                    poseStack,
                    bufferSource,
                    animatable,
                    outlineRenderType,
                    outlineBuffer,
                    partialTick,
                    0xF000F0,
                    packedOverlay,
                    1.0F,
                    1.0F,
                    1.0F,
                    1.0F
            );

        } finally {

            this.coreCubePass = false;
        }

        if (bufferSource
                instanceof MultiBufferSource.BufferSource bufferSourceImpl) {

            bufferSourceImpl.endBatch(
                    outlineRenderType
            );
        }
    }

    @Override
    public void renderRecursively(
            PoseStack poseStack,
            RailgunCannonEntity animatable,
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

            animatable.setClientModelPose(
                    new Matrix4f(this.entryPose)
                            .invert()
                            .mul(poseStack.last().pose()),
                    this.entryX,
                    this.entryY,
                    this.entryZ
            );
        }

        if (this.coreCubePass) {

            if (OUTLINE_BONE.equals(
                    bone.getName()
            )) {

                outlineLayer.renderCoreCubes(
                        poseStack,
                        bone,
                        buffer
                );

            } else {

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
            }

            poseStack.popPose();

            return;
        }

        if (!isReRender
                && OUTLINE_BONE.equals(
                bone.getName()
        )) {

            outlineLayer.renderOutlineCubes(
                    poseStack,
                    bone,
                    bufferSource,
                    packedLight,
                    packedOverlay
            );

            if (bufferSource
                    instanceof MultiBufferSource.BufferSource bufferSourceImpl) {

                bufferSourceImpl.endBatch(
                        outlineLayer.getOutlineRenderType()
                );
            }

            poseStack.popPose();

            return;
        }

        if (!OUTLINE_BONE.equals(
                bone.getName()
        )) {

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