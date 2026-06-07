package net.kamaarion.roacw.effects;

import io.redspace.ironsspellbooks.api.registry.AttributeRegistry;
import io.redspace.ironsspellbooks.effect.MagicMobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeMap;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;

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
        this.addAttributeModifier(AttributeRegistry.SPELL_RESIST.get(), "9760a734-1ad6-4fb3-9d1c-54dff1e558d4", AuricChargeEffect.SPELL_RESIST_PER_LEVEL, AttributeModifier.Operation.MULTIPLY_TOTAL);
        this.addAttributeModifier(Attributes.MOVEMENT_SPEED, "9760a734-1ad6-4fb3-9d1c-54dff1e558d4", AuricChargeEffect.MOVEMENT_SPEED_PER_LEVEL, AttributeModifier.Operation.MULTIPLY_TOTAL);
        this.addAttributeModifier(Attributes.ATTACK_DAMAGE, "9760a734-1ad6-4fb3-9d1c-54dff1e558d4", AuricChargeEffect.ATTACK_DAMAGE_PER_LEVEL, AttributeModifier.Operation.ADDITION);
        this.addAttributeModifier(AttributeRegistry.SPELL_POWER.get(), "9760a734-1ad6-4fb3-9d1c-54dff1e558d4", AuricChargeEffect.SPELL_POWER_PER_LEVEL, AttributeModifier.Operation.MULTIPLY_TOTAL);
    }

    @Override
    public void applyEffectTick(LivingEntity entity, int amplifier) {
        // Only execute on the logical server side to prevent desync
        if (!entity.getCommandSenderWorld().isClientSide()) {

            // 2. Original Healing Logic
            // Your custom tick check below controls how often this healing actually runs
            int duration = entity.getEffect(this).getDuration();
            int ticks = 40 >> amplifier;

            if (ticks <= 0 || duration % ticks == 0) {
                // Heals 2.0F (1 full heart) multiplied by effect level
                entity.heal(2.0F * (amplifier + 1));
            }
        }
    }

    @Override
    public boolean isDurationEffectTick(int duration, int amplifier) {
        // CHANGED: Force this method to return true every single tick (1).
        // This ensures the fire resistance immunity is constantly reapplied without gaps.
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

