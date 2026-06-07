package net.kamaarion.roacw.effects;

import io.redspace.ironsspellbooks.api.registry.AttributeRegistry;
import net.kamaarion.roacw.ROACW; // Imported your main mod class for the MODID
import net.kamaarion.roacw.registeries.ROACWEffectRegistry;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraftforge.client.extensions.common.IClientMobEffectExtensions;
import net.minecraftforge.event.entity.living.LivingHealEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod; // Imported the event bus subscriber tool

import java.util.function.Consumer;

@Mod.EventBusSubscriber(modid = ROACW.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class ElementalMixEffect extends MobEffect {

    public ElementalMixEffect() {
        super(MobEffectCategory.HARMFUL, 9722673);
        this.addAttributeModifier(Attributes.ARMOR, "68078724-8653-42D5-A245-9D14A1F54685", -0.2D, AttributeModifier.Operation.MULTIPLY_TOTAL);
        this.addAttributeModifier(Attributes.ARMOR_TOUGHNESS, "B237E76D-15E8-4513-A735-55BB25C33603", -0.2D, AttributeModifier.Operation.MULTIPLY_TOTAL);
        this.addAttributeModifier(Attributes.MOVEMENT_SPEED, "B237E76D-15E8-4513-A735-55BB25C33604", -0.2D, AttributeModifier.Operation.MULTIPLY_TOTAL);
        this.addAttributeModifier(AttributeRegistry.SPELL_RESIST.get(), "a807afe8-33ec-41f9-8d32-05b47302e73a", -0.2D, AttributeModifier.Operation.MULTIPLY_TOTAL);
    }

    public static final float HEALING_PER_LEVEL = -.05f;

    public ElementalMixEffect(MobEffectCategory pCategory, int pColor) {
        super(pCategory, pColor);
    }

    @Override
    public void initializeClient(Consumer<IClientMobEffectExtensions> consumer) {
        consumer.accept(new IClientMobEffectExtensions() {
            @Override
            public boolean isVisibleInGui(MobEffectInstance instance) {
                return true;
            }

            @Override
            public boolean isVisibleInInventory(MobEffectInstance instance) {
                return true;
            }
        });
    }

    @Override
    public void applyEffectTick(net.minecraft.world.entity.LivingEntity entity, int amplifier) {
        net.minecraft.world.level.Level level = entity.level();
        int duration = 0;
        var effectInstance = entity.getEffect(this);

        if (effectInstance != null) {
            duration = effectInstance.getDuration();
        }

        if (!level.isClientSide() && duration % 40 == 0) {
            float damageAmount = 4.0f + (float) amplifier;
            entity.hurt(entity.damageSources().magic(), damageAmount);
        }

        if (!level.isClientSide() && duration % 5 == 0 && level instanceof net.minecraft.server.level.ServerLevel serverLevel) {
            double spawnX = entity.getX();
            double spawnY = entity.getY() + (entity.getBbHeight() / 2);
            double spawnZ = entity.getZ();

            serverLevel.sendParticles(
                    ParticleTypes.DRAGON_BREATH,
                    spawnX, spawnY, spawnZ,
                    5,
                    0.3, 0.5, 0.3,
                    0.05
            );

            serverLevel.sendParticles(
                    ParticleTypes.SOUL_FIRE_FLAME,
                    spawnX, spawnY, spawnZ,
                    5,
                    0.3, 0.5, 0.3,
                    0.05
            );
        }
    }

    @Override
    public boolean isDurationEffectTick(int duration, int amplifier) {
        return true;
    }

    @SubscribeEvent
    public static void reduceHealing(LivingHealEvent event) {
        var effect = event.getEntity().getEffect(ROACWEffectRegistry.ELEMENTAL_MIX.get());
        if (effect != null) {
            int lvl = effect.getAmplifier() + 1;
            float healingMult = 1 + HEALING_PER_LEVEL * lvl;
            event.setAmount(event.getAmount() * healingMult);
        }
    }
}
