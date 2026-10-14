package net.kamaarion.roacw.entity.summon.baby_plaguebringer;

import software.bernie.geckolib.renderer.GeoEntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;

public class BabyPlaguebringerRenderer extends GeoEntityRenderer<BabyPlaguebringerEntity> {

    public BabyPlaguebringerRenderer(EntityRendererProvider.Context context) {
        super(context, new BabyPlaguebringerModel());
    }
}