package net.kamaarion.roacw.items.curios.alchemical_decanter;

import net.kamaarion.roacw.items.curios.evasion_scarf.EvasionScarf;
import net.kamaarion.roacw.items.curios.evasion_scarf.EvasionScarfModel;
import software.bernie.geckolib.renderer.GeoItemRenderer;

public class AlchemicalDecanterRenderer extends GeoItemRenderer<AlchemicalDecanter> {
    public AlchemicalDecanterRenderer() {
        super(new AlchemicalDecanterScarfModel());
    }
}
