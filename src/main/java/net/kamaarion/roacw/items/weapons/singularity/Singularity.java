package net.kamaarion.roacw.items.weapons.singularity;

import io.redspace.ironsspellbooks.api.registry.SpellRegistry;
import io.redspace.ironsspellbooks.api.spells.IPresetSpellContainer;
import io.redspace.ironsspellbooks.api.spells.ISpellContainer;
import io.redspace.ironsspellbooks.item.weapons.StaffItem;
import net.kamaarion.roacw.items.weapons.ROACWWeaponTiers;
import net.kamaarion.roacw.registeries.ROACWItemRegistry;
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
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.AnimationState;
import software.bernie.geckolib.core.animation.RawAnimation;
import software.bernie.geckolib.core.object.PlayState;
import software.bernie.geckolib.util.GeckoLibUtil;

import javax.annotation.Nullable;
import java.util.List;
import java.util.function.Consumer;

public class Singularity extends StaffItem implements GeoItem, IPresetSpellContainer {

    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    public Singularity(Properties properties) {
        super(
                properties
                        .stacksTo(1)
                        .fireResistant()
                        .rarity(ROACWItemRegistry.OTHERWORLDLY),
                ROACWWeaponTiers.SINGULARITY
        );
    }

    @Override
    public void initializeClient(Consumer<IClientItemExtensions> consumer) {
        consumer.accept(new IClientItemExtensions() {

            private SingularityRenderer renderer = null;

            @Override
            public BlockEntityWithoutLevelRenderer getCustomRenderer() {
                if (this.renderer == null) {
                    this.renderer = new SingularityRenderer();
                }

                return this.renderer;
            }
        });
    }

    private PlayState idlePredicate(AnimationState<Singularity> event) {
        event.getController().setAnimation(
                RawAnimation.begin().thenLoop("idle")
        );

        return PlayState.CONTINUE;
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(
                this,
                "controller",
                0,
                this::idlePredicate
        ));
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
                SpellRegistry.BLACK_HOLE_SPELL.get(),
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
                    "item.roacw.singularity.desc"
            ).withStyle(ChatFormatting.LIGHT_PURPLE);

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

            pTooltipComponents.add(Component.empty());

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