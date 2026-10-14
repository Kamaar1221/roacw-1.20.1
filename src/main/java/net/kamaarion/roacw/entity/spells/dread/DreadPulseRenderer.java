package net.kamaarion.roacw.entity.spells.dread;

import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.resources.ResourceLocation;

public class DreadPulseRenderer
        extends EntityRenderer<DreadPulse> {

    public DreadPulseRenderer(
            EntityRendererProvider.Context context
    ) {
        super(context);
    }

    @Override
    public ResourceLocation getTextureLocation(
            DreadPulse entity
    ) {
        return TextureAtlas.LOCATION_BLOCKS;
    }

    @Override
    public boolean shouldRender(
            DreadPulse entity,
            Frustum frustum,
            double x,
            double y,
            double z
    ) {
        return false;
    }
}