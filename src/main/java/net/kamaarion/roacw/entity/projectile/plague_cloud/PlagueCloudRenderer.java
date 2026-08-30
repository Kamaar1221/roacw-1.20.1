package net.kamaarion.roacw.entity.projectile.plague_cloud;

import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;

public class PlagueCloudRenderer extends EntityRenderer<PlagueCloudEntity> {

    public PlagueCloudRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public ResourceLocation getTextureLocation(PlagueCloudEntity entity) {
        return null;
    }

}