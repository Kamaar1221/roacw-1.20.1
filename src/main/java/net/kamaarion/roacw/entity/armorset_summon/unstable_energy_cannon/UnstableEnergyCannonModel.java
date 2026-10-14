package net.kamaarion.roacw.entity.armorset_summon.unstable_energy_cannon;

import net.kamaarion.roacw.ROACW;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public class UnstableEnergyCannonModel
        extends GeoModel<UnstableEnergyCannonEntity> {

    private static final ResourceLocation MODEL =
            ResourceLocation.fromNamespaceAndPath(
                    ROACW.MODID,
                    "geo/entity/armorset_summon/mars_unstable_energy_cannon.geo.json"
            );

    private static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath(
                    ROACW.MODID,
                    "textures/entity/armorset_summon/mars_unstable_energy_cannon.png"
            );

    private static final ResourceLocation ANIMATION =
            ResourceLocation.fromNamespaceAndPath(
                    ROACW.MODID,
                    "animations/entity/armorset_summon/mars_unstable_energy_cannon.animation.json"
            );

    @Override
    public ResourceLocation getModelResource(
            UnstableEnergyCannonEntity animatable
    ) {
        return MODEL;
    }

    @Override
    public ResourceLocation getTextureResource(
            UnstableEnergyCannonEntity animatable
    ) {
        return TEXTURE;
    }

    @Override
    public ResourceLocation getAnimationResource(
            UnstableEnergyCannonEntity animatable
    ) {
        return ANIMATION;
    }
}