package net.kamaarion.roacw.effects;

import io.redspace.ironsspellbooks.effect.MagicMobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;

public class ArmorCrunchEffect extends MagicMobEffect {

    private static final double ARMOR_REDUCTION = -10.0D;
    private static final double TOUGHNESS_REDUCTION = -0.20D;

    public ArmorCrunchEffect() {
        super(MobEffectCategory.HARMFUL, 0x8B4513);

        // Remove 10 armor points
        this.addAttributeModifier(
                Attributes.ARMOR,
                "6c77a0d0-1d8b-4f5e-9e0d-8c5e7f4f3b21",
                ARMOR_REDUCTION,
                AttributeModifier.Operation.ADDITION
        );

        // Reduce armor toughness by 20%
        this.addAttributeModifier(
                Attributes.ARMOR_TOUGHNESS,
                "c4e3d8a9-72a1-45e5-95f8-b6a6e0d1f332",
                TOUGHNESS_REDUCTION,
                AttributeModifier.Operation.MULTIPLY_TOTAL
        );
    }
}