package net.kamaarion.roacw.items.curios.burst_sheath;

import net.kamaarion.roacw.ROACW;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.DefaultedItemGeoModel;

public class BurstSheathModel extends DefaultedItemGeoModel<BurstSheath> {
    public BurstSheathModel() {
        super(ResourceLocation.fromNamespaceAndPath(ROACW.MODID, ""));
    }

    @Override
    public ResourceLocation getModelResource(BurstSheath object) {
        return ResourceLocation.fromNamespaceAndPath(ROACW.MODID, "geo/item/curio/burst_sheath.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(BurstSheath object) {
        return ResourceLocation.fromNamespaceAndPath(ROACW.MODID, "textures/item/curio/burst_sheath.png");
    }

    @Override
    public ResourceLocation getAnimationResource(BurstSheath animatable) {
        return ResourceLocation.fromNamespaceAndPath(ROACW.MODID, "animations/mars_armor.animation.json");
    }
}