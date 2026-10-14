package net.kamaarion.roacw.entity.armorset_summon.railgun_cannon;

import net.kamaarion.roacw.ROACW;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public class RailgunCannonModel
        extends GeoModel<RailgunCannonEntity> {

    private static final ResourceLocation MODEL =
            ResourceLocation.fromNamespaceAndPath(
                    ROACW.MODID,
                    "geo/entity/armorset_summon/mars_railgun_cannon.geo.json"
            );

    private static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath(
                    ROACW.MODID,
                    "textures/entity/armorset_summon/mars_railgun_cannon.png"
            );

    private static final ResourceLocation ANIMATION =
            ResourceLocation.fromNamespaceAndPath(
                    ROACW.MODID,
                    "animations/entity/armorset_summon/mars_railgun_cannon.animation.json"
            );

    @Override
    public ResourceLocation getModelResource(
            RailgunCannonEntity animatable
    ) {
        return MODEL;
    }

    @Override
    public ResourceLocation getTextureResource(
            RailgunCannonEntity animatable
    ) {
        return TEXTURE;
    }

    @Override
    public ResourceLocation getAnimationResource(
            RailgunCannonEntity animatable
    ) {
        return ANIMATION;
    }
}