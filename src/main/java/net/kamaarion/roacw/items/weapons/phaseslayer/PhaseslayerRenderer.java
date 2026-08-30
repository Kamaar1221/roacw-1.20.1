package net.kamaarion.roacw.items.weapons.phaseslayer;

import net.kamaarion.roacw.items.weapons.the_burning_sky.BurningSky;
import net.kamaarion.roacw.items.weapons.the_burning_sky.BurningSkyModel;
import software.bernie.geckolib.renderer.GeoItemRenderer;

public class PhaseslayerRenderer extends GeoItemRenderer<Phaseslayer> {

    public PhaseslayerRenderer()
    {
        super(new PhaseslayerModel());
    }
}
