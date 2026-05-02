package net.rev.marsarmor.marsarmorset;

import net.minecraft.resources.ResourceLocation;
import net.rev.marsarmor.MarsArmor;
import software.bernie.geckolib.model.GeoModel;

public class MarsArmorModel extends GeoModel<MarsArmorItem> {
    public MarsArmorModel() {
    }

    public ResourceLocation getModelResource(MarsArmorItem object) {
        return ResourceLocation.fromNamespaceAndPath(MarsArmor.MODID, "geo/item/armor/mars_armor.geo.json");
    }

    public ResourceLocation getTextureResource(MarsArmorItem object) {
        return ResourceLocation.fromNamespaceAndPath(MarsArmor.MODID, "textures/item/armor/mars_armor.png");
    }

    public ResourceLocation getAnimationResource(MarsArmorItem animatable) {
        return ResourceLocation.fromNamespaceAndPath(MarsArmor.MODID, "animations/mars_armor.animation.json");
    }
}
