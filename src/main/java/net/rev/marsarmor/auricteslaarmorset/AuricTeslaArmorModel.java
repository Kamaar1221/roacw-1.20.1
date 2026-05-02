package net.rev.marsarmor.auricteslaarmorset;

import net.minecraft.resources.ResourceLocation;
import net.rev.marsarmor.MarsArmor;
import net.rev.marsarmor.marsarmorset.MarsArmorItem;
import software.bernie.geckolib.model.GeoModel;

public class AuricTeslaArmorModel extends GeoModel<AuricTeslaArmorItem> {
    public AuricTeslaArmorModel() {
    }

    public ResourceLocation getModelResource(AuricTeslaArmorItem object) {
        return ResourceLocation.fromNamespaceAndPath(MarsArmor.MODID, "geo/item/armor/auric_tesla_armor.geo.json");
    }

    public ResourceLocation getTextureResource(AuricTeslaArmorItem object) {
        return ResourceLocation.fromNamespaceAndPath(MarsArmor.MODID, "textures/item/armor/auric_tesla_armor.png");
    }

    public ResourceLocation getAnimationResource(AuricTeslaArmorItem animatable) {
        return ResourceLocation.fromNamespaceAndPath(MarsArmor.MODID, "animations/auric_tesla_armor.animation.json");
    }
}
