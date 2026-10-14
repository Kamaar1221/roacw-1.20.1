package net.kamaarion.roacw.items.curios.eldritch_soul_artifact;

import net.kamaarion.roacw.ROACW;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.DefaultedItemGeoModel;

public class EldritchSoulArtifactModel extends DefaultedItemGeoModel<EldritchSoulArtifact> {

    public EldritchSoulArtifactModel() {
        super(ResourceLocation.fromNamespaceAndPath(ROACW.MODID, ""));
    }

    @Override
    public ResourceLocation getModelResource(EldritchSoulArtifact object) {
        return ResourceLocation.fromNamespaceAndPath(
                ROACW.MODID,
                "geo/item/curio/eldritch_soul_artifact.geo.json"
        );
    }

    @Override
    public ResourceLocation getTextureResource(EldritchSoulArtifact object) {
        return ResourceLocation.fromNamespaceAndPath(
                ROACW.MODID,
                "textures/item/curio/eldritch_soul_artifact.png"
        );
    }

    @Override
    public ResourceLocation getAnimationResource(EldritchSoulArtifact animatable) {
        return ResourceLocation.fromNamespaceAndPath(
                ROACW.MODID,
                "animations/mars_armor.animation.json"
        );
    }
}