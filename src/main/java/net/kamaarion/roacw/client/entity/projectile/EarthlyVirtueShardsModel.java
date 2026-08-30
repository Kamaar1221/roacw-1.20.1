package net.kamaarion.roacw.client.entity.projectile;

import net.kamaarion.roacw.entity.spells.earthly_virtue.EarthlyVirtueShards;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public class EarthlyVirtueShardsModel extends GeoModel<EarthlyVirtueShards> {
    @Override
    public ResourceLocation getModelResource(EarthlyVirtueShards animatable) {
        return ResourceLocation.fromNamespaceAndPath("roacw", "geo/spell/earth_splitter_shards.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(EarthlyVirtueShards animatable) {
        return ResourceLocation.fromNamespaceAndPath("roacw", "textures/entity/spell/earth_splitter/earth_splitter_shards.png");
    }

    @Override
    public ResourceLocation getAnimationResource(EarthlyVirtueShards animatable) {
        return ResourceLocation.fromNamespaceAndPath("roacw", "animations/entity/earthly_virtue_shards.animation.json");
    }
}
