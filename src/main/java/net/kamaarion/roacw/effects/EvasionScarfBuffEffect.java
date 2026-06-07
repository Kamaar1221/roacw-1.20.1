package net.kamaarion.roacw.effects;

import io.redspace.ironsspellbooks.api.registry.AttributeRegistry;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;

public class EvasionScarfBuffEffect extends MobEffect {
    public EvasionScarfBuffEffect() {
        super(MobEffectCategory.HARMFUL, 9722673);
        this.addAttributeModifier(Attributes.ATTACK_DAMAGE, "68078724-8653-42D5-A245-9D14A1F54685", 0.2D, AttributeModifier.Operation.MULTIPLY_TOTAL);
        this.addAttributeModifier(AttributeRegistry.SPELL_POWER.get(), "B237E76D-15E8-4513-A735-55BB25C33603", 0.2D, AttributeModifier.Operation.MULTIPLY_TOTAL);
        this.addAttributeModifier(Attributes.MOVEMENT_SPEED, "B237E76D-15E8-4513-A735-55BB25C33603", 0.2D, AttributeModifier.Operation.MULTIPLY_TOTAL);
    }
}
