package net.kamaarion.roacw.items.weapons.phaseslayer;

import net.kamaarion.roacw.ROACW;
import net.kamaarion.roacw.items.weapons.the_burning_sky.BurningSky;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.DefaultedItemGeoModel;

public class PhaseslayerModel extends DefaultedItemGeoModel<Phaseslayer> {
    public PhaseslayerModel() {super (ResourceLocation.fromNamespaceAndPath(ROACW.MODID, ""));}

    public ResourceLocation getModelResource(Phaseslayer object) {
        return ResourceLocation.fromNamespaceAndPath(ROACW.MODID, "geo/item/weapon/phaseslayer.geo.json");
    }

    public ResourceLocation getTextureResource(Phaseslayer object) {
        return ResourceLocation.fromNamespaceAndPath(ROACW.MODID, "textures/item/weapon/phaseslayer.png");
    }

    public ResourceLocation getAnimationResource(Phaseslayer animatable) {
        return ResourceLocation.fromNamespaceAndPath("irons_spellbooks", "animations/wizard_armor_animation.json");
    }
}
