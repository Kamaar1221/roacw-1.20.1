package net.kamaarion.roacw.entity.renderer;

import net.kamaarion.roacw.entity.spells.earthly_virtue.EarthlyVirtueAoE;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.resources.ResourceLocation;

public class EarthlyVirtueAoERenderer
        extends EntityRenderer<EarthlyVirtueAoE> {

    public EarthlyVirtueAoERenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public ResourceLocation getTextureLocation(EarthlyVirtueAoE entity) {
        return TextureAtlas.LOCATION_BLOCKS;
    }

    @Override
    public boolean shouldRender(
            EarthlyVirtueAoE entity,
            Frustum frustum,
            double x,
            double y,
            double z
    ) {
        return false;
    }
}