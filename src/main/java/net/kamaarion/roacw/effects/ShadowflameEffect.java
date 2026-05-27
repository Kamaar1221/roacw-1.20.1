package net.kamaarion.roacw.effects;

import net.kamaarion.roacw.registeries.ROACWEffectRegistry;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.event.entity.living.LivingHealEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.event.ForgeEventFactory;

public class ShadowflameEffect extends MobEffect {
    public ShadowflameEffect() {
        super(MobEffectCategory.HARMFUL, 9722673);
    }
    public static final float HEALING_PER_LEVEL = -.20f;

    public ShadowflameEffect(MobEffectCategory pCategory, int pColor) {
        super(pCategory, pColor);
    }

    @SubscribeEvent
    public static void reduceHealing(LivingHealEvent event) {
        var effect = event.getEntity().getEffect(ROACWEffectRegistry.SHADOWFLAME.get());
        if (effect != null) {
            int lvl = effect.getAmplifier() + 1;
            float healingMult = 1 + HEALING_PER_LEVEL * lvl;
            float before = event.getAmount();
            event.setAmount(event.getAmount() * healingMult);
            //IronsSpellbooks.LOGGER.debug("BlightEffect.reduceHealing: {}->{}", before, event.getAmount());

        }
    }
}
