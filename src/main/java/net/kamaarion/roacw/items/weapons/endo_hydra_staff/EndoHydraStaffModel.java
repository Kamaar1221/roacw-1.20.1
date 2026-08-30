package net.kamaarion.roacw.items.weapons.endo_hydra_staff;

import net.kamaarion.roacw.ROACW;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.DefaultedItemGeoModel;

public class EndoHydraStaffModel extends DefaultedItemGeoModel<EndoHydraStaff> {
    public EndoHydraStaffModel() {super (ResourceLocation.fromNamespaceAndPath(ROACW.MODID, ""));}

    public ResourceLocation getModelResource(EndoHydraStaff object) {
        return ResourceLocation.fromNamespaceAndPath(ROACW.MODID, "geo/item/weapon/endo_hydra_staff.geo.json");
    }

    public ResourceLocation getTextureResource(EndoHydraStaff object) {
        return ResourceLocation.fromNamespaceAndPath(ROACW.MODID, "textures/item/weapon/endo_hydra_staff.png");
    }

    public ResourceLocation getAnimationResource(EndoHydraStaff animatable) {
        return ResourceLocation.fromNamespaceAndPath("irons_spellbooks", "animations/wizard_armor_animation.json");
    }
}
