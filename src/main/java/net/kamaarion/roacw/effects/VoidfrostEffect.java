package net.kamaarion.roacw.effects;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;

import java.util.UUID;

public class VoidfrostEffect extends MobEffect {

    private static final UUID SLOW_MODIFIER_ID =
            UUID.fromString("b1e2c3d4-5566-7788-99aa-bbccddeeff00");

    private static final double SLOW_PER_STACK = -0.25D;

    private static final int DAMAGE_INTERVAL_TICKS = 40;

    private static final float DAMAGE_PER_STACK = 1.0F;

    public VoidfrostEffect() {
        super(MobEffectCategory.HARMFUL, 0x1B2A4A);

        this.addAttributeModifier(
                Attributes.MOVEMENT_SPEED,
                SLOW_MODIFIER_ID.toString(),
                SLOW_PER_STACK,
                AttributeModifier.Operation.MULTIPLY_TOTAL
        );
    }

    @Override
    public boolean isDurationEffectTick(int duration, int amplifier) {
        return duration % DAMAGE_INTERVAL_TICKS == 0;
    }

    @Override
    public void applyEffectTick(LivingEntity entity, int amplifier) {
        int stacks = amplifier + 1;

        entity.hurt(
                entity.damageSources().magic(),
                DAMAGE_PER_STACK * stacks
        );
    }
}