package net.kamaarion.roacw.items.weapons.singularity;

import net.kamaarion.roacw.ROACW;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.DefaultedItemGeoModel;

public class SingularityModel extends DefaultedItemGeoModel<Singularity> {

    public SingularityModel() {
        super(ResourceLocation.fromNamespaceAndPath(ROACW.MODID, ""));
    }

    @Override
    public ResourceLocation getModelResource(Singularity object) {
        return ResourceLocation.fromNamespaceAndPath(
                ROACW.MODID,
                "geo/item/weapon/singularity.geo.json"
        );
    }

    @Override
    public ResourceLocation getTextureResource(Singularity object) {
        return ResourceLocation.fromNamespaceAndPath(
                ROACW.MODID,
                "textures/item/weapon/singularity.png"
        );
    }

    @Override
    public ResourceLocation getAnimationResource(Singularity animatable) {
        return ResourceLocation.fromNamespaceAndPath("irons_spellbooks", "animations/wizard_armor_animation.json");
    }
}