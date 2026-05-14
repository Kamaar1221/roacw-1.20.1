package net.rev.roacw.items.armor.auricteslaarmorset;

import net.minecraft.resources.ResourceLocation;
import net.rev.roacw.ROACW;
import software.bernie.geckolib.model.GeoModel;

public class AuricTeslaArmorModel extends GeoModel<AuricTeslaArmorItem> {
    public AuricTeslaArmorModel() {
    }

    public ResourceLocation getModelResource(AuricTeslaArmorItem object) {
        return ResourceLocation.fromNamespaceAndPath(ROACW.MODID, "geo/item/armor/auric_tesla_armor.geo.json");
    }

    public ResourceLocation getTextureResource(AuricTeslaArmorItem object) {
        return ResourceLocation.fromNamespaceAndPath(ROACW.MODID, "textures/item/armor/auric_tesla_armor.png");
    }

    public ResourceLocation getAnimationResource(AuricTeslaArmorItem animatable) {
        return ResourceLocation.fromNamespaceAndPath(ROACW.MODID, "animations/auric_tesla_armor.animation.json");
    }
}
