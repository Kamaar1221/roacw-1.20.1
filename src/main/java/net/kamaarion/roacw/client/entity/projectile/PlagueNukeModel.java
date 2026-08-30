package net.kamaarion.roacw.client.entity.projectile;

import net.kamaarion.roacw.entity.projectile.plague_nuke.PlagueNukeEntity;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public class PlagueNukeModel extends GeoModel<PlagueNukeEntity> {
    @Override
    public ResourceLocation getModelResource(PlagueNukeEntity animatable) {
        return ResourceLocation.fromNamespaceAndPath("roacw", "geo/entity/projectile/plague_nuke.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(PlagueNukeEntity animatable) {
        return ResourceLocation.fromNamespaceAndPath("roacw", "textures/entity/projectile/plague_nuke.png");
    }

    @Override
    public ResourceLocation getAnimationResource(PlagueNukeEntity animatable) {
        return ResourceLocation.fromNamespaceAndPath("roacw", "animations/entity/impaling_column_shards.animation.json");
    }
}