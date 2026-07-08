package net.kamaarion.roacw.items.weapons.earth_splitter;

import net.kamaarion.roacw.ROACW;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.DefaultedItemGeoModel;

public class EarthSplitterModel extends DefaultedItemGeoModel<EarthSplitterItem> {
    public EarthSplitterModel() {super (ResourceLocation.fromNamespaceAndPath(ROACW.MODID, ""));}

    public ResourceLocation getModelResource(EarthSplitterItem object) {
        return ResourceLocation.fromNamespaceAndPath(ROACW.MODID, "geo/item/weapon/earth_splitter.geo.json");
    }

    public ResourceLocation getTextureResource(EarthSplitterItem object) {
        return ResourceLocation.fromNamespaceAndPath(ROACW.MODID, "textures/item/weapon/earth_splitter.png");
    }

    public ResourceLocation getAnimationResource(EarthSplitterItem animatable) {
        return ResourceLocation.fromNamespaceAndPath("irons_spellbooks", "animations/wizard_armor_animation.json");
    }
}
