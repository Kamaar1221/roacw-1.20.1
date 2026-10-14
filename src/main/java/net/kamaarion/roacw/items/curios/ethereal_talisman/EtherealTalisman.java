package net.kamaarion.roacw.items.curios.ethereal_talisman;

import com.gametechbc.spelllib.init.GSLAttributeRegistry;
import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.Multimap;
import io.redspace.ironsspellbooks.api.magic.MagicData;
import io.redspace.ironsspellbooks.api.registry.AttributeRegistry;
import io.redspace.ironsspellbooks.item.curios.CurioBaseItem;
import net.acetheeldritchking.cataclysm_spellbooks.registries.CSAttributeRegistry;
import net.kamaarion.roacw.registeries.ROACWItemRegistry;
import net.minecraft.ChatFormatting;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;
import software.bernie.geckolib.animatable.GeoItem;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.AnimationState;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.core.animation.RawAnimation;
import software.bernie.geckolib.core.object.PlayState;
import software.bernie.geckolib.util.GeckoLibUtil;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.SlotContext;
import top.theillusivec4.curios.api.type.capability.ICurioItem;

import javax.annotation.Nullable;
import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;

public class EtherealTalisman extends CurioBaseItem implements ICurioItem, GeoItem {

    public static final float MANA_PER_KILL = 50.0F;

    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    public EtherealTalisman(Properties properties) {
        super(properties
                .stacksTo(1)
                .fireResistant()
                .rarity(ROACWItemRegistry.OTHERWORLDLY)
        );
    }

    @Override
    public void initializeClient(Consumer<IClientItemExtensions> consumer) {
        consumer.accept(new IClientItemExtensions() {
            private EtherealTalismanRenderer renderer;

            @Override
            public BlockEntityWithoutLevelRenderer getCustomRenderer() {
                if (this.renderer == null) {
                    this.renderer = new EtherealTalismanRenderer();
                }

                return this.renderer;
            }
        });
    }

    @Override
    public void curioTick(SlotContext slotContext, ItemStack stack) {
        // Ethereal Talisman tick behavior will go here later.
    }

    @Override
    public boolean canEquip(SlotContext slotContext, ItemStack stack) {
        return true;
    }

    @Override
    public boolean canUnequip(SlotContext slotContext, ItemStack stack) {
        return true;
    }

    public static boolean isEquipped(Player player) {
        return CuriosApi.getCuriosInventory(player)
                .map(handler -> handler.findFirstCurio(
                        stack -> stack.getItem() instanceof EtherealTalisman
                ).isPresent())
                .orElse(false);
    }

    public static void restoreMana(Player player) {
        MagicData magicData = MagicData.getPlayerMagicData(player);

        if (magicData != null) {
            magicData.addMana(MANA_PER_KILL);
        }
    }


    private PlayState idlePredicate(AnimationState<EtherealTalisman> event) {
        event.getController().setAnimation(
                RawAnimation.begin().thenLoop("idle")
        );

        return PlayState.CONTINUE;
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar data) {
        data.add(new AnimationController<>(
                this,
                "controller",
                20,
                this::idlePredicate
        ));
    }

    @Override
    public Multimap<Attribute, AttributeModifier> getAttributeModifiers(SlotContext slotContext, UUID uuid, ItemStack stack) {
        Multimap<Attribute, AttributeModifier> attr = LinkedHashMultimap.create();
        attr.put(AttributeRegistry.MAX_MANA.get(),
                new AttributeModifier(uuid, "Mana", 500, AttributeModifier.Operation.ADDITION));
        attr.put(AttributeRegistry.SPELL_POWER.get(),
                new AttributeModifier(uuid, "Spell Power", 0.25, AttributeModifier.Operation.MULTIPLY_BASE));
        attr.put(GSLAttributeRegistry.SPELL_CRIT_CHANCE.get(),
                new AttributeModifier(uuid, "Spell Crit Chance", 0.25, AttributeModifier.Operation.MULTIPLY_BASE));

        return attr;
    }

    @Override
    public void appendHoverText(ItemStack pStack, @Nullable Level pLevel, List<Component> pTooltipComponents, TooltipFlag pIsAdvanced) {
        if (pLevel != null && pLevel.isClientSide()) {
            net.minecraft.client.gui.Font font = net.minecraft.client.Minecraft.getInstance().font;
            int maxTooltipWidth = 300;

            Component loreText = Component.translatable("item.roacw.ethereal_talisman.desc").withStyle(ChatFormatting.AQUA);
            font.getSplitter().splitLines(loreText, maxTooltipWidth, loreText.getStyle()).forEach(formattedText -> {
                pTooltipComponents.add(Component.literal(formattedText.getString()).withStyle(loreText.getStyle()));
            });

            pTooltipComponents.add(Component.empty());


        } else {
            super.appendHoverText(pStack, pLevel, pTooltipComponents, pIsAdvanced);
        }
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.cache;
    }

    @Override
    public double getTick(Object object) {
        return 0;
    }
}