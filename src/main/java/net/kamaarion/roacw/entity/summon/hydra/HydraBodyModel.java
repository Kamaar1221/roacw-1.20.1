package net.kamaarion.roacw.entity.summon.hydra;

import net.kamaarion.roacw.ROACW;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import software.bernie.geckolib.model.GeoModel;

@OnlyIn(Dist.CLIENT)
public class HydraBodyModel extends GeoModel<HydraBodyEntity> {

    private static final ResourceLocation MODEL = ResourceLocation.fromNamespaceAndPath(ROACW.MODID,"geo/entity/summon/hydra_body.geo.json");
    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(ROACW.MODID,"textures/entity/summon/hydra_texture.png");
    private static final ResourceLocation ANIMATION = ResourceLocation.fromNamespaceAndPath(ROACW.MODID,"animations/entity/endo_hydra_body.animation.json");

    @Override
    public ResourceLocation getModelResource(HydraBodyEntity animatable) {
        return MODEL;
    }

    @Override
    public ResourceLocation getTextureResource(HydraBodyEntity animatable) {
        return TEXTURE;
    }

    @Override
    public ResourceLocation getAnimationResource(HydraBodyEntity animatable) {
        return ANIMATION;
    }
}
