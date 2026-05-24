package net.kamaarion.roacw.items.curios.nightmare_tome;

import net.kamaarion.roacw.items.curios.elemental_gauntlet.ElementalGauntletRenderer;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.world.item.Item;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;
import software.bernie.geckolib.animatable.GeoItem;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.util.GeckoLibUtil;

import java.util.function.Consumer;

public class NightmareTome extends Item implements GeoItem {
  private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);
  public NightmareTome() {
    super(new Item.Properties());
  }
  
  @Override
  public void initializeClient(Consumer<IClientItemExtensions> consumer) {
    consumer.accept(new IClientItemExtensions() {
      private NightmareTomeRenderer renderer;
      
      public BlockEntityWithoutLevelRenderer getCustomRenderer() {
        if (this.renderer == null) {
          this.renderer = new NightmareTomeRenderer();
        }
        
        return this.renderer;
      }
    });
  }
  
  @Override
  public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
  
  }
  
  @Override
  public AnimatableInstanceCache getAnimatableInstanceCache() {
    return this.cache;
  }
}
