package net.kamaarion.roacw.items.armor.fearmongerarmorset;

import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.renderer.GeoArmorRenderer;

public class FearmongerArmorRenderer extends GeoArmorRenderer<FearmongerArmorItem> {

    public FearmongerArmorRenderer(FearmongerArmorModel fearmongerArmorModel) {
        super(new FearmongerArmorModel());

        this.addRenderLayer(new FearmongerArmorLayer(this));
    }

    public RenderType getRenderType(
            FearmongerArmorItem animatable,
            ResourceLocation texture,
            MultiBufferSource bufferSource,
            float partialTick
    ) {
        return RenderType.entityTranslucent(this.getTextureLocation(animatable));
    }
}