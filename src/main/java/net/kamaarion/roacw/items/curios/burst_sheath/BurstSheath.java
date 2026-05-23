package net.kamaarion.roacw.items.curios.burst_sheath;

import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.Multimap;
import io.redspace.ironsspellbooks.api.registry.AttributeRegistry;
import io.redspace.ironsspellbooks.item.curios.CurioBaseItem;
import io.redspace.ironsspellbooks.util.ItemPropertiesHelper;
import net.kamaarion.roacw.items.curios.burst_sheath.BurstSheathRenderer;
import net.kamaarion.roacw.registeries.ROACWAttributeRegistry;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;
import software.bernie.geckolib.animatable.GeoItem;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.AnimationState;
import software.bernie.geckolib.core.animation.RawAnimation;
import software.bernie.geckolib.core.object.PlayState;
import software.bernie.geckolib.util.GeckoLibUtil;
import top.theillusivec4.curios.api.SlotContext;
import top.theillusivec4.curios.api.type.capability.ICurioItem;

import java.util.UUID;
import java.util.function.Consumer;

public class BurstSheath extends CurioBaseItem implements ICurioItem, GeoItem {
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    public BurstSheath() {
        super(ItemPropertiesHelper
                .equipment()
                .stacksTo(1)
                .fireResistant()
                .rarity(Rarity.EPIC)
        );
    }

    public void initializeClient(Consumer<IClientItemExtensions> consumer) {
        consumer.accept(new IClientItemExtensions() {
            private BurstSheathRenderer renderer;

            public BlockEntityWithoutLevelRenderer getCustomRenderer() {
                if (this.renderer == null) {
                    this.renderer = new BurstSheathRenderer();
                }

                return this.renderer;
            }
        });
    }

    @Override
    public void curioTick(SlotContext slotContext, ItemStack stack) {

        // Runs every tick while worn
        // You can leave this empty if you want

    }

    @Override
    public boolean canEquip(SlotContext slotContext, ItemStack stack) {
        return true;
    }

    @Override
    public boolean canUnequip(SlotContext slotContext, ItemStack stack) {
        return true;
    }

    private PlayState idlePredicate(AnimationState<net.kamaarion.roacw.items.curios.stasis_curse.StatisCurse> event) {
        event.getController().setAnimation(RawAnimation.begin().thenLoop("idle"));
        return PlayState.CONTINUE;
    }

    public void registerControllers(AnimatableManager.ControllerRegistrar data) {
        data.add(new AnimationController[]{new AnimationController(this, "controller", 20, this::idlePredicate)});
    }

    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.cache;
    }

    @Override
    public double getTick(Object object) {
        return 0;
    }

    @Override
    public Multimap<Attribute, AttributeModifier> getAttributeModifiers(SlotContext slotContext, UUID uuid, ItemStack stack) {
        Multimap<Attribute, AttributeModifier> attr = LinkedHashMultimap.create();
        attr.put(ROACWAttributeRegistry.EXO_MAGIC_POWER.get(),
                new AttributeModifier(uuid, "Exo Spell Power", 0.15, AttributeModifier.Operation.MULTIPLY_BASE));
        attr.put(AttributeRegistry.SUMMON_DAMAGE.get(),
                new AttributeModifier(uuid, "Summon Damage", 0.30, AttributeModifier.Operation.MULTIPLY_BASE));
        attr.put(AttributeRegistry.MAX_MANA.get(),
                new AttributeModifier(uuid, "Max Mana", 200, AttributeModifier.Operation.ADDITION));

        return attr;
    }


}