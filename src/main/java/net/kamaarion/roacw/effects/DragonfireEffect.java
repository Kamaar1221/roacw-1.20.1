package net.kamaarion.roacw.effects;

import io.redspace.ironsspellbooks.registries.ParticleRegistry;
import net.kamaarion.roacw.events.EffectStackAPI;
import net.kamaarion.roacw.registeries.ROACWEffectRegistry;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.AABB;

import java.util.List;

public class DragonfireEffect extends MobEffect {

    public static final String STACK_KEY = "ROACW_DragonfireStacks";
    public static final int MAX_STACKS = 10;
    public static final int EFFECT_DURATION = 300;

    private static final float SELF_DAMAGE = 25.0F;
    private static final float AREA_DAMAGE = 10.0F;
    private static final double EXPLOSION_RADIUS = 6.0D;

    public DragonfireEffect() {
        super(
                MobEffectCategory.HARMFUL,
                0xFF4500
        );
    }

    public static void addStack(LivingEntity entity) {
        addStacks(entity, 1);
    }

    public static void addStacks(LivingEntity entity, int amount) {
        if (entity == null || amount <= 0) {
            return;
        }

        EffectStackAPI.addStacks(
                entity,
                STACK_KEY,
                amount,
                MAX_STACKS
        );

        int stacks = getStacks(entity);

        if (stacks <= 0) {
            return;
        }

        entity.addEffect(
                new MobEffectInstance(
                        ROACWEffectRegistry.DRAGONFIRE.get(),
                        EFFECT_DURATION,
                        stacks - 1,
                        false,
                        false,
                        true
                )
        );
    }

    public static int getStacks(LivingEntity entity) {
        return EffectStackAPI.getStacks(
                entity,
                STACK_KEY
        );
    }

    public static void removeStacks(LivingEntity entity) {
        EffectStackAPI.removeStacks(
                entity,
                STACK_KEY
        );
    }

    public static void clearDragonfire(LivingEntity entity) {
        removeStacks(entity);
        entity.removeEffect(ROACWEffectRegistry.DRAGONFIRE.get());
    }

    @Override
    public void applyEffectTick(LivingEntity entity, int amplifier) {
        if (entity.level().isClientSide) {
            return;
        }

        ServerLevel level = (ServerLevel) entity.level();

        level.sendParticles(
                ParticleRegistry.DRAGON_FIRE_PARTICLE.get(),
                entity.getX(),
                entity.getY() + entity.getBbHeight() * 0.5D,
                entity.getZ(),
                2,
                entity.getBbWidth() * 0.4D,
                entity.getBbHeight() * 0.4D,
                entity.getBbWidth() * 0.4D,
                0.02D
        );

        if (EffectStackAPI.hasMaxStacks(
                entity,
                STACK_KEY,
                MAX_STACKS
        )) {
            detonate(entity, level);
        }
    }

    @Override
    public boolean isDurationEffectTick(int duration, int amplifier) {
        return true;
    }

    private static void detonate(LivingEntity source, ServerLevel level) {
        double x = source.getX();
        double y = source.getY() + source.getBbHeight() * 0.5D;
        double z = source.getZ();

        level.sendParticles(
                ParticleTypes.FLAME,
                x,
                y,
                z,
                100,
                1.0D,
                1.0D,
                1.0D,
                0.15D
        );

        level.sendParticles(
                ParticleTypes.LAVA,
                x,
                y,
                z,
                40,
                1.0D,
                1.0D,
                1.0D,
                0.1D
        );

        level.playSound(
                null,
                x,
                y,
                z,
                SoundEvents.GENERIC_EXPLODE,
                SoundSource.HOSTILE,
                2.0F,
                0.7F
        );

        source.hurt(
                source.damageSources().inFire(),
                SELF_DAMAGE
        );

        AABB area = source.getBoundingBox().inflate(EXPLOSION_RADIUS);

        List<LivingEntity> nearbyEntities = level.getEntitiesOfClass(
                LivingEntity.class,
                area,
                entity -> entity != source && entity.isAlive()
        );

        for (LivingEntity target : nearbyEntities) {
            target.hurt(
                    source.damageSources().inFire(),
                    AREA_DAMAGE
            );

            double dx = target.getX() - source.getX();
            double dz = target.getZ() - source.getZ();

            double distance = Math.sqrt(dx * dx + dz * dz);

            if (distance > 0.001D) {
                dx /= distance;
                dz /= distance;

                target.push(
                        dx * 1.5D,
                        0.5D,
                        dz * 1.5D
                );
            }
        }

        clearDragonfire(source);
    }
}