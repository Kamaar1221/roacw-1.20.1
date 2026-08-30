package net.kamaarion.roacw.client.entity.summon;

import net.kamaarion.roacw.ROACW;
import net.kamaarion.roacw.entity.summon.plague_charger.PlagueChargerEntity;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public class PlagueChargerModel extends GeoModel<PlagueChargerEntity> {

    @Override
    public ResourceLocation getModelResource(PlagueChargerEntity animatable) {
        return ResourceLocation.fromNamespaceAndPath(ROACW.MODID, "geo/entity/summon/plague_charger.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(PlagueChargerEntity animatable) {
        return ResourceLocation.fromNamespaceAndPath(ROACW.MODID, "textures/entity/summon/plague_charger.png");
    }

    @Override
    public ResourceLocation getAnimationResource(PlagueChargerEntity animatable) {
        return ResourceLocation.fromNamespaceAndPath(ROACW.MODID, "animations/entity/plague_charger.animation.json");
    }
}