package net.kamaarion.roacw.items.armor.shrimparmorset;

import net.kamaarion.roacw.ROACW;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public class ShrimpArmorModel extends GeoModel<ShrimpArmorItem> {

    @Override
    public ResourceLocation getModelResource(ShrimpArmorItem object) {
        return ResourceLocation.fromNamespaceAndPath(
                ROACW.MODID,
                "geo/item/armor/shrimp_armor.geo.json"
        );
    }

    @Override
    public ResourceLocation getTextureResource(ShrimpArmorItem object) {
        return ResourceLocation.fromNamespaceAndPath(
                ROACW.MODID,
                "textures/item/armor/shrimp_armor.png"
        );
    }

    @Override
    public ResourceLocation getAnimationResource(ShrimpArmorItem animatable) {
        return ResourceLocation.fromNamespaceAndPath(
                ROACW.MODID,
                "animations/auric_tesla_armor.animation.json"
        );
    }
}