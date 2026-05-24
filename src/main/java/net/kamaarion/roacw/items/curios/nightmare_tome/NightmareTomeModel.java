package net.kamaarion.roacw.items.curios.nightmare_tome;

import net.kamaarion.roacw.ROACW;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public class NightmareTomeModel extends GeoModel<NightmareTome> {

  @Override
  public ResourceLocation getModelResource(NightmareTome animatable) {
    return ROACW.id("geo/item/curio/nightmare_tome.geo.json");
  }
  
  @Override
  public ResourceLocation getTextureResource(NightmareTome animatable) {
    return ROACW.id("textures/item/curio/nightmare_tome.png");
  }
  
  @Override
  public ResourceLocation getAnimationResource(NightmareTome animatable) {
    return ROACW.id("animations/nightmare_tome.animation.json");
  }
}
