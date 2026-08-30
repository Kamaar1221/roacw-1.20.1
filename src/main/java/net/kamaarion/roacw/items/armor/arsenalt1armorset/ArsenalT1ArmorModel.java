package net.kamaarion.roacw.items.armor.arsenalt1armorset;

import net.kamaarion.roacw.ROACW;
import net.kamaarion.roacw.items.armor.fearmongerarmorset.FearmongerArmorItem;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public class ArsenalT1ArmorModel extends GeoModel<ArsenalT1ArmorItem> {
    public ArsenalT1ArmorModel() {
    }

    public ResourceLocation getModelResource(ArsenalT1ArmorItem object) {
        return ResourceLocation.fromNamespaceAndPath(ROACW.MODID, "geo/item/armor/arsenal_t1_armor.geo.json");
    }

    public ResourceLocation getTextureResource(ArsenalT1ArmorItem object) {
        return ResourceLocation.fromNamespaceAndPath(ROACW.MODID, "textures/item/armor/arsenal_t1_armor.png");
    }

    public ResourceLocation getAnimationResource(ArsenalT1ArmorItem animatable) {
        return ResourceLocation.fromNamespaceAndPath(ROACW.MODID, "animations/auric_tesla_armor.animation.json");
    }
}
