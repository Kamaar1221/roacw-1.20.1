package net.kamaarion.roacw.events;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.LivingEntity;

public final class EffectStackAPI {

    private EffectStackAPI() {
    }

    public static void addStack(
            LivingEntity entity,
            String stackKey,
            int maxStacks
    ) {
        addStacks(entity, stackKey, 1, maxStacks);
    }

    public static void addStacks(
            LivingEntity entity,
            String stackKey,
            int amount,
            int maxStacks
    ) {
        if (entity == null || stackKey == null || stackKey.isEmpty()) {
            return;
        }

        if (amount <= 0 || maxStacks <= 0) {
            return;
        }

        int currentStacks = getStacks(entity, stackKey);

        int newStacks = Math.min(
                currentStacks + amount,
                maxStacks
        );

        setStacks(entity, stackKey, newStacks);
    }

    public static int getStacks(
            LivingEntity entity,
            String stackKey
    ) {
        if (entity == null || stackKey == null || stackKey.isEmpty()) {
            return 0;
        }

        CompoundTag data = entity.getPersistentData();

        return Math.max(
                0,
                data.getInt(stackKey)
        );
    }

    public static void setStacks(
            LivingEntity entity,
            String stackKey,
            int stacks
    ) {
        if (entity == null || stackKey == null || stackKey.isEmpty()) {
            return;
        }

        if (stacks <= 0) {
            removeStacks(entity, stackKey);
            return;
        }

        entity.getPersistentData().putInt(
                stackKey,
                stacks
        );
    }

    public static void setStacks(
            LivingEntity entity,
            String stackKey,
            int stacks,
            int maxStacks
    ) {
        if (entity == null || stackKey == null || stackKey.isEmpty()) {
            return;
        }

        if (stacks <= 0 || maxStacks <= 0) {
            removeStacks(entity, stackKey);
            return;
        }

        entity.getPersistentData().putInt(
                stackKey,
                Math.min(stacks, maxStacks)
        );
    }

    public static void removeStacks(
            LivingEntity entity,
            String stackKey,
            int amount
    ) {
        if (entity == null || stackKey == null || stackKey.isEmpty()) {
            return;
        }

        if (amount <= 0) {
            return;
        }

        int currentStacks = getStacks(entity, stackKey);

        setStacks(
                entity,
                stackKey,
                Math.max(0, currentStacks - amount)
        );
    }

    public static void removeStacks(
            LivingEntity entity,
            String stackKey
    ) {
        if (entity == null || stackKey == null || stackKey.isEmpty()) {
            return;
        }

        entity.getPersistentData().remove(stackKey);
    }

    public static boolean hasStacks(
            LivingEntity entity,
            String stackKey
    ) {
        return getStacks(entity, stackKey) > 0;
    }

    public static boolean hasMaxStacks(
            LivingEntity entity,
            String stackKey,
            int maxStacks
    ) {
        if (maxStacks <= 0) {
            return false;
        }

        return getStacks(entity, stackKey) >= maxStacks;
    }

    public static int getRemainingStacks(
            LivingEntity entity,
            String stackKey,
            int maxStacks
    ) {
        if (maxStacks <= 0) {
            return 0;
        }

        return Math.max(
                0,
                maxStacks - getStacks(entity, stackKey)
        );
    }

    public static float getStackProgress(
            LivingEntity entity,
            String stackKey,
            int maxStacks
    ) {
        if (maxStacks <= 0) {
            return 0.0F;
        }

        return Math.min(
                1.0F,
                (float) getStacks(entity, stackKey)
                        / (float) maxStacks
        );
    }
}