package net.kamaarion.roacw.effects;

import io.redspace.ironsspellbooks.api.registry.AttributeRegistry;
import io.redspace.ironsspellbooks.effect.MagicMobEffect;
import io.redspace.ironsspellbooks.registries.ParticleRegistry;
import net.kamaarion.roacw.registeries.ROACWParticleRegistry;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeMap;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.core.particles.ParticleTypes; // ADDED
import net.minecraft.server.level.ServerLevel;     // ADDED

public class AuricChargeEffect extends MagicMobEffect {
    // Buffs
    public static final float ARMOR_PER_LEVEL = 0.5f;
    public static final float ARMOR_TOUGHNESS_PER_LEVEL = 0.5f;
    public static final float MOVEMENT_SPEED_PER_LEVEL = 0.25f;
    public static final float SPELL_RESIST_PER_LEVEL = 0.20f;
    public static final float ATTACK_DAMAGE_PER_LEVEL = 5.0f;
    public static final float SPELL_POWER_PER_LEVEL = 0.1f;

    public AuricChargeEffect() {
        super(MobEffectCategory.BENEFICIAL, 8571381);

        this.addAttributeModifier(Attributes.ARMOR, "204e4bd9-08c5-4d77-b90c-4223bbc650ac", AuricChargeEffect.ARMOR_PER_LEVEL, AttributeModifier.Operation.MULTIPLY_TOTAL);
        this.addAttributeModifier(Attributes.ARMOR_TOUGHNESS, "9760a734-1ad6-4fb3-9d1c-54dff1e558d4", AuricChargeEffect.ARMOR_TOUGHNESS_PER_LEVEL, AttributeModifier.Operation.MULTIPLY_TOTAL);
        this.addAttributeModifier(AttributeRegistry.SPELL_RESIST.get(), "3b185bdf-2b36-4171-8bc6-df41285efda4", AuricChargeEffect.SPELL_RESIST_PER_LEVEL, AttributeModifier.Operation.MULTIPLY_TOTAL);
        this.addAttributeModifier(Attributes.MOVEMENT_SPEED, "e5f29d11-5364-4e92-bc96-1cda5a901047", AuricChargeEffect.MOVEMENT_SPEED_PER_LEVEL, AttributeModifier.Operation.MULTIPLY_TOTAL);
        this.addAttributeModifier(Attributes.ATTACK_DAMAGE, "8b17b209-e85d-4f76-8fef-92ee241bbf84", AuricChargeEffect.ATTACK_DAMAGE_PER_LEVEL, AttributeModifier.Operation.ADDITION);
        this.addAttributeModifier(AttributeRegistry.SPELL_POWER.get(), "5c1fcd81-fba2-4099-bc3d-51970ab2c40c", AuricChargeEffect.SPELL_POWER_PER_LEVEL, AttributeModifier.Operation.MULTIPLY_TOTAL);
    }

    @Override
    public void applyEffectTick(LivingEntity entity, int amplifier) {
        if (!entity.getCommandSenderWorld().isClientSide() && entity.getCommandSenderWorld() instanceof ServerLevel serverLevel) {

            if (entity.tickCount % 3 == 0) {
                double x = entity.getX();
                double y = entity.getY() + (entity.getBbHeight() / 2.0);
                double z = entity.getZ();

                serverLevel.sendParticles(
                        ParticleRegistry.ELECTRICITY_PARTICLE.get(),
                        x, y, z,
                        2,
                        0.4, 0.5, 0.4,
                        0.02
                );
            }

            if (entity.getEffect(this) != null) {
                int duration = entity.getEffect(this).getDuration();
                int ticks = 40 >> amplifier;
                if (ticks <= 0 || duration % ticks == 0) {
                    entity.heal(2.0F * (amplifier + 1));
                }
            }
        }
    }

    @Override
    public boolean isDurationEffectTick(int duration, int amplifier) {
        return true;
    }

    @Override
    public void removeAttributeModifiers(LivingEntity pLivingEntity, AttributeMap pAttributeMap, int pAmplifier) {
        super.removeAttributeModifiers(pLivingEntity, pAttributeMap, pAmplifier);
    }

    @Override
    public void addAttributeModifiers(LivingEntity pLivingEntity, AttributeMap pAttributeMap, int pAmplifier) {
        super.addAttributeModifiers(pLivingEntity, pAttributeMap, pAmplifier);
    }
}
