package net.kamaarion.roacw.entity.summon.hydra;

import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

@OnlyIn(Dist.CLIENT)
public class HydraHeadRenderer extends GeoEntityRenderer<HydraHead> {

    public HydraHeadRenderer(EntityRendererProvider.Context context) {
        super(context, new HydraHeadModel());
        this.shadowRadius = 0f;
    }
}
