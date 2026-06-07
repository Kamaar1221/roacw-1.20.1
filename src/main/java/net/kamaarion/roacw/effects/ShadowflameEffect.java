package net.kamaarion.roacw.effects;

import net.kamaarion.roacw.ROACW; // Imported your main mod class for the MODID
import net.kamaarion.roacw.registeries.ROACWEffectRegistry;
import net.kamaarion.roacw.registeries.ROACWParticleRegistry;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraftforge.event.entity.living.LivingHealEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod; // Imported the event bus subscriber tool

// FIXED: Added the subscriber annotation so Forge listens to the healing reduction logic
@Mod.EventBusSubscriber(modid = ROACW.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class ShadowflameEffect extends MobEffect {

    public ShadowflameEffect() {
        super(MobEffectCategory.HARMFUL, 9722673);
    }

    public static final float HEALING_PER_LEVEL = -.20f;

    public ShadowflameEffect(MobEffectCategory pCategory, int pColor) {
        super(pCategory, pColor);
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
            float damageAmount = 1.0f + (float) amplifier;
            entity.hurt(entity.damageSources().magic(), damageAmount);
        }

        if (!level.isClientSide() && duration % 5 == 0 && level instanceof net.minecraft.server.level.ServerLevel serverLevel) {
            serverLevel.sendParticles(
                    ROACWParticleRegistry.SHADOWFLAME.get(),
                    entity.getX(), entity.getY() + (entity.getBbHeight() / 2), entity.getZ(),
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
        var effect = event.getEntity().getEffect(ROACWEffectRegistry.SHADOWFLAME.get());
        if (effect != null) {
            int lvl = effect.getAmplifier() + 1;
            float healingMult = 1 + HEALING_PER_LEVEL * lvl;
            event.setAmount(event.getAmount() * healingMult);
        }
    }
}
