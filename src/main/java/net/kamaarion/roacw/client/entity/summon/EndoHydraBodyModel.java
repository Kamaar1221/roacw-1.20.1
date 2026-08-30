package net.kamaarion.roacw.client.entity.summon;

import net.kamaarion.roacw.ROACW;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public class EndoHydraBodyModel
        extends GeoModel<EndoHydraEntity> {

    @Override
    public ResourceLocation getModelResource(
            EndoHydraEntity animatable
    ) {
        return ResourceLocation.fromNamespaceAndPath(
                ROACW.MODID,
                "geo/entity/summon/endo_hydra_body.geo.json"
        );
    }

    @Override
    public ResourceLocation getTextureResource(
            EndoHydraEntity animatable
    ) {
        return ResourceLocation.fromNamespaceAndPath(
                ROACW.MODID,
                "textures/entity/summon/endo_hydra_body.png"
        );
    }

    @Override
    public ResourceLocation getAnimationResource(
            EndoHydraEntity animatable
    ) {
        return ResourceLocation.fromNamespaceAndPath(
                ROACW.MODID,
                "animations/entity/endo_hydra_body.animation.json"
        );
    }
}

