package net.kamaarion.roacw.effects;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class AuricExhaustionEffect extends MobEffect {
    public AuricExhaustionEffect() {
        super(MobEffectCategory.HARMFUL, 9722673);
    }

    @Override
    public List<ItemStack> getCurativeItems() {
        // Returning an empty list means nothing can cure this effect early
        return new ArrayList<>();
    }
}