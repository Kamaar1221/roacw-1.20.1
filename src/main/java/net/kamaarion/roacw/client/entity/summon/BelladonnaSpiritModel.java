package net.kamaarion.roacw.client.entity.summon;

import net.kamaarion.roacw.ROACW;
import net.kamaarion.roacw.entity.summon.belladonna_spirit.BelladonnaSpiritEntity;
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
        return ResourceLocation.fromNamespaceAndPath(ROACW.MODID, "animations/entity/belladonna_spirit.animation.json");
    }
}