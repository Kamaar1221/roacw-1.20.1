package net.rev.roacw.stasiscurse;

import net.minecraft.resources.ResourceLocation;
import net.rev.roacw.ROACW;
import software.bernie.geckolib.model.GeoModel;

public class StatisCurseModel extends GeoModel<StatisCurseItem> {
    public StatisCurseModel() {
    }

    public ResourceLocation getModelResource(StatisCurseItem object) {
        return ResourceLocation.fromNamespaceAndPath(ROACW.MODID, "geo/item/curio/statis_curse.geo.json");
    }

    public ResourceLocation getTextureResource(StatisCurseItem object) {
        return ResourceLocation.fromNamespaceAndPath(ROACW.MODID, "textures/item/curio/statis_curse.png");
    }

    public ResourceLocation getAnimationResource(StatisCurseItem animatable) {
        return ResourceLocation.fromNamespaceAndPath(ROACW.MODID, "animations/mars_armor.animation.json");
    }
}
