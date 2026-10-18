package net.kamaarion.roacw.items.weapons.phaseslayer;

import software.bernie.geckolib.renderer.GeoItemRenderer;

public class PhaseslayerRenderer extends GeoItemRenderer<Phaseslayer> {

    public PhaseslayerRenderer() {
        super(new PhaseslayerModel());
        this.addRenderLayer(new PhaseslayerLayer(this));
    }
}