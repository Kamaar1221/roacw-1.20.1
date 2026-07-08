package net.kamaarion.roacw.items.weapons.earth_splitter;

import io.redspace.ironsspellbooks.api.spells.IPresetSpellContainer;
import io.redspace.ironsspellbooks.api.spells.ISpellContainer;
import io.redspace.ironsspellbooks.item.weapons.StaffItem;
import io.redspace.ironsspellbooks.util.ItemPropertiesHelper;
import net.kamaarion.roacw.items.weapons.ROACWWeaponTiers;
import net.kamaarion.roacw.registeries.ROACWItemRegistry;
import net.kamaarion.roacw.registeries.ROACWSpellRegistries;
import net.minecraft.ChatFormatting;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;
import software.bernie.geckolib.animatable.GeoItem;
import software.bernie.geckolib.core.animatable.GeoAnimatable;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.util.GeckoLibUtil;

import javax.annotation.Nullable;
import java.util.List;
import java.util.function.Consumer;

public class EarthSplitterItem extends StaffItem implements GeoItem, IPresetSpellContainer, GeoAnimatable {
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    public EarthSplitterItem(Properties rarity) {
        super(ItemPropertiesHelper.equipment().stacksTo(1).rarity(ROACWItemRegistry.GOD_FORGED), ROACWWeaponTiers.EARTH_SPLITTER);
    }



    @Override
    public void initializeClient(Consumer<IClientItemExtensions> consumer) {
        consumer.accept(new IClientItemExtensions() {
            private EarthSplitterRenderer renderer = null;

            public BlockEntityWithoutLevelRenderer getCustomRenderer() {
                if (this.renderer == null) {
                    this.renderer = new EarthSplitterRenderer();
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
        return cache;
    }

    @Override
    public void initializeSpellContainer(ItemStack itemStack) {
        if (itemStack == null || ISpellContainer.isSpellContainer(itemStack)) {
            return;
        }
        var spellContainer = ISpellContainer.create(2, true, false).mutableCopy();
        spellContainer.addSpell(ROACWSpellRegistries.IMPALING_COLUMN.get(), 8, true);
        spellContainer.addSpell(ROACWSpellRegistries.EARTHLY_VIRTUE.get(), 5, true);
        ISpellContainer.set(itemStack, spellContainer.toImmutable());
    }
    @Override
    public void appendHoverText(ItemStack pStack, @Nullable Level pLevel, List<Component> pTooltipComponents, TooltipFlag pIsAdvanced) {
        pTooltipComponents.add(Component.translatable("item.roacw.earth_splitter.desc").withStyle(ChatFormatting.GOLD));
    }
}
