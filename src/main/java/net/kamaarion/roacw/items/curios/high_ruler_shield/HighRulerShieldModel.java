package net.kamaarion.roacw.items.curios.high_ruler_shield;

import net.kamaarion.roacw.ROACW;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.DefaultedItemGeoModel;

public class HighRulerShieldModel extends DefaultedItemGeoModel<HighRulerShield> {
    public HighRulerShieldModel() {
        super(ResourceLocation.fromNamespaceAndPath(ROACW.MODID,"high_ruler_shield"));
    }

    @Override
    public ResourceLocation getModelResource(HighRulerShield object) {
        return ResourceLocation.fromNamespaceAndPath(ROACW.MODID, "geo/item/curio/high_ruler_shield.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(HighRulerShield object) {
        return ResourceLocation.fromNamespaceAndPath(ROACW.MODID, "textures/item/curio/high_ruler_shield.png");
    }

    @Override
    public ResourceLocation getAnimationResource(HighRulerShield animatable) {
        // Note: Ensure this file path matches your intended asset location
        return ResourceLocation.fromNamespaceAndPath(ROACW.MODID, "animations/mars_armor.animation.json");
    }
}
