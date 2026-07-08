package net.kamaarion.roacw.effects;

import io.redspace.ironsspellbooks.registries.ParticleRegistry;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.core.particles.ParticleTypes; // ADDED
import net.minecraft.server.level.ServerLevel;     // ADDED
import java.util.UUID;

public class SanctifiedBedrockEffect extends MobEffect {
    private static final UUID ARMOR_MODIFIER_UUID = UUID.fromString("6a4f216a-b21d-4f11-9a77-4b953d528b12");
    private static final float HEAL_ON_EXPIRE = 6.0F; // 3 hearts

    public SanctifiedBedrockEffect() {
        super(MobEffectCategory.BENEFICIAL, 0xF0E68C);
        // +10 armor while effect is active
        this.addAttributeModifier(
                Attributes.ARMOR,
                ARMOR_MODIFIER_UUID.toString(),
                10.0D,
                AttributeModifier.Operation.ADDITION
        );
    }

    @Override
    public void applyEffectTick(LivingEntity entity, int amplifier) {
        if (!entity.level().isClientSide() && entity.level() instanceof ServerLevel serverLevel) {

            var effectInstance = entity.getEffect(this);
            if (effectInstance != null) {
                int duration = effectInstance.getDuration();

                double x = entity.getX();
                double y = entity.getY();
                double z = entity.getZ();

                if (entity.tickCount % 4 == 0) {
                    serverLevel.sendParticles(
                            ParticleRegistry.CLEANSE_PARTICLE.get(),
                            x, y + (entity.getBbHeight() / 2.0), z,
                            3,
                            0.3, 0.4, 0.3,
                            0.01
                    );
                }

                if (duration == 1) {
                    entity.heal(HEAL_ON_EXPIRE);


                    serverLevel.sendParticles(
                            ParticleTypes.TOTEM_OF_UNDYING,
                            x, y + 1.0, z,
                            25,
                            0.5, 0.5, 0.5,
                            0.15
                    );
                }
            }
        }
    }

    @Override
    public boolean isDurationEffectTick(int duration, int amplifier) {
        // CHANGED: Must return true every tick so applyEffectTick runs constantly to render particles
        return true;
    }
}
