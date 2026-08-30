package net.kamaarion.roacw.items.weapons.the_burning_sky;

import net.kamaarion.roacw.items.weapons.earth_splitter.EarthSplitter;
import net.kamaarion.roacw.items.weapons.earth_splitter.EarthSplitterModel;
import software.bernie.geckolib.renderer.GeoItemRenderer;

public class BurningSkyRenderer extends GeoItemRenderer<BurningSky> {

    public BurningSkyRenderer()
    {
        super(new BurningSkyModel());
    }
}
