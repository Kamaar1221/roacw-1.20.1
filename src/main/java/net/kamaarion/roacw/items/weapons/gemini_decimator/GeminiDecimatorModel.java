package net.kamaarion.roacw.items.weapons.gemini_decimator;

import net.kamaarion.roacw.ROACW;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.DefaultedItemGeoModel;

public class GeminiDecimatorModel extends DefaultedItemGeoModel<GeminiDecimator> {
    public GeminiDecimatorModel() {
        super(ResourceLocation.fromNamespaceAndPath(ROACW.MODID, ""));
    }

    @Override
    public ResourceLocation getModelResource(GeminiDecimator object) {
        return ResourceLocation.fromNamespaceAndPath(ROACW.MODID, "geo/item/weapon/gemini_decimator.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(GeminiDecimator object) {
        return ResourceLocation.fromNamespaceAndPath(ROACW.MODID, "textures/item/weapon/gemini_decimator.png");
    }

    @Override
    public ResourceLocation getAnimationResource(GeminiDecimator animatable) {
        return ResourceLocation.fromNamespaceAndPath("irons_spellbooks", "animations/wizard_armor_animation.json");
    }
}