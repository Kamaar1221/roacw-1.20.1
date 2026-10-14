package net.kamaarion.roacw.entity.summon.mini_plaguebringer;

import net.kamaarion.roacw.ROACW;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public class MiniPlaguebringerModel extends GeoModel<MiniPlaguebringerEntity> {

    @Override
    public ResourceLocation getModelResource(MiniPlaguebringerEntity animatable) {
        return ResourceLocation.fromNamespaceAndPath(
                ROACW.MODID,
                "geo/entity/summon/mini_plaguebringer.geo.json"
        );
    }

    @Override
    public ResourceLocation getTextureResource(MiniPlaguebringerEntity animatable) {
        return ResourceLocation.fromNamespaceAndPath(
                ROACW.MODID,
                "textures/entity/summon/mini_plaguebringer.png"
        );
    }

    @Override
    public ResourceLocation getAnimationResource(MiniPlaguebringerEntity animatable) {
        return ResourceLocation.fromNamespaceAndPath(
                ROACW.MODID,
                "animations/entity/summon/mini_plaguebringer.animation.json"
        );
    }
}