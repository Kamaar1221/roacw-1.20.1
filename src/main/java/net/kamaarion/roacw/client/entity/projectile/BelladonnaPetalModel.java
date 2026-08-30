package net.kamaarion.roacw.client.entity.projectile;

import net.kamaarion.roacw.ROACW;
import net.kamaarion.roacw.entity.projectile.belladonna_petal.BelladonnaPetalEntity;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public class BelladonnaPetalModel extends GeoModel<BelladonnaPetalEntity> {

    @Override
    public ResourceLocation getModelResource(BelladonnaPetalEntity animatable) {
        return ResourceLocation.fromNamespaceAndPath(ROACW.MODID, "geo/entity/projectile/belladonna_petal.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(BelladonnaPetalEntity animatable) {
        return ResourceLocation.fromNamespaceAndPath(ROACW.MODID, "textures/entity/projectile/belladonna_petal.png");
    }

    @Override
    public ResourceLocation getAnimationResource(BelladonnaPetalEntity animatable) {
        return ResourceLocation.fromNamespaceAndPath(ROACW.MODID, "animations/impaling_column_shards.animation.json");
    }
}