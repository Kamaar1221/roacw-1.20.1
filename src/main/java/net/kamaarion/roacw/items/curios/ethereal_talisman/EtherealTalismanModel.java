package net.kamaarion.roacw.items.curios.ethereal_talisman;

import net.kamaarion.roacw.ROACW;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.DefaultedItemGeoModel;

public class EtherealTalismanModel extends DefaultedItemGeoModel<EtherealTalisman> {

    public EtherealTalismanModel() {
        super(ResourceLocation.fromNamespaceAndPath(ROACW.MODID, ""));
    }

    @Override
    public ResourceLocation getModelResource(EtherealTalisman object) {
        return ResourceLocation.fromNamespaceAndPath(
                ROACW.MODID,
                "geo/item/curio/ethereal_talisman.geo.json"
        );
    }

    @Override
    public ResourceLocation getTextureResource(EtherealTalisman object) {
        return ResourceLocation.fromNamespaceAndPath(
                ROACW.MODID,
                "textures/item/curio/ethereal_talisman.png"
        );
    }

    @Override
    public ResourceLocation getAnimationResource(EtherealTalisman animatable) {
        return ResourceLocation.fromNamespaceAndPath(
                ROACW.MODID,
                "animations/mars_armor.animation.json"
        );
    }
}