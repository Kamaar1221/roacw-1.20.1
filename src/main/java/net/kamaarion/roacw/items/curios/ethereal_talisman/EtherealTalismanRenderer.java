package net.kamaarion.roacw.items.curios.ethereal_talisman;

import software.bernie.geckolib.renderer.GeoItemRenderer;

public class EtherealTalismanRenderer extends GeoItemRenderer<EtherealTalisman> {

    public EtherealTalismanRenderer() {
        super(new EtherealTalismanModel());

        addRenderLayer(new EtherealTalismanGlowLayer(this));
    }
}