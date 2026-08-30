package net.kamaarion.roacw.effects;

import net.kamaarion.roacw.particle.PlagueParticleHelper;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.server.level.ServerLevel;

public class PlagueEffect extends MobEffect {

    public PlagueEffect() {
        super(MobEffectCategory.HARMFUL, 0x66CC66);
    }

    @Override
    public boolean isDurationEffectTick(int duration, int amplifier) {
        return true; // Tick every game tick
    }

    @Override
    public void applyEffectTick(LivingEntity entity, int amplifier) {
        Level level = entity.level();

        if (!level.isClientSide() && level instanceof ServerLevel serverLevel) {
            int duration = 0;
            var effectInstance = entity.getEffect(this);

            if (effectInstance != null) {
                duration = effectInstance.getDuration();
            }

            // =====================
            // Damage
            // =====================
            if (duration % 20 == 0) {
                float damage = 1.0F + (amplifier * 0.5F);

                entity.invulnerableTime = 0;
                entity.hurt(entity.damageSources().magic(), damage);
            }

            // =====================
            // Particles - broadcast from the server via sendParticles().
            // applyEffectTick() only ever ran client-side for the entity's
            // OWNING player before, since vanilla doesn't sync full
            // MobEffectInstance data to other clients for arbitrary
            // entities - only a packed "effect color" for the default
            // swirl. That's why this worked on players but never on mobs.
            // =====================
            PlagueParticleHelper.spawnAroundEntityServer(
                    serverLevel,
                    entity,
                    4, // green particles
                    1  // red particles
            );
        }
    }
}