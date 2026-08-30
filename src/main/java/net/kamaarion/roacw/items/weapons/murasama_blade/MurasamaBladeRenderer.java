package net.kamaarion.roacw.items.weapons.murasama_blade;

import software.bernie.geckolib.renderer.GeoItemRenderer;
import software.bernie.geckolib.renderer.layer.AutoGlowingGeoLayer;

public class MurasamaBladeRenderer extends GeoItemRenderer<MurasamaBlade> {

    public MurasamaBladeRenderer ()
    {
        super(new MurasamaBladeModel());
        this.addRenderLayer(new AutoGlowingGeoLayer<>(this));
    }
}
