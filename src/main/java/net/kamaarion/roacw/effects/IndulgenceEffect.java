package net.kamaarion.roacw.effects;

import io.redspace.ironsspellbooks.api.registry.AttributeRegistry;
import net.kamaarion.roacw.ROACW;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraftforge.event.entity.living.MobEffectEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

public class IndulgenceEffect extends MobEffect {
    private static final String ABSORPTION_KEY = "roacw_indulgence_absorption";

    public IndulgenceEffect() {
        super(MobEffectCategory.BENEFICIAL, 0xD4AF37);

        addAttributeModifier(
                Attributes.ATTACK_DAMAGE,
                "a78d4a21-6b31-4e90-8f52-1d7c9b3e5a60",
                15.0,
                AttributeModifier.Operation.ADDITION
        );

        addAttributeModifier(
                Attributes.ARMOR,
                "b89e5b32-7c42-4fa1-9a63-2e8d0c4f6b71",
                10.0,
                AttributeModifier.Operation.ADDITION
        );

        addAttributeModifier(
                Attributes.MOVEMENT_SPEED,
                "c90f6c43-8d53-40b2-ab74-3f9e1d5a7c82",
                0.20,
                AttributeModifier.Operation.MULTIPLY_TOTAL
        );

        addAttributeModifier(
                AttributeRegistry.SPELL_POWER.get(),
                "d01a7d54-9e64-41c3-bc85-4a0f2e6b8d93",
                0.10,
                AttributeModifier.Operation.MULTIPLY_TOTAL
        );

        addAttributeModifier(
                AttributeRegistry.MANA_REGEN.get(),
                "e12b8e65-af75-42d4-cd96-5b1a3f7c9e04",
                0.10,
                AttributeModifier.Operation.MULTIPLY_TOTAL
        );
    }

    @Override
    public boolean isDurationEffectTick(int duration, int amplifier) {
        return duration % 40 == 0;
    }

    @Override
    public void applyEffectTick(LivingEntity entity, int amplifier) {
        if (!entity.level().isClientSide && entity.getHealth() < entity.getMaxHealth()) {
            entity.heal(1.0F);
        }
    }

    @Mod.EventBusSubscriber(modid = ROACW.MODID)
    public static class AbsorptionHandler {
        @SubscribeEvent
        public static void onEffectAdded(MobEffectEvent.Added event) {
            if (event.getEffectInstance().getEffect() instanceof IndulgenceEffect) {
                LivingEntity entity = event.getEntity();

                if (!entity.level().isClientSide) {
                    entity.getPersistentData().putFloat(ABSORPTION_KEY, 10.0F);
                    entity.setAbsorptionAmount(entity.getAbsorptionAmount() + 10.0F);
                }
            }
        }

        @SubscribeEvent
        public static void onEffectRemoved(MobEffectEvent.Remove event) {
            if (event.getEffect() instanceof IndulgenceEffect) {
                removeAbsorption(event.getEntity());
            }
        }

        @SubscribeEvent
        public static void onEffectExpired(MobEffectEvent.Expired event) {
            if (event.getEffectInstance().getEffect() instanceof IndulgenceEffect) {
                removeAbsorption(event.getEntity());
            }
        }

        private static void removeAbsorption(LivingEntity entity) {
            if (entity.level().isClientSide) {
                return;
            }

            float granted = entity.getPersistentData().getFloat(ABSORPTION_KEY);

            if (granted > 0.0F) {
                entity.setAbsorptionAmount(
                        Math.max(0.0F, entity.getAbsorptionAmount() - granted)
                );

                entity.getPersistentData().remove(ABSORPTION_KEY);
            }
        }
    }
}