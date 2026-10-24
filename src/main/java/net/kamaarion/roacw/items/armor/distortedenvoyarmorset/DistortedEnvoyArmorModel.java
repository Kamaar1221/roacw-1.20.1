package net.kamaarion.roacw.items.armor.distortedenvoyarmorset;

import net.kamaarion.roacw.ROACW;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public class DistortedEnvoyArmorModel extends GeoModel<DistortedEnvoyArmorItem> {

    @Override
    public ResourceLocation getModelResource(DistortedEnvoyArmorItem object) {
        return ResourceLocation.fromNamespaceAndPath(
                ROACW.MODID,
                "geo/item/armor/distorted_envoy_armor.geo.json"
        );
    }

    @Override
    public ResourceLocation getTextureResource(DistortedEnvoyArmorItem object) {
        return ResourceLocation.fromNamespaceAndPath(
                ROACW.MODID,
                "textures/item/armor/distorted_envoy_armor.png"
        );
    }

    @Override
    public ResourceLocation getAnimationResource(DistortedEnvoyArmorItem animatable) {
        return ResourceLocation.fromNamespaceAndPath(
                ROACW.MODID,
                "animations/item/armor/distorted_envoy_armor.animation.json"
        );
    }
}