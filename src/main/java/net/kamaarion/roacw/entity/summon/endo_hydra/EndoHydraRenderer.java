package net.kamaarion.roacw.entity.summon.endo_hydra;

import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

public class EndoHydraRenderer
        extends GeoEntityRenderer<EndoHydraEntity> {

    public EndoHydraRenderer(
            EntityRendererProvider.Context context
    ) {
        super(
                context,
                new EndoHydraModel()
        );

        this.shadowRadius = 1.0F;

        this.addRenderLayer(
                new EndoHydraGlowLayer(this)
        );
    }

    @Override
    public boolean shouldRender(
            EndoHydraEntity entity,
            Frustum frustum,
            double camX,
            double camY,
            double camZ
    ) {

        return true;
    }
}