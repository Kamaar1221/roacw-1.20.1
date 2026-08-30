package net.kamaarion.roacw.items.curios.alchemical_decanter;

import net.kamaarion.roacw.ROACW;
import net.kamaarion.roacw.items.curios.evasion_scarf.EvasionScarf;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.DefaultedItemGeoModel;

public class AlchemicalDecanterScarfModel extends DefaultedItemGeoModel<AlchemicalDecanter> {
    public AlchemicalDecanterScarfModel() {
        super(ResourceLocation.fromNamespaceAndPath(ROACW.MODID, ""));
    }

    @Override
    public ResourceLocation getModelResource(AlchemicalDecanter object) {
        return ResourceLocation.fromNamespaceAndPath(ROACW.MODID, "geo/item/curio/alchemical_decanter.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(AlchemicalDecanter object) {
        return ResourceLocation.fromNamespaceAndPath(ROACW.MODID, "textures/item/curio/alchemical_decanter.png");
    }

    @Override
    public ResourceLocation getAnimationResource(AlchemicalDecanter animatable) {
        return ResourceLocation.fromNamespaceAndPath(ROACW.MODID, "animations/mars_armor.animation.json");
    }
}
