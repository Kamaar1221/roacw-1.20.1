package net.kamaarion.roacw.client.entity.projectile;

import net.kamaarion.roacw.entity.spells.impaling_column.ImpalingColumnShards;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public class ImpalingColumnShardsModel extends GeoModel<ImpalingColumnShards> {
    @Override
    public ResourceLocation getModelResource(ImpalingColumnShards animatable) {
        return ResourceLocation.fromNamespaceAndPath("roacw", "geo/spell/earth_splitter_shards.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(ImpalingColumnShards animatable) {
        return ResourceLocation.fromNamespaceAndPath("roacw", "textures/entity/spell/earth_splitter/earth_splitter_shards.png");
    }

    @Override
    public ResourceLocation getAnimationResource(ImpalingColumnShards animatable) {
        return ResourceLocation.fromNamespaceAndPath("roacw", "animations/entity/impaling_column_shards.animation.json");
    }
}
