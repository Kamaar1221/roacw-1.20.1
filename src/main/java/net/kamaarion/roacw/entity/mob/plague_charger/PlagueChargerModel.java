package net.kamaarion.roacw.entity.mob.plague_charger;

import net.kamaarion.roacw.ROACW;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public class PlagueChargerModel extends GeoModel<PlagueChargerEntity> {

    @Override
    public ResourceLocation getModelResource(PlagueChargerEntity animatable) {
        return ResourceLocation.fromNamespaceAndPath(ROACW.MODID, "geo/entity/mob/plague_charger.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(PlagueChargerEntity animatable) {
        return ResourceLocation.fromNamespaceAndPath(ROACW.MODID, "textures/entity/mob/plague_charger.png");
    }

    @Override
    public ResourceLocation getAnimationResource(PlagueChargerEntity animatable) {
        return ResourceLocation.fromNamespaceAndPath(ROACW.MODID, "animations/entity/mob/plague_charger.animation.json");
    }
}