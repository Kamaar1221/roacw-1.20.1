package net.kamaarion.roacw.items.weapons.endo_hydra_staff;

import software.bernie.geckolib.renderer.GeoItemRenderer;

public class EndoHydraStaffRenderer extends GeoItemRenderer<EndoHydraStaff> {

    public EndoHydraStaffRenderer() {
        super(new EndoHydraStaffModel());
        this.addRenderLayer(new EndoHydraStaffLayer(this));
    }
}