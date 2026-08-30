package net.kamaarion.roacw.items.weapons.the_burning_sky;

import net.kamaarion.roacw.ROACW;
import net.kamaarion.roacw.items.weapons.earth_splitter.EarthSplitter;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.DefaultedItemGeoModel;

public class BurningSkyModel extends DefaultedItemGeoModel<BurningSky> {
    public BurningSkyModel() {super (ResourceLocation.fromNamespaceAndPath(ROACW.MODID, ""));}

    public ResourceLocation getModelResource(BurningSky object) {
        return ResourceLocation.fromNamespaceAndPath(ROACW.MODID, "geo/item/weapon/the_burning_sky.geo.json");
    }

    public ResourceLocation getTextureResource(BurningSky object) {
        return ResourceLocation.fromNamespaceAndPath(ROACW.MODID, "textures/item/weapon/the_burning_sky.png");
    }

    public ResourceLocation getAnimationResource(BurningSky animatable) {
        return ResourceLocation.fromNamespaceAndPath("irons_spellbooks", "animations/wizard_armor_animation.json");
    }
}
