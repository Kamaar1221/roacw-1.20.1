package net.kamaarion.roacw.items.curios.elemental_gauntlet;

import net.minecraft.resources.ResourceLocation;
import net.kamaarion.roacw.ROACW;
import software.bernie.geckolib.model.DefaultedItemGeoModel;

public class ElementalGauntletModel extends DefaultedItemGeoModel<ElementalGauntlet> {

    // Variable to track if the current player skin is slim
    private boolean isSlim = false;

    public ElementalGauntletModel() {
        super(ResourceLocation.fromNamespaceAndPath(ROACW.MODID, ""));
    }

    // Setter method to pass the skin status from your Curio Renderer
    public void setSlim(boolean isSlim) {
        this.isSlim = isSlim;
    }

    @Override
    public ResourceLocation getModelResource(ElementalGauntlet object) {
        // Dynamically choose the model file based on skin type
        if (this.isSlim) {
            return ResourceLocation.fromNamespaceAndPath(ROACW.MODID, "geo/item/curio/elemental_gauntlet_slim.geo.json");
        }
        return ResourceLocation.fromNamespaceAndPath(ROACW.MODID, "geo/item/curio/elemental_gauntlet.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(ElementalGauntlet object) {
        return ResourceLocation.fromNamespaceAndPath(ROACW.MODID, "textures/item/curio/elemental_gauntlet.png");
    }

    @Override
    public ResourceLocation getAnimationResource(ElementalGauntlet animatable) {
        return ResourceLocation.fromNamespaceAndPath(ROACW.MODID, "animations/mars_armor.animation.json");
    }
}
