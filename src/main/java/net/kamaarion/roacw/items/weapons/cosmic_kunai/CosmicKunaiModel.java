package net.kamaarion.roacw.items.weapons.cosmic_kunai;

import net.kamaarion.roacw.ROACW;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.DefaultedItemGeoModel;

public class CosmicKunaiModel extends DefaultedItemGeoModel<CosmicKunai> {

    public CosmicKunaiModel() {
        super(ResourceLocation.fromNamespaceAndPath(ROACW.MODID, ""));
    }

    public ResourceLocation getModelResource(CosmicKunai object) {
        return ResourceLocation.fromNamespaceAndPath(
                ROACW.MODID,
                "geo/item/weapon/cosmic_kunai.geo.json"
        );
    }

    public ResourceLocation getTextureResource(CosmicKunai object) {
        return ResourceLocation.fromNamespaceAndPath(
                ROACW.MODID,
                "textures/item/weapon/cosmic_kunai.png"
        );
    }

    public ResourceLocation getAnimationResource(CosmicKunai animatable) {
        return ResourceLocation.fromNamespaceAndPath(
                "irons_spellbooks",
                "animations/wizard_armor_animation.json"
        );
    }
}