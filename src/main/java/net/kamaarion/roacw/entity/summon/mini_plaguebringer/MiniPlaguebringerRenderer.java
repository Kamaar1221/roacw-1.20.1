package net.kamaarion.roacw.entity.summon.mini_plaguebringer;

import net.minecraft.client.renderer.entity.EntityRendererProvider;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

public class MiniPlaguebringerRenderer
        extends GeoEntityRenderer<MiniPlaguebringerEntity> {

    public MiniPlaguebringerRenderer(
            EntityRendererProvider.Context context
    ) {
        super(
                context,
                new MiniPlaguebringerModel()
        );

        this.addRenderLayer(
                new MiniPlaguebringerGlowLayer(this)
        );
    }
}