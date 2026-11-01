package net.kamaarion.roacw.items.weapons.enchanted_conch;

import net.kamaarion.roacw.ROACW;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.DefaultedItemGeoModel;

public class EnchantedConchModel extends DefaultedItemGeoModel<EnchantedConch> {

    public EnchantedConchModel() {
        super(ResourceLocation.fromNamespaceAndPath(ROACW.MODID, ""));
    }

    public ResourceLocation getModelResource(EnchantedConch object) {
        return ResourceLocation.fromNamespaceAndPath(
                ROACW.MODID,
                "geo/item/weapon/enchanted_conch.geo.json"
        );
    }

    public ResourceLocation getTextureResource(EnchantedConch object) {
        return ResourceLocation.fromNamespaceAndPath(
                ROACW.MODID,
                "textures/item/weapon/enchanted_conch.png"
        );
    }

    public ResourceLocation getAnimationResource(EnchantedConch animatable) {
        return ResourceLocation.fromNamespaceAndPath(
                "irons_spellbooks",
                "animations/wizard_armor_animation.json"
        );
    }
}