package net.rev.roacw.registeries;

import com.github.alexmodguy.alexscaves.server.potion.ACEffectRegistry;
import net.acetheeldritchking.cataclysm_spellbooks.registries.CSPotionEffectRegistry;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.food.FoodProperties;

public class ROACWFood {
    public static final FoodProperties FRIED_CHICKEN = (new FoodProperties.Builder()).nutrition(4).saturationMod(1.2F).effect(new MobEffectInstance(MobEffects.REGENERATION, 400, 1), 1.0F).effect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 6000, 0), 1.0F).effect(new MobEffectInstance(MobEffects.FIRE_RESISTANCE, 6000, 0), 1.0F).effect(new MobEffectInstance(MobEffects.ABSORPTION, 2400, 3), 1.0F).effect(new MobEffectInstance(CSPotionEffectRegistry.WRATHFUL.get(), 3600, 3), 1.0F).effect(new MobEffectInstance(CSPotionEffectRegistry.SNIPER_EFFECT.get(), 3600, 3), 1.0F).effect(new MobEffectInstance(ACEffectRegistry.RAGE.get(), 3600, 3), 1.0F).effect(new MobEffectInstance(ACEffectRegistry.SUGAR_RUSH.get(), 3600, 3), 1.0F).effect(new MobEffectInstance(MobEffects.SATURATION, 3600, 3), 1.0F).alwaysEat().build();

}
