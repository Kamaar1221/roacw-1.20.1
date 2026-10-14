package net.kamaarion.roacw.entity.projectile.endo_hydra_beam;

import net.kamaarion.roacw.ROACW;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public class EndoHydraBeamModel extends GeoModel<EndoHydraBeamEntity> {

    private static final ResourceLocation BEAM_8 =
            ResourceLocation.fromNamespaceAndPath(
                    ROACW.MODID,
                    "geo/entity/projectile/endo_hydra_beam_8.geo.json"
            );

    private static final ResourceLocation BEAM_16 =
            ResourceLocation.fromNamespaceAndPath(
                    ROACW.MODID,
                    "geo/entity/projectile/endo_hydra_beam_16.geo.json"
            );

    private static final ResourceLocation BEAM_24 =
            ResourceLocation.fromNamespaceAndPath(
                    ROACW.MODID,
                    "geo/entity/projectile/endo_hydra_beam_24.geo.json"
            );

    @Override
    public ResourceLocation getModelResource(
            EndoHydraBeamEntity animatable
    ) {

        double length =
                animatable.getBeamLength();

        if (length <= 8.0D) {
            return BEAM_8;
        }

        if (length <= 16.0D) {
            return BEAM_16;
        }

        return BEAM_24;
    }

    @Override
    public ResourceLocation getTextureResource(
            EndoHydraBeamEntity animatable
    ) {

        return ResourceLocation.fromNamespaceAndPath(
                ROACW.MODID,
                "textures/entity/projectile/endo_hydra_beam.png"
        );
    }

    @Override
    public ResourceLocation getAnimationResource(
            EndoHydraBeamEntity animatable
    ) {

        return ResourceLocation.fromNamespaceAndPath(
                ROACW.MODID,
                "animations/entity/projectile/endo_hydra_beam.animation.json"
        );
    }
}