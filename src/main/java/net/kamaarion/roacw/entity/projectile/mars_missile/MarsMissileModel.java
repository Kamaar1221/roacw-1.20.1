package net.kamaarion.roacw.entity.projectile.mars_missile;

import net.kamaarion.roacw.ROACW;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public class MarsMissileModel extends GeoModel<MarsMissileEntity> {

    @Override
    public ResourceLocation getModelResource(MarsMissileEntity animatable) {
        return ResourceLocation.fromNamespaceAndPath(
                ROACW.MODID,
                "geo/entity/projectile/mars_missile.geo.json"
        );
    }

    @Override
    public ResourceLocation getTextureResource(MarsMissileEntity animatable) {
        return ResourceLocation.fromNamespaceAndPath(
                ROACW.MODID,
                "textures/entity/projectile/mars_missile.png"
        );
    }

    @Override
    public ResourceLocation getAnimationResource(MarsMissileEntity animatable) {
        return ResourceLocation.fromNamespaceAndPath(
                ROACW.MODID,
                "animations/entity/impaling_column_shards.animation.json"
        );
    }
}