package net.rev.roacw.items.curios.stasis_curse;

import net.minecraft.resources.ResourceLocation;
import net.rev.roacw.ROACW;
import software.bernie.geckolib.model.DefaultedItemGeoModel;

public class StatisCurseModel extends DefaultedItemGeoModel<StatisCurse> {
    public StatisCurseModel() {
        super(ResourceLocation.fromNamespaceAndPath(ROACW.MODID, ""));
    }

    @Override
    public ResourceLocation getModelResource(StatisCurse object) {
        return ResourceLocation.fromNamespaceAndPath(ROACW.MODID, "geo/item/curio/statis_curse.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(StatisCurse object) {
        return ResourceLocation.fromNamespaceAndPath(ROACW.MODID, "textures/item/curio/statis_curse.png");
    }

    @Override
    public ResourceLocation getAnimationResource(StatisCurse animatable) {
        return ResourceLocation.fromNamespaceAndPath(ROACW.MODID, "animations/mars_armor.animation.json");
    }
}
