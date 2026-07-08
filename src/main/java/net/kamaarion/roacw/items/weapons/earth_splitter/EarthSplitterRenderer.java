package net.kamaarion.roacw.items.weapons.earth_splitter;

import net.kamaarion.roacw.items.weapons.murasama_blade.MurasamaBladeItem;
import net.kamaarion.roacw.items.weapons.murasama_blade.MurasamaBladeModel;
import software.bernie.geckolib.renderer.GeoItemRenderer;
import software.bernie.geckolib.renderer.layer.AutoGlowingGeoLayer;

public class EarthSplitterRenderer extends GeoItemRenderer<EarthSplitterItem> {

    public EarthSplitterRenderer()
    {
        super(new EarthSplitterModel());
    }
}
