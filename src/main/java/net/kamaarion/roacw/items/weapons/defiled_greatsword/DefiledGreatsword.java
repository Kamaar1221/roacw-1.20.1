package net.kamaarion.roacw.items.weapons.defiled_greatsword;

import io.redspace.ironsspellbooks.api.registry.SpellRegistry;
import io.redspace.ironsspellbooks.api.spells.IPresetSpellContainer;
import io.redspace.ironsspellbooks.api.spells.ISpellContainer;
import io.redspace.ironsspellbooks.item.weapons.StaffItem;
import net.acetheeldritchking.cataclysm_spellbooks.registries.SpellRegistries;
import net.kamaarion.roacw.items.weapons.ROACWWeaponTiers;
import net.kamaarion.roacw.registeries.ROACWItemRegistry;
import net.kamaarion.roacw.registeries.ROACWSpellRegistry;
import net.minecraft.ChatFormatting;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;
import software.bernie.geckolib.animatable.GeoItem;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.util.GeckoLibUtil;

import javax.annotation.Nullable;
import java.util.List;
import java.util.function.Consumer;

public class DefiledGreatsword extends StaffItem implements GeoItem, IPresetSpellContainer {

    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    public DefiledGreatsword(Properties properties) {
        super(
                properties
                        .stacksTo(1)
                        .rarity(ROACWItemRegistry.GOD_FORGED),
                ROACWWeaponTiers.DEFILED_GREATSWORD
        );
    }

    @Override
    public void initializeClient(Consumer<IClientItemExtensions> consumer) {
        consumer.accept(new IClientItemExtensions() {

            private DefiledGreatswordRenderer renderer = null;

            @Override
            public BlockEntityWithoutLevelRenderer getCustomRenderer() {
                if (this.renderer == null) {
                    this.renderer = new DefiledGreatswordRenderer();
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

        var spellContainer = ISpellContainer.create(1, true, false).mutableCopy();

        spellContainer.addSpell(
                SpellRegistry.FLAMING_STRIKE_SPELL.get(),
                5,
                true
        );

        ISpellContainer.set(itemStack, spellContainer.toImmutable());
    }

    @Override
    public void appendHoverText(
            ItemStack pStack,
            @Nullable Level pLevel,
            List<Component> pTooltipComponents,
            TooltipFlag pIsAdvanced
    ) {
        if (pLevel != null && pLevel.isClientSide()) {
            net.minecraft.client.gui.Font font =
                    net.minecraft.client.Minecraft.getInstance().font;

            int maxTooltipWidth = 240;

            Component loreText = Component.translatable(
                    "item.roacw.defiled_greatsword.desc"
            ).withStyle(ChatFormatting.YELLOW);

            font.getSplitter()
                    .splitLines(
                            loreText,
                            maxTooltipWidth,
                            loreText.getStyle()
                    )
                    .forEach(formattedText -> {
                        pTooltipComponents.add(
                                Component.literal(formattedText.getString())
                                        .withStyle(loreText.getStyle())
                        );
                    });

        } else {
            super.appendHoverText(
                    pStack,
                    pLevel,
                    pTooltipComponents,
                    pIsAdvanced
            );
        }
    }
}