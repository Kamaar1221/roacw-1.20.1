package net.kamaarion.roacw.effects;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

import java.util.List;

public class AuricExhaustionEffect extends MobEffect {
    public AuricExhaustionEffect() {
        super(MobEffectCategory.HARMFUL, 9722673);
    }

    public void m_6742_(LivingEntity LivingEntityIn, int amplifier) {
    }

    public boolean m_6584_(int duration, int amplifier) {
        int k = 50 >> amplifier;
        if (k > 0) {
            return duration % k == 0;
        } else {
            return true;
        }
    }

    public List<ItemStack> getCurativeItems() {
        return List.of();
    }
}