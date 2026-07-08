package net.kamaarion.roacw.items.armor.earthenpaladinset;

import net.kamaarion.roacw.ROACW;
import net.kamaarion.roacw.items.armor.fearmongerarmorset.FearmongerArmorItem;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public class EarthenPaladinArmorModel extends GeoModel<EarthenPaladinArmorItem> {
    public EarthenPaladinArmorModel() {
    }

    public ResourceLocation getModelResource(EarthenPaladinArmorItem object) {
        return ResourceLocation.fromNamespaceAndPath(ROACW.MODID, "geo/item/armor/earthen_paladin_armor.geo.json");
    }

    public ResourceLocation getTextureResource(EarthenPaladinArmorItem object) {
        return ResourceLocation.fromNamespaceAndPath(ROACW.MODID, "textures/item/armor/earthen_paladin_armor.png");
    }

    public ResourceLocation getAnimationResource(EarthenPaladinArmorItem animatable) {
        return ResourceLocation.fromNamespaceAndPath(ROACW.MODID, "animations/auric_tesla_armor.animation.json");
    }
}
