package net.kamaarion.roacw.items.armor.arsenalt1armorset;

import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.renderer.GeoArmorRenderer;

public class ArsenalT1ArmorRenderer extends GeoArmorRenderer<ArsenalT1ArmorItem> {

    public ArsenalT1ArmorRenderer(ArsenalT1ArmorModel arsenalArmorModel) {
        super(new ArsenalT1ArmorModel());

        this.addRenderLayer(new ArsenalT1ArmorLayer(this));
    }

    public RenderType getRenderType(
            ArsenalT1ArmorItem animatable,
            ResourceLocation texture,
            MultiBufferSource bufferSource,
            float partialTick
    ) {
        return RenderType.entityTranslucent(this.getTextureLocation(animatable));
    }
}