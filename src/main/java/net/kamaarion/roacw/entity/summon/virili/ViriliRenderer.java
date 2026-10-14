package net.kamaarion.roacw.entity.summon.virili;

import net.minecraft.client.renderer.entity.EntityRendererProvider;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

public class ViriliRenderer extends GeoEntityRenderer<ViriliEntity> {

    public ViriliRenderer(EntityRendererProvider.Context context) {
        super(context, new ViriliModel());
        this.shadowRadius = 0.6F;
        this.addRenderLayer(new ViriliGlowLayer(this));
    }
}