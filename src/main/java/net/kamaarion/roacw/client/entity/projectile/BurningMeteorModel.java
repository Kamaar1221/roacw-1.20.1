package net.kamaarion.roacw.client.entity.projectile;

import net.kamaarion.roacw.entity.spells.burning_meteor.BurningMeteorEntity;
import net.kamaarion.roacw.entity.spells.earthly_virtue.EarthlyVirtueShards;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public class BurningMeteorModel extends GeoModel<BurningMeteorEntity> {
    @Override
    public ResourceLocation getModelResource(BurningMeteorEntity animatable) {
        return ResourceLocation.fromNamespaceAndPath("roacw", "geo/spell/burning_meteor.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(BurningMeteorEntity animatable) {
        return ResourceLocation.fromNamespaceAndPath("roacw", "textures/entity/spell/burning_meteor/burning_meteor.png");
    }

    @Override
    public ResourceLocation getAnimationResource(BurningMeteorEntity animatable) {
        return ResourceLocation.fromNamespaceAndPath("roacw", "animations/entity/earthly_virtue_shards.animation.json");
    }
}
