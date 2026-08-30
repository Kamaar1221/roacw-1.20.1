package net.kamaarion.roacw.items.weapons.corvid_harbinger_staff;

import net.kamaarion.roacw.ROACW;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.DefaultedItemGeoModel;

public class CorvidHarbingerModel extends DefaultedItemGeoModel<CorvidHarbinger> {
    public CorvidHarbingerModel() {super (ResourceLocation.fromNamespaceAndPath(ROACW.MODID, ""));}

    public ResourceLocation getModelResource(CorvidHarbinger object) {
        return ResourceLocation.fromNamespaceAndPath(ROACW.MODID, "geo/item/weapon/corvid_harbinger_staff.geo.json");
    }

    public ResourceLocation getTextureResource(CorvidHarbinger object) {
        return ResourceLocation.fromNamespaceAndPath(ROACW.MODID, "textures/item/weapon/corvid_harbinger_staff.png");
    }

    public ResourceLocation getAnimationResource(CorvidHarbinger animatable) {
        return ResourceLocation.fromNamespaceAndPath("irons_spellbooks", "animations/wizard_armor_animation.json");
    }
}
