package net.kamaarion.roacw.entity.mob.plague_charger;

import net.minecraft.client.renderer.entity.EntityRendererProvider;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

public class PlagueChargerRenderer extends GeoEntityRenderer<PlagueChargerEntity> {

    public PlagueChargerRenderer(EntityRendererProvider.Context context) {
        super(context, new PlagueChargerModel());
        this.shadowRadius = 0.6F;
    }
}