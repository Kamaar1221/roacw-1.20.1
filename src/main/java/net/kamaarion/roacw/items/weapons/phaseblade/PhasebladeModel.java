package net.kamaarion.roacw.items.weapons.phaseblade;

import net.kamaarion.roacw.ROACW;
import net.kamaarion.roacw.items.weapons.phaseslayer.Phaseslayer;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.DefaultedItemGeoModel;

public class PhasebladeModel extends DefaultedItemGeoModel<Phaseblade> {
    public PhasebladeModel() {super (ResourceLocation.fromNamespaceAndPath(ROACW.MODID, ""));}

    public ResourceLocation getModelResource(Phaseblade object) {
        return ResourceLocation.fromNamespaceAndPath(ROACW.MODID, "geo/item/weapon/phaseblade.geo.json");
    }

    public ResourceLocation getTextureResource(Phaseblade object) {
        return ResourceLocation.fromNamespaceAndPath(ROACW.MODID, "textures/item/weapon/phaseblade.png");
    }

    public ResourceLocation getAnimationResource(Phaseblade animatable) {
        return ResourceLocation.fromNamespaceAndPath("irons_spellbooks", "animations/wizard_armor_animation.json");
    }
}
