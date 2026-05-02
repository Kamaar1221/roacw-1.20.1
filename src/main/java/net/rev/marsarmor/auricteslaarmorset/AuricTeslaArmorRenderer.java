package net.rev.marsarmor.auricteslaarmorset;

import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.rev.marsarmor.marsarmorset.MarsArmorItem;
import net.rev.marsarmor.marsarmorset.MarsArmorLayer;
import net.rev.marsarmor.marsarmorset.MarsArmorModel;
import software.bernie.geckolib.cache.object.GeoBone;
import software.bernie.geckolib.renderer.GeoArmorRenderer;

public class AuricTeslaArmorRenderer extends GeoArmorRenderer<AuricTeslaArmorItem> {
    public AuricTeslaArmorRenderer(AuricTeslaArmorModel auricTeslaArmorModel) {
        super(new AuricTeslaArmorModel());
    }
}