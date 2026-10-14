package net.kamaarion.roacw.entity.projectile.plague_missile;

import net.kamaarion.roacw.ROACW;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public class PlagueMissileModel extends GeoModel<PlagueMissileEntity> {

    @Override
    public ResourceLocation getModelResource(PlagueMissileEntity animatable) {
        return ResourceLocation.fromNamespaceAndPath(
                ROACW.MODID,
                "geo/entity/projectile/plague_missile.geo.json"
        );
    }

    @Override
    public ResourceLocation getTextureResource(PlagueMissileEntity animatable) {
        return ResourceLocation.fromNamespaceAndPath(
                ROACW.MODID,
                "textures/entity/projectile/plague_missile.png"
        );
    }

    @Override
    public ResourceLocation getAnimationResource(PlagueMissileEntity animatable) {
        return ResourceLocation.fromNamespaceAndPath(
                ROACW.MODID,
                "animations/entity/impaling_column_shards.animation.json"
        );
    }
}