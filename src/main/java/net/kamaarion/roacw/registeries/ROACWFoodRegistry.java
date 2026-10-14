package net.kamaarion.roacw.registeries;

import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.food.FoodProperties;

public class ROACWFoodRegistry {
    public static final FoodProperties FRIED_CHICKEN =
            new FoodProperties.Builder()
                    .nutrition(4)
                    .saturationMod(1.2F)
                    .effect(
                            new MobEffectInstance(
                                    ROACWEffectRegistry.INDULGENCE.get(),
                                    400,
                                    0
                            ),
                            1.0F
                    )
                    .alwaysEat()
                    .build();
}