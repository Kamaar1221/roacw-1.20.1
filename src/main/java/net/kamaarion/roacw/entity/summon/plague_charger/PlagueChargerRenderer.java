package net.kamaarion.roacw.entity.summon.plague_charger;

import net.kamaarion.roacw.client.entity.summon.PlagueChargerModel;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

public class PlagueChargerRenderer extends GeoEntityRenderer<PlagueChargerEntity> {

    public PlagueChargerRenderer(EntityRendererProvider.Context context) {
        super(context, new PlagueChargerModel());
        this.shadowRadius = 0.6F; // TODO tune against actual model size
    }
}