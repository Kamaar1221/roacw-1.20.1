package net.kamaarion.roacw.items.curios.nightmare_tome;

import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.Multimap;
import io.redspace.ironsspellbooks.api.registry.AttributeRegistry;
import io.redspace.ironsspellbooks.item.SpellBook;
import io.redspace.ironsspellbooks.item.weapons.AttributeContainer;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;
import software.bernie.geckolib.animatable.GeoItem;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.util.GeckoLibUtil;
import top.theillusivec4.curios.api.SlotContext;

import java.util.UUID;
import java.util.function.Consumer;

public class NightmareTome extends SpellBook implements GeoItem {
  private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);
  public NightmareTome() {
    super(10);
    withSpellbookAttributes(
            new AttributeContainer(AttributeRegistry.ICE_SPELL_POWER, 0.3, AttributeModifier.Operation.MULTIPLY_BASE),
            new AttributeContainer(AttributeRegistry.FIRE_SPELL_POWER, 0.3, AttributeModifier.Operation.MULTIPLY_BASE),
            new AttributeContainer(AttributeRegistry.SUMMON_DAMAGE, 0.15, AttributeModifier.Operation.MULTIPLY_BASE),
            new AttributeContainer(AttributeRegistry.ELDRITCH_SPELL_POWER, 0.1, AttributeModifier.Operation.MULTIPLY_BASE),
            new AttributeContainer(AttributeRegistry.MAX_MANA, 300, AttributeModifier.Operation.ADDITION));
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
