package net.kamaarion.roacw.effects;

import net.kamaarion.roacw.ROACW;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = ROACW.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class BrimstoneFlamesEffect extends MobEffect {

    public BrimstoneFlamesEffect() {
        super(MobEffectCategory.HARMFUL, 0xFF3636);
    }

    @Override
    public void applyEffectTick(LivingEntity entity, int amplifier) {
        if (entity.level().isClientSide()) {
            return;
        }

        var effectInstance = entity.getEffect(this);

        if (effectInstance == null) {
            return;
        }

        int duration = effectInstance.getDuration();

        if (duration % 5 == 0) {
            float damageAmount = 4.0f + amplifier;

            entity.hurt(entity.damageSources().magic(), damageAmount);
        }
    }

    @Override
    public boolean isDurationEffectTick(int duration, int amplifier) {
        return true;
    }
}