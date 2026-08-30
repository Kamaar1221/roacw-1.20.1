package net.kamaarion.roacw.client.entity.projectile;

import net.kamaarion.roacw.entity.projectile.plague_rocket.PlagueRocketEntity;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public class PlagueRocketModel extends GeoModel<PlagueRocketEntity> {
    @Override
    public ResourceLocation getModelResource(PlagueRocketEntity animatable) {
        return ResourceLocation.fromNamespaceAndPath("roacw", "geo/entity/projectile/plague_rocket.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(PlagueRocketEntity animatable) {
        return ResourceLocation.fromNamespaceAndPath("roacw", "textures/entity/projectile/plague_rocket.png");
    }

    @Override
    public ResourceLocation getAnimationResource(PlagueRocketEntity animatable) {
        return ResourceLocation.fromNamespaceAndPath("roacw", "animations/entity/plague_rocket.animation.json");
            }
}