package net.kamaarion.roacw.entity.summon.endo_hydra;

import net.kamaarion.roacw.ROACW;
import net.kamaarion.roacw.client.others.BoneTransformHelper;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.cache.object.GeoBone;
import software.bernie.geckolib.core.animation.AnimationState;
import software.bernie.geckolib.model.GeoModel;

public class EndoHydraModel extends GeoModel<EndoHydraEntity> {

    private static final ResourceLocation MODEL =
            ResourceLocation.fromNamespaceAndPath(
                    ROACW.MODID,
                    "geo/entity/summon/endo_hydra.geo.json"
            );

    private static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath(
                    ROACW.MODID,
                    "textures/entity/summon/endo_hydra.png"
            );

    private static final ResourceLocation ANIMATION =
            ResourceLocation.fromNamespaceAndPath(
                    ROACW.MODID,
                    "animations/entity/summon/endo_hydra.animation.json"
            );

    private static final String[] HEAD_BONES = {
            EndoHydraEntity.HEAD_1_BONE,
            EndoHydraEntity.HEAD_2_BONE,
            EndoHydraEntity.HEAD_3_BONE,
            EndoHydraEntity.HEAD_4_BONE,
            EndoHydraEntity.HEAD_5_BONE
    };

    private static final String[] MOUTH_BONES = {
            EndoHydraEntity.MOUTH_1_BONE,
            EndoHydraEntity.MOUTH_2_BONE,
            EndoHydraEntity.MOUTH_3_BONE,
            EndoHydraEntity.MOUTH_4_BONE,
            EndoHydraEntity.MOUTH_5_BONE
    };

    @Override
    public ResourceLocation getModelResource(EndoHydraEntity animatable) {
        return MODEL;
    }

    @Override
    public ResourceLocation getTextureResource(EndoHydraEntity animatable) {
        return TEXTURE;
    }

    @Override
    public ResourceLocation getAnimationResource(EndoHydraEntity animatable) {
        return ANIMATION;
    }

    @Override
    public void setCustomAnimations(
            EndoHydraEntity animatable,
            long instanceId,
            AnimationState<EndoHydraEntity> animationState
    ) {
        BakedGeoModel model = getBakedModel(getModelResource(animatable));

        for (String boneName : HEAD_BONES) {
            model.getBone(boneName).ifPresent(bone -> {
                bone.setRotX(0);
                bone.setRotY(0);
                bone.setRotZ(0);
            });
        }

        super.setCustomAnimations(animatable, instanceId, animationState);

        updateHeadVisibility(model, animatable);

        float partialTick = animationState.getPartialTick();

        for (int i = 0; i < HEAD_BONES.length; i++) {
            if (!animatable.isHeadActive(i)) {
                continue;
            }

            applyHeadRotation(
                    model,
                    HEAD_BONES[i],
                    animatable,
                    i,
                    partialTick
            );
        }

        updateMouthPositions(animatable);
    }

    private void updateHeadVisibility(
            BakedGeoModel model,
            EndoHydraEntity animatable
    ) {
        for (String rootBoneName : EndoHydraEntity.ALL_HEAD_ROOTS_FOR_RENDERING) {
            model.getBone(rootBoneName).ifPresent(bone -> bone.setHidden(false));
        }

        for (String rootBoneName : animatable.getHiddenHeadRootBones()) {
            model.getBone(rootBoneName).ifPresent(bone -> bone.setHidden(true));
        }
    }

    private void applyHeadRotation(
            BakedGeoModel model,
            String boneName,
            EndoHydraEntity animatable,
            int headIndex,
            float partialTick
    ) {
        GeoBone bone = model.getBone(boneName).orElse(null);

        if (bone == null) {
            return;
        }

        float previousYaw = animatable.getHeadPreviousYaw(headIndex);
        float currentYaw = animatable.getHeadLookYaw(headIndex);

        float interpolatedYaw = Mth.rotLerp(
                partialTick,
                previousYaw,
                currentYaw
        );

        float previousPitch = animatable.getHeadPreviousPitch(headIndex);
        float currentPitch = animatable.getHeadLookPitch(headIndex);

        float interpolatedPitch = Mth.lerp(
                partialTick,
                previousPitch,
                currentPitch
        );

        float visualYaw = Mth.wrapDegrees(-interpolatedYaw);
        float targetYawRadians = visualYaw * Mth.DEG_TO_RAD;
        float targetPitchRadians = interpolatedPitch * Mth.DEG_TO_RAD;

        float animationYaw = bone.getRotY();
        float animationPitch = bone.getRotX();

        bone.setRotY(animationYaw + targetYawRadians);
        bone.setRotX(animationPitch + targetPitchRadians);
    }

    private void updateMouthPositions(EndoHydraEntity animatable) {
        for (int i = 0; i < MOUTH_BONES.length; i++) {
            if (!animatable.isHeadActive(i)) {
                continue;
            }

            GeoBone mouthBone = getBone(MOUTH_BONES[i]).orElse(null);

            if (mouthBone == null) {
                continue;
            }

            Vec3 worldPosition = BoneTransformHelper.getWorldPosition(mouthBone);

            if (worldPosition == null) {
                continue;
            }

            animatable.setClientMouthPosition(i, worldPosition);
        }
    }
}