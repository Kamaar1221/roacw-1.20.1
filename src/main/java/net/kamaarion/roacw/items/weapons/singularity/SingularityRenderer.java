package net.kamaarion.roacw.items.weapons.singularity;

import software.bernie.geckolib.renderer.GeoItemRenderer;

public class SingularityRenderer extends GeoItemRenderer<Singularity> {

    public SingularityRenderer() {
        super(new SingularityModel());

         addRenderLayer(new SingularityGlowLayer(this));
    }
}