package net.kamaarion.roacw.entity.projectile.energy_cannon_beam;

import net.kamaarion.roacw.ROACW;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public class EnergyCannonBeamModel extends GeoModel<EnergyCannonBeamEntity> {

    @Override
    public ResourceLocation getModelResource(EnergyCannonBeamEntity animatable) {
        return ResourceLocation.fromNamespaceAndPath(ROACW.MODID, "geo/entity/projectile/energy_cannon_beam.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(EnergyCannonBeamEntity animatable) {
        return ResourceLocation.fromNamespaceAndPath(ROACW.MODID, "textures/entity/projectile/energy_cannon_beam.png");
    }

    @Override
    public ResourceLocation getAnimationResource(EnergyCannonBeamEntity animatable) {
        return ResourceLocation.fromNamespaceAndPath(ROACW.MODID, "animations/entity/projectile/energy_cannon_beam.animation.json");
    }
}