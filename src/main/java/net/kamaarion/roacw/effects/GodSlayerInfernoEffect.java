package net.kamaarion.roacw.effects;

import net.kamaarion.roacw.registeries.ROACWTags;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;

public class GodSlayerInfernoEffect extends MobEffect {

    private static final float BASE_DAMAGE_PER_TICK = 5.0F;
    private static final float BOSS_DAMAGE_MULTIPLIER = 5.0F;
    private static final int TICK_INTERVAL_TICKS = 10;

    public GodSlayerInfernoEffect() {
        super(MobEffectCategory.HARMFUL, 0xFF3333);
    }

    @Override
    public void applyEffectTick(LivingEntity entity, int amplifier) {
        if (entity.level().isClientSide) {
            return;
        }

        var effect = entity.getEffect(this);

        if (effect == null || effect.getDuration() % TICK_INTERVAL_TICKS != 0) {
            return;
        }

        float damage = BASE_DAMAGE_PER_TICK * (amplifier + 1);

        if (entity.getType().is(ROACWTags.BOSSES)) {
            damage *= BOSS_DAMAGE_MULTIPLIER;
        }

        entity.hurt(entity.damageSources().magic(), damage);
    }

    @Override
    public boolean isDurationEffectTick(int duration, int amplifier) {
        return true;
    }
}