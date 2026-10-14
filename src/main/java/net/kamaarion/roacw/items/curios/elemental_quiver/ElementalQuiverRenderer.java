package net.kamaarion.roacw.items.curios.elemental_quiver;

import software.bernie.geckolib.renderer.GeoItemRenderer;

public class ElementalQuiverRenderer extends GeoItemRenderer<ElementalQuiver> {

    public ElementalQuiverRenderer() {
        super(new ElementalQuiverModel());

        addRenderLayer(new ElementalQuiverGlowLayer(this));
    }
}