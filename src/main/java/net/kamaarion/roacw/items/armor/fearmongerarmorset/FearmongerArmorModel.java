package net.kamaarion.roacw.items.armor.fearmongerarmorset;

import net.kamaarion.roacw.ROACW;
import net.kamaarion.roacw.items.armor.auricteslaarmorset.AuricTeslaArmorItem;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public class FearmongerArmorModel extends GeoModel<FearmongerArmorItem> {
    public FearmongerArmorModel() {
    }

    public ResourceLocation getModelResource(FearmongerArmorItem object) {
        return ResourceLocation.fromNamespaceAndPath(ROACW.MODID, "geo/item/armor/fearmonger_armor.geo.json");
    }

    public ResourceLocation getTextureResource(FearmongerArmorItem object) {
        return ResourceLocation.fromNamespaceAndPath(ROACW.MODID, "textures/item/armor/fearmonger_armor.png");
    }

    public ResourceLocation getAnimationResource(FearmongerArmorItem animatable) {
        return ResourceLocation.fromNamespaceAndPath(ROACW.MODID, "animations/auric_tesla_armor.animation.json");
    }
}
