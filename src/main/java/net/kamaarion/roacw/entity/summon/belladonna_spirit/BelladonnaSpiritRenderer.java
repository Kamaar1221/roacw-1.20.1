package net.kamaarion.roacw.entity.summon.belladonna_spirit;

import net.kamaarion.roacw.client.entity.summon.BelladonnaSpiritModel;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

public class BelladonnaSpiritRenderer extends GeoEntityRenderer<BelladonnaSpiritEntity> {

    public BelladonnaSpiritRenderer(EntityRendererProvider.Context context) {
        super(context, new BelladonnaSpiritModel());
        this.shadowRadius = 0.4F;
    }
}