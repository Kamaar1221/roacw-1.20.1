package net.kamaarion.roacw.client.entity;

import net.kamaarion.roacw.entity.spells.impaling_column.ImpalingColumnShards;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public class ImpalingColumnShardsModel extends GeoModel<ImpalingColumnShards> {
    @Override
    public ResourceLocation getModelResource(ImpalingColumnShards animatable) {
        // Points to your exported Blockbench GeckoLib JSON map file
        return ResourceLocation.fromNamespaceAndPath("roacw", "geo/spell/earth_splitter_shards.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(ImpalingColumnShards animatable) {
        // Points to your texturing sheet PNG
        return ResourceLocation.fromNamespaceAndPath("roacw", "textures/entity/crushing_smash/earth_splitter_shards.png");
    }

    @Override
    public ResourceLocation getAnimationResource(ImpalingColumnShards animatable) {
        // Points to your animation sequence timelines file
        return ResourceLocation.fromNamespaceAndPath("roacw", "animations/entity/impaling_column_shards.animation.json");
    }
}
