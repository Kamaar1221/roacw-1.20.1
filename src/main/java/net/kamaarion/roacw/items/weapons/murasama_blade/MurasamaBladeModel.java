package net.kamaarion.roacw.items.weapons.murasama_blade;

import net.kamaarion.roacw.ROACW;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.DefaultedItemGeoModel;

public class MurasamaBladeModel extends DefaultedItemGeoModel<MurasamaBlade> {
    public MurasamaBladeModel() {super (ResourceLocation.fromNamespaceAndPath(ROACW.MODID, ""));}

    public ResourceLocation getModelResource(MurasamaBlade object) {
        return ResourceLocation.fromNamespaceAndPath(ROACW.MODID, "geo/item/weapon/murasama_blade.geo.json");
    }

    public ResourceLocation getTextureResource(MurasamaBlade object) {
        return ResourceLocation.fromNamespaceAndPath(ROACW.MODID, "textures/item/weapon/murasama_blade.png");
    }

    public ResourceLocation getAnimationResource(MurasamaBlade animatable) {
        return ResourceLocation.fromNamespaceAndPath("irons_spellbooks", "animations/wizard_armor_animation.json");
    }
}
