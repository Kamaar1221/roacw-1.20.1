package net.kamaarion.roacw.items.weapons.defiled_greatsword;

import net.kamaarion.roacw.ROACW;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.DefaultedItemGeoModel;

public class DefiledGreatswordModel extends DefaultedItemGeoModel<DefiledGreatsword> {

    public DefiledGreatswordModel() {
        super(ResourceLocation.fromNamespaceAndPath(ROACW.MODID, ""));
    }

    public ResourceLocation getModelResource(DefiledGreatsword object) {
        return ResourceLocation.fromNamespaceAndPath(
                ROACW.MODID,
                "geo/item/weapon/defiled_greatsword.geo.json"
        );
    }

    public ResourceLocation getTextureResource(DefiledGreatsword object) {
        return ResourceLocation.fromNamespaceAndPath(
                ROACW.MODID,
                "textures/item/weapon/defiled_greatsword.png"
        );
    }

    public ResourceLocation getAnimationResource(DefiledGreatsword animatable) {
        return ResourceLocation.fromNamespaceAndPath(
                "irons_spellbooks",
                "animations/wizard_armor_animation.json"
        );
    }
}