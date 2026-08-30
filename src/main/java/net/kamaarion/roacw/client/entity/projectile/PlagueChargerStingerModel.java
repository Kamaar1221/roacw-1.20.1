package net.kamaarion.roacw.client.entity.projectile;

import net.kamaarion.roacw.ROACW;
import net.kamaarion.roacw.entity.projectile.plague_charger_stinger.PlagueChargerStingerEntity;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public class PlagueChargerStingerModel extends GeoModel<PlagueChargerStingerEntity> {

    @Override
    public ResourceLocation getModelResource(PlagueChargerStingerEntity animatable) {
        return ResourceLocation.fromNamespaceAndPath(ROACW.MODID, "geo/entity/projectile/plague_charger_stinger.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(PlagueChargerStingerEntity animatable) {
        return ResourceLocation.fromNamespaceAndPath(ROACW.MODID, "textures/entity/projectile/plague_charger_stinger.png");
    }

    @Override
    public ResourceLocation getAnimationResource(PlagueChargerStingerEntity animatable) {
        return ResourceLocation.fromNamespaceAndPath(ROACW.MODID, "animations/impaling_column_shards.animation.json");
    }
}