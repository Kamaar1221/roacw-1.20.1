package net.kamaarion.roacw.entity.projectile.homing_plague_missile;

import net.kamaarion.roacw.ROACW;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public class HomingPlagueMissileModel extends GeoModel<HomingPlagueMissileEntity> {

    @Override
    public ResourceLocation getModelResource(HomingPlagueMissileEntity animatable) {
        return ResourceLocation.fromNamespaceAndPath(
                ROACW.MODID,
                "geo/entity/projectile/homing_plague_missile.geo.json"
        );
    }

    @Override
    public ResourceLocation getTextureResource(HomingPlagueMissileEntity animatable) {
        return ResourceLocation.fromNamespaceAndPath(
                ROACW.MODID,
                "textures/entity/projectile/homing_plague_missile.png"
        );
    }

    @Override
    public ResourceLocation getAnimationResource(HomingPlagueMissileEntity animatable) {
        return ResourceLocation.fromNamespaceAndPath(
                ROACW.MODID,
                "animations/entity/impaling_column_shards.animation.json"
        );
    }
}