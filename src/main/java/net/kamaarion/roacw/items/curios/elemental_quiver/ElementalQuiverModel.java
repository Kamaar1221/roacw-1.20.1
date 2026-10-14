package net.kamaarion.roacw.items.curios.elemental_quiver;

import net.kamaarion.roacw.ROACW;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.DefaultedItemGeoModel;

public class ElementalQuiverModel extends DefaultedItemGeoModel<ElementalQuiver> {

    public ElementalQuiverModel() {
        super(ResourceLocation.fromNamespaceAndPath(ROACW.MODID, ""));
    }

    @Override
    public ResourceLocation getModelResource(ElementalQuiver object) {
        return ResourceLocation.fromNamespaceAndPath(
                ROACW.MODID,
                "geo/item/curio/elemental_quiver.geo.json"
        );
    }

    @Override
    public ResourceLocation getTextureResource(ElementalQuiver object) {
        return ResourceLocation.fromNamespaceAndPath(
                ROACW.MODID,
                "textures/item/curio/elemental_quiver.png"
        );
    }

    @Override
    public ResourceLocation getAnimationResource(ElementalQuiver animatable) {
        return ResourceLocation.fromNamespaceAndPath(
                ROACW.MODID,
                "animations/mars_armor.animation.json"
        );
    }
}