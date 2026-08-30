package net.kamaarion.roacw.items.armor.auricteslaarmorset;

import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.renderer.GeoArmorRenderer;

public class AuricTeslaArmorRenderer extends GeoArmorRenderer<AuricTeslaArmorItem> {

    public AuricTeslaArmorRenderer(AuricTeslaArmorModel auricTeslaArmorModel) {
        super(new AuricTeslaArmorModel());

        this.addRenderLayer(new AuricTeslaArmorLayer(this));
    }

    public RenderType getRenderType(
            AuricTeslaArmorItem animatable,
            ResourceLocation texture,
            MultiBufferSource bufferSource,
            float partialTick
    ) {
        return RenderType.entityTranslucent(this.getTextureLocation(animatable));
    }
}