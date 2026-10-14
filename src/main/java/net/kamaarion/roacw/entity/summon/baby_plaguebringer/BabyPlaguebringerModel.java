package net.kamaarion.roacw.entity.summon.baby_plaguebringer;

import net.kamaarion.roacw.ROACW;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public class BabyPlaguebringerModel extends GeoModel<BabyPlaguebringerEntity> {

    @Override
    public ResourceLocation getModelResource(BabyPlaguebringerEntity animatable) {
        return ResourceLocation.fromNamespaceAndPath(
                ROACW.MODID,
                "geo/entity/summon/baby_plaguebringer.geo.json"
        );
    }

    @Override
    public ResourceLocation getTextureResource(BabyPlaguebringerEntity animatable) {
        return ResourceLocation.fromNamespaceAndPath(
                ROACW.MODID,
                "textures/entity/summon/baby_plaguebringer.png"
        );
    }

    @Override
    public ResourceLocation getAnimationResource(BabyPlaguebringerEntity animatable) {
        return ResourceLocation.fromNamespaceAndPath(
                ROACW.MODID,
                "animations/entity/summon/baby_plaguebringer.animation.json"
        );
    }
}