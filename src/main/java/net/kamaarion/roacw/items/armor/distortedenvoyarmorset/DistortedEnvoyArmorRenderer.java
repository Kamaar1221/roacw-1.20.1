package net.kamaarion.roacw.items.armor.distortedenvoyarmorset;

import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.renderer.GeoArmorRenderer;

public class DistortedEnvoyArmorRenderer extends GeoArmorRenderer<DistortedEnvoyArmorItem> {

    public DistortedEnvoyArmorRenderer(DistortedEnvoyArmorModel model) {
        super(model);
    }

    @Override
    public RenderType getRenderType(
            DistortedEnvoyArmorItem animatable,
            ResourceLocation texture,
            MultiBufferSource bufferSource,
            float partialTick
    ) {
        return RenderType.entityTranslucent(this.getTextureLocation(animatable));
    }
}