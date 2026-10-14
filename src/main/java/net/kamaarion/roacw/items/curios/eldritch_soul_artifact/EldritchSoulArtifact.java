package net.kamaarion.roacw.items.curios.eldritch_soul_artifact;

import com.gametechbc.spelllib.init.GSLAttributeRegistry;
import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.Multimap;
import dev.shadowsoffire.attributeslib.api.ALObjects;
import io.redspace.ironsspellbooks.api.registry.AttributeRegistry;
import io.redspace.ironsspellbooks.item.curios.CurioBaseItem;
import net.kamaarion.roacw.registeries.ROACWItemRegistry;
import net.minecraft.ChatFormatting;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;
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
import top.theillusivec4.curios.api.SlotContext;
import top.theillusivec4.curios.api.type.capability.ICurioItem;

import javax.annotation.Nullable;
import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;

public class EldritchSoulArtifact extends CurioBaseItem implements ICurioItem, GeoItem {

    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    public EldritchSoulArtifact(Properties properties) {
        super(properties
                .stacksTo(1)
                .fireResistant()
                .rarity(ROACWItemRegistry.OTHERWORLDLY)
        );
    }

    @Override
    public void initializeClient(Consumer<IClientItemExtensions> consumer) {
        consumer.accept(new IClientItemExtensions() {
            private EldritchSoulArtifactRenderer renderer;

            @Override
            public BlockEntityWithoutLevelRenderer getCustomRenderer() {
                if (this.renderer == null) {
                    this.renderer = new EldritchSoulArtifactRenderer();
                }

                return this.renderer;
            }
        });
    }

    @Override
    public void curioTick(SlotContext slotContext, ItemStack stack) {
    }

    @Override
    public boolean canEquip(SlotContext slotContext, ItemStack stack) {
        return true;
    }

    @Override
    public boolean canUnequip(SlotContext slotContext, ItemStack stack) {
        return true;
    }

    private PlayState idlePredicate(AnimationState<EldritchSoulArtifact> event) {
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
        attr.put(Attributes.ATTACK_SPEED,
                new AttributeModifier(uuid, "Attack Speed", 0.30, AttributeModifier.Operation.MULTIPLY_BASE));
        attr.put(ALObjects.Attributes.ARROW_DAMAGE.get(),
                new AttributeModifier(uuid, "Arrow Damage", 0.15, AttributeModifier.Operation.MULTIPLY_BASE));
        attr.put(GSLAttributeRegistry.MAGIC_PROJECTILE_DAMAGE.get(),
                new AttributeModifier(uuid, "Magic Projectile Damage", 0.15, AttributeModifier.Operation.MULTIPLY_BASE));
        attr.put(AttributeRegistry.MANA_REGEN.get(),
                new AttributeModifier(uuid, "Mana Regen", 0.20, AttributeModifier.Operation.MULTIPLY_BASE));
        attr.put(AttributeRegistry.SUMMON_DAMAGE.get(),
                new AttributeModifier(uuid, "Summon Damage", 0.25, AttributeModifier.Operation.MULTIPLY_BASE));

        return attr;
    }

    @Override
    public void appendHoverText(ItemStack pStack, @Nullable Level pLevel, List<Component> pTooltipComponents, TooltipFlag pIsAdvanced) {
        if (pLevel != null && pLevel.isClientSide()) {
            net.minecraft.client.gui.Font font = net.minecraft.client.Minecraft.getInstance().font;
            int maxTooltipWidth = 240;

            Component loreText = Component.translatable("item.roacw.eldritch_soul_artifact.desc").withStyle(ChatFormatting.DARK_RED);
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