package net.kamaarion.roacw.client.entity.projectile;

import net.kamaarion.roacw.ROACW;
import net.kamaarion.roacw.entity.projectile.god_killer_dart.GodKillerDartEntity;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public class GodKillerDartModel extends GeoModel<GodKillerDartEntity> {

    @Override
    public ResourceLocation getModelResource(GodKillerDartEntity animatable) {
        return ResourceLocation.fromNamespaceAndPath(ROACW.MODID, "geo/entity/projectile/god_killer_dart.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(GodKillerDartEntity animatable) {
        return ResourceLocation.fromNamespaceAndPath(ROACW.MODID, "textures/entity/projectile/god_killer_dart.png");
    }

    @Override
    public ResourceLocation getAnimationResource(GodKillerDartEntity animatable) {
        return ResourceLocation.fromNamespaceAndPath(ROACW.MODID, "animations/impaling_column_shards.animation.json");
    }
}