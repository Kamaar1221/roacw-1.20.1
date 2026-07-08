package net.kamaarion.roacw.items.weapons.high_ruler_sword;

import net.kamaarion.roacw.ROACW;
import net.kamaarion.roacw.items.weapons.earth_splitter.EarthSplitterItem;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.DefaultedItemGeoModel;

public class HighRulerSwordModel extends DefaultedItemGeoModel<HighRulerSwordItem> {
    public HighRulerSwordModel() {super (ResourceLocation.fromNamespaceAndPath(ROACW.MODID, ""));}

    public ResourceLocation getModelResource(HighRulerSwordItem object) {
        return ResourceLocation.fromNamespaceAndPath(ROACW.MODID, "geo/item/weapon/high_ruler_sword.geo.json");
    }

    public ResourceLocation getTextureResource(HighRulerSwordItem object) {
        return ResourceLocation.fromNamespaceAndPath(ROACW.MODID, "textures/item/weapon/high_ruler_sword.png");
    }

    public ResourceLocation getAnimationResource(HighRulerSwordItem animatable) {
        return ResourceLocation.fromNamespaceAndPath("irons_spellbooks", "animations/wizard_armor_animation.json");
    }
}
