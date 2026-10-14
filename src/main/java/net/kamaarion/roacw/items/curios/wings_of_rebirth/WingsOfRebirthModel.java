package net.kamaarion.roacw.items.curios.wings_of_rebirth;

import net.kamaarion.roacw.ROACW;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.DefaultedItemGeoModel;

public class WingsOfRebirthModel extends DefaultedItemGeoModel<WingsOfRebirth> {
    // Shared with WingsOfRebirthWornModel
    static final ResourceLocation MODEL =
            ResourceLocation.fromNamespaceAndPath(ROACW.MODID, "geo/item/curio/wings_of_rebirth.geo.json");
    static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath(ROACW.MODID, "textures/item/curio/wings_of_rebirth.png");
    static final ResourceLocation ANIMATION =
            ResourceLocation.fromNamespaceAndPath(ROACW.MODID, "animations/item/curio/wings_of_rebirth.animation.json");

    public WingsOfRebirthModel() {
        super(ResourceLocation.fromNamespaceAndPath(ROACW.MODID, ""));
    }

    @Override
    public ResourceLocation getModelResource(WingsOfRebirth object) {
        return MODEL;
    }

    @Override
    public ResourceLocation getTextureResource(WingsOfRebirth object) {
        return TEXTURE;
    }

    @Override
    public ResourceLocation getAnimationResource(WingsOfRebirth animatable) {
        return ANIMATION;
    }
}