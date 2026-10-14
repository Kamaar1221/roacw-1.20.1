package net.kamaarion.roacw.entity.summon.belladonna_spirit;

import net.kamaarion.roacw.ROACW;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public class BelladonnaSpiritModel extends GeoModel<BelladonnaSpiritEntity> {

    @Override
    public ResourceLocation getModelResource(BelladonnaSpiritEntity animatable) {
        return ResourceLocation.fromNamespaceAndPath(ROACW.MODID, "geo/entity/summon/belladonna_spirit.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(BelladonnaSpiritEntity animatable) {
        return ResourceLocation.fromNamespaceAndPath(ROACW.MODID, "textures/entity/summon/belladonna_spirit.png");
    }

    @Override
    public ResourceLocation getAnimationResource(BelladonnaSpiritEntity animatable) {
        return ResourceLocation.fromNamespaceAndPath(ROACW.MODID, "animations/entity/summon/belladonna_spirit.animation.json");
    }
}