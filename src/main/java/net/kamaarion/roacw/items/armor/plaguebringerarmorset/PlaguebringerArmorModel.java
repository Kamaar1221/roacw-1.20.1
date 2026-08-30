package net.kamaarion.roacw.items.armor.plaguebringerarmorset;

import net.kamaarion.roacw.ROACW;
import net.kamaarion.roacw.items.armor.earthenpaladinset.EarthenPaladinArmorItem;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public class PlaguebringerArmorModel extends GeoModel<PlaguebringerArmorItem> {
    public PlaguebringerArmorModel() {
    }

    public ResourceLocation getModelResource(PlaguebringerArmorItem object) {
        return ResourceLocation.fromNamespaceAndPath(ROACW.MODID, "geo/item/armor/plaguebringer_armor.geo.json");
    }

    public ResourceLocation getTextureResource(PlaguebringerArmorItem object) {
        return ResourceLocation.fromNamespaceAndPath(ROACW.MODID, "textures/item/armor/plaguebringer_armor.png");
    }

    public ResourceLocation getAnimationResource(PlaguebringerArmorItem animatable) {
        return ResourceLocation.fromNamespaceAndPath(ROACW.MODID, "animations/auric_tesla_armor.animation.json");
    }
}
