package net.kamaarion.roacw.entity.projectile.plague_bee;

import net.kamaarion.roacw.ROACW;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public class PlagueBeeModel extends GeoModel<PlagueBeeEntity> {

    @Override
    public ResourceLocation getModelResource(PlagueBeeEntity animatable) {
        return ResourceLocation.fromNamespaceAndPath(
                ROACW.MODID,
                "geo/entity/projectile/plague_bee.geo.json"
        );
    }

    @Override
    public ResourceLocation getTextureResource(PlagueBeeEntity animatable) {
        return ResourceLocation.fromNamespaceAndPath(
                ROACW.MODID,
                "textures/entity/projectile/plague_bee.png"
        );
    }

    @Override
    public ResourceLocation getAnimationResource(PlagueBeeEntity animatable) {
        return ResourceLocation.fromNamespaceAndPath(
                ROACW.MODID,
                "animations/entity/projectile/plague_bee.animation.json"
        );
    }
}