package net.kamaarion.roacw.items.weapons.the_hive;

import net.kamaarion.roacw.ROACW;
import net.kamaarion.roacw.items.weapons.earth_splitter.EarthSplitter;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.DefaultedItemGeoModel;

public class TheHiveModel extends DefaultedItemGeoModel<TheHive> {
    public TheHiveModel() {super (ResourceLocation.fromNamespaceAndPath(ROACW.MODID, ""));}

    public ResourceLocation getModelResource(TheHive object) {
        return ResourceLocation.fromNamespaceAndPath(ROACW.MODID, "geo/item/weapon/the_hive.geo.json");
    }

    public ResourceLocation getTextureResource(TheHive object) {
        return ResourceLocation.fromNamespaceAndPath(ROACW.MODID, "textures/item/weapon/the_hive.png");
    }

    public ResourceLocation getAnimationResource(TheHive animatable) {
        return ResourceLocation.fromNamespaceAndPath("irons_spellbooks", "animations/wizard_armor_animation.json");
    }
}
