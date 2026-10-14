package net.kamaarion.roacw.entity.summon.dark_raven;

import net.kamaarion.roacw.ROACW;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public class DarkRavenModel extends GeoModel<DarkRavenEntity> {

    @Override
    public ResourceLocation getModelResource(DarkRavenEntity animatable) {
        return ResourceLocation.fromNamespaceAndPath(
                ROACW.MODID,
                "geo/entity/summon/dark_raven.geo.json"
        );
    }

    @Override
    public ResourceLocation getTextureResource(DarkRavenEntity animatable) {
        return ResourceLocation.fromNamespaceAndPath(
                ROACW.MODID,
                "textures/entity/summon/dark_raven.png"
        );
    }

    @Override
    public ResourceLocation getAnimationResource(DarkRavenEntity animatable) {
        return ResourceLocation.fromNamespaceAndPath(
                ROACW.MODID,
                "animations/entity/summon/dark_raven.animation.json"
        );
    }
}