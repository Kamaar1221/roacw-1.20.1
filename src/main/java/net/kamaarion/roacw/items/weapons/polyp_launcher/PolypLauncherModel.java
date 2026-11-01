package net.kamaarion.roacw.items.weapons.polyp_launcher;

import net.kamaarion.roacw.ROACW;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.DefaultedItemGeoModel;

public class PolypLauncherModel extends DefaultedItemGeoModel<PolypLauncher> {

    public PolypLauncherModel() {
        super(ResourceLocation.fromNamespaceAndPath(ROACW.MODID, ""));
    }

    public ResourceLocation getModelResource(PolypLauncher object) {
        return ResourceLocation.fromNamespaceAndPath(
                ROACW.MODID,
                "geo/item/weapon/polyp_launcher.geo.json"
        );
    }

    public ResourceLocation getTextureResource(PolypLauncher object) {
        return ResourceLocation.fromNamespaceAndPath(
                ROACW.MODID,
                "textures/item/weapon/polyp_launcher.png"
        );
    }

    public ResourceLocation getAnimationResource(PolypLauncher animatable) {
        return ResourceLocation.fromNamespaceAndPath(
                "irons_spellbooks",
                "animations/wizard_armor_animation.json"
        );
    }
}