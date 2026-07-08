package net.kamaarion.roacw.items.weapons.high_ruler_sword;

import net.kamaarion.roacw.items.weapons.earth_splitter.EarthSplitterItem;
import net.kamaarion.roacw.items.weapons.earth_splitter.EarthSplitterModel;
import software.bernie.geckolib.renderer.GeoItemRenderer;

public class HighRulerSwordRenderer extends GeoItemRenderer<HighRulerSwordItem> {

    public HighRulerSwordRenderer()
    {
        super(new HighRulerSwordModel());
    }
}
