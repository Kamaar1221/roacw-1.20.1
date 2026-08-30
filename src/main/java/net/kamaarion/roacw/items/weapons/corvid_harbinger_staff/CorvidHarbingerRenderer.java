package net.kamaarion.roacw.items.weapons.corvid_harbinger_staff;

import software.bernie.geckolib.renderer.GeoItemRenderer;

public class CorvidHarbingerRenderer extends GeoItemRenderer<CorvidHarbinger> {

    public CorvidHarbingerRenderer() {
        super(new CorvidHarbingerModel());
        this.addRenderLayer(new CorvidHarbingerLayer(this));
    }
}