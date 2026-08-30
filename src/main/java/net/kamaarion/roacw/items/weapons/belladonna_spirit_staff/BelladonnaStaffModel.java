package net.kamaarion.roacw.items.weapons.belladonna_spirit_staff;

import net.kamaarion.roacw.ROACW;
import net.kamaarion.roacw.items.weapons.earth_splitter.EarthSplitter;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.DefaultedItemGeoModel;

public class BelladonnaStaffModel extends DefaultedItemGeoModel<BelladonnaStaff> {
    public BelladonnaStaffModel() {super (ResourceLocation.fromNamespaceAndPath(ROACW.MODID, ""));}

    public ResourceLocation getModelResource(BelladonnaStaff object) {
        return ResourceLocation.fromNamespaceAndPath(ROACW.MODID, "geo/item/weapon/belladonna_spirit_staff.geo.json");
    }

    public ResourceLocation getTextureResource(BelladonnaStaff object) {
        return ResourceLocation.fromNamespaceAndPath(ROACW.MODID, "textures/item/weapon/belladonna_spirit_staff.png");
    }

    public ResourceLocation getAnimationResource(BelladonnaStaff animatable) {
        return ResourceLocation.fromNamespaceAndPath("irons_spellbooks", "animations/wizard_armor_animation.json");
    }
}
