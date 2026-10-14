package net.kamaarion.roacw.entity.summon.virili;

import net.kamaarion.roacw.ROACW;
import net.kamaarion.roacw.client.others.BoneTransformHelper;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import software.bernie.geckolib.cache.object.GeoBone;
import software.bernie.geckolib.core.animation.AnimationState;
import software.bernie.geckolib.model.GeoModel;

public class ViriliModel extends GeoModel<ViriliEntity> {

    private static final String MISSILE_SPAWN_BONE = "Missilespawn";
    private static final String ROOT_BONE = "ViriliBody";
    private static final float TILT_MULTIPLIER = 1.6F;

    @Override
    public ResourceLocation getModelResource(ViriliEntity animatable) {
        return ResourceLocation.fromNamespaceAndPath(
                ROACW.MODID,
                "geo/entity/summon/virili.geo.json"
        );
    }

    @Override
    public ResourceLocation getTextureResource(ViriliEntity animatable) {
        return ResourceLocation.fromNamespaceAndPath(
                ROACW.MODID,
                "textures/entity/summon/virili.png"
        );
    }

    @Override
    public ResourceLocation getAnimationResource(ViriliEntity animatable) {
        return ResourceLocation.fromNamespaceAndPath(
                ROACW.MODID,
                "animations/entity/summon/virili.animation.json"
        );
    }

    @Override
    public void setCustomAnimations(
            ViriliEntity animatable,
            long instanceId,
            AnimationState<ViriliEntity> animationState
    ) {
        super.setCustomAnimations(
                animatable,
                instanceId,
                animationState
        );

        updateMissileSpawnPosition(animatable);
        applyBodyPitchTilt(animatable);
    }

    private void applyBodyPitchTilt(ViriliEntity animatable) {
        GeoBone rootBone = getBone(ROOT_BONE).orElse(null);

        if (rootBone == null) {
            return;
        }

        float pitchRadians = -animatable.getXRot()
                * TILT_MULTIPLIER
                * Mth.DEG_TO_RAD;

        rootBone.setRotX(
                rootBone.getRotX() + pitchRadians
        );
    }

    private void updateMissileSpawnPosition(ViriliEntity animatable) {
        GeoBone missileSpawnBone = getBone(MISSILE_SPAWN_BONE).orElse(null);

        if (missileSpawnBone == null) {
            return;
        }

        Vec3 worldPosition = BoneTransformHelper.getWorldPosition(
                missileSpawnBone
        );

        if (worldPosition == null) {
            return;
        }

        animatable.setClientMissileSpawnPosition(worldPosition);
    }
}