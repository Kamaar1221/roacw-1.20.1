package net.kamaarion.roacw.effects;

import io.redspace.ironsspellbooks.api.registry.AttributeRegistry;
import io.redspace.ironsspellbooks.effect.MagicMobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeMap;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;

public class AuricChargeEffect extends MagicMobEffect {
    public AuricChargeEffect() {
        super(MobEffectCategory.BENEFICIAL, 8571381);

        this.addAttributeModifier(Attributes.ARMOR, "204e4bd9-08c5-4d77-b90c-4223bbc650ac", AuricChargeEffect.ARMOR_PER_LEVEL, AttributeModifier.Operation.MULTIPLY_TOTAL);
        this.addAttributeModifier(Attributes.ARMOR_TOUGHNESS, "9760a734-1ad6-4fb3-9d1c-54dff1e558d4", AuricChargeEffect.ARMOR_TOUGHNESS_PER_LEVEL, AttributeModifier.Operation.MULTIPLY_TOTAL);
    }

    // Buffs
    public static final float ARMOR_PER_LEVEL = 0.1f;
    public static final float ARMOR_TOUGHNESS_PER_LEVEL = 0.1f;

    @Override
    public void removeAttributeModifiers(LivingEntity pLivingEntity, AttributeMap pAttributeMap, int pAmplifier) {
        super.removeAttributeModifiers(pLivingEntity, pAttributeMap, pAmplifier);
    }

    @Override
    public void addAttributeModifiers(LivingEntity pLivingEntity, AttributeMap pAttributeMap, int pAmplifier) {
        super.addAttributeModifiers(pLivingEntity, pAttributeMap, pAmplifier);
    }
}