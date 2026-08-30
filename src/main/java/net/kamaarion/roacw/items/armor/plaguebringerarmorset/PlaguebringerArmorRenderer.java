package net.kamaarion.roacw.items.armor.plaguebringerarmorset;

import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.renderer.GeoArmorRenderer;

public class PlaguebringerArmorRenderer extends GeoArmorRenderer<PlaguebringerArmorItem> {

    public PlaguebringerArmorRenderer() {
        super(new PlaguebringerArmorModel());

        this.addRenderLayer(new PlaguebringerArmorLayer(this));
    }

    public RenderType getRenderType(
            PlaguebringerArmorItem animatable,
            ResourceLocation texture,
            MultiBufferSource bufferSource,
            float partialTick
    ) {
        return RenderType.entityTranslucent(this.getTextureLocation(animatable));
    }
}