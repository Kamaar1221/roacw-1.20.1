package net.kamaarion.roacw.client.entity;

import net.kamaarion.roacw.entity.spells.earthly_virtue.EarthlyVirtueShards;
import net.kamaarion.roacw.entity.spells.impaling_column.ImpalingColumnShards;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public class EarthlyVirtueShardsModel extends GeoModel<EarthlyVirtueShards> {
    @Override
    public ResourceLocation getModelResource(EarthlyVirtueShards animatable) {
        // Points to your exported Blockbench GeckoLib JSON map file
        return ResourceLocation.fromNamespaceAndPath("roacw", "geo/spell/earth_splitter_shards.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(EarthlyVirtueShards animatable) {
        // Points to your texturing sheet PNG
        return ResourceLocation.fromNamespaceAndPath("roacw", "textures/entity/crushing_smash/earth_splitter_shards.png");
    }

    @Override
    public ResourceLocation getAnimationResource(EarthlyVirtueShards animatable) {
        // Points to your animation sequence timelines file
        return ResourceLocation.fromNamespaceAndPath("roacw", "animations/entity/earthly_virtue_shards.animation.json");
    }
}
