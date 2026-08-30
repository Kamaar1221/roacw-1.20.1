package net.kamaarion.roacw.items.weapons.phaseblade;

import software.bernie.geckolib.renderer.GeoItemRenderer;

public class PhasebladeRenderer extends GeoItemRenderer<Phaseblade> {

    public PhasebladeRenderer() {
        super(new PhasebladeModel());
        this.addRenderLayer(new PhasebladeLayer(this));
    }
}