package net.kamaarion.roacw.items.armor.earthenpaladinset;

import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.renderer.GeoArmorRenderer;

public class EarthenPaladinArmorRenderer extends GeoArmorRenderer<EarthenPaladinArmorItem> {

    public EarthenPaladinArmorRenderer(EarthenPaladinArmorModel earthenPaladinArmorModel) {
        super(new EarthenPaladinArmorModel());

        this.addRenderLayer(new EarthenPaladinArmorLayer(this));
    }

    public RenderType getRenderType(
            EarthenPaladinArmorItem animatable,
            ResourceLocation texture,
            MultiBufferSource bufferSource,
            float partialTick
    ) {
        return RenderType.entityTranslucent(this.getTextureLocation(animatable));
    }
}