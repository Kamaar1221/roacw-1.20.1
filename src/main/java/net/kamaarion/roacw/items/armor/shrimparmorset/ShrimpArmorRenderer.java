package net.kamaarion.roacw.items.armor.shrimparmorset;

import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.renderer.GeoArmorRenderer;

public class ShrimpArmorRenderer extends GeoArmorRenderer<ShrimpArmorItem> {

    public ShrimpArmorRenderer(ShrimpArmorModel model) {
        super(model);
    }

    @Override
    public RenderType getRenderType(
            ShrimpArmorItem animatable,
            ResourceLocation texture,
            MultiBufferSource bufferSource,
            float partialTick
    ) {
        return RenderType.entityTranslucent(this.getTextureLocation(animatable));
    }
}