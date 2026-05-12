package net.rev.roacw.marsarmorset;

import net.minecraft.resources.ResourceLocation;
import net.rev.roacw.ROACW;
import software.bernie.geckolib.model.GeoModel;

public class MarsArmorModel extends GeoModel<MarsArmorItem> {
    public MarsArmorModel() {
    }

    public ResourceLocation getModelResource(MarsArmorItem object) {
        return ResourceLocation.fromNamespaceAndPath(ROACW.MODID, "geo/item/armor/mars_armor.geo.json");
    }

    public ResourceLocation getTextureResource(MarsArmorItem object) {
        return ResourceLocation.fromNamespaceAndPath(ROACW.MODID, "textures/item/armor/mars_armor.png");
    }

    public ResourceLocation getAnimationResource(MarsArmorItem animatable) {
        return ResourceLocation.fromNamespaceAndPath(ROACW.MODID, "animations/mars_armor.animation.json");
    }
}
