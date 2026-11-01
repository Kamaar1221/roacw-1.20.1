package net.kamaarion.roacw.items.weapons.gemini_decimator;

import software.bernie.geckolib.renderer.GeoItemRenderer;

public class GeminiDecimatorRenderer extends GeoItemRenderer<GeminiDecimator> {

    public GeminiDecimatorRenderer() {
        super(new GeminiDecimatorModel());
        this.addRenderLayer(new GeminiDecimatorLayer(this));
    }
}