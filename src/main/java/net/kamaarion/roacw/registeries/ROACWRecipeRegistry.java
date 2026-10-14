package net.kamaarion.roacw.registeries;

import net.kamaarion.roacw.ROACW;
import net.kamaarion.roacw.recipes.WingsReviveSmithingRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ROACWRecipeRegistry {
    public static final DeferredRegister<RecipeSerializer<?>> RECIPE_SERIALIZERS =
            DeferredRegister.create(ForgeRegistries.RECIPE_SERIALIZERS, ROACW.MODID);

    public static final RegistryObject<RecipeSerializer<WingsReviveSmithingRecipe>> WINGS_REVIVE_SMITHING =
            RECIPE_SERIALIZERS.register("wings_revive_smithing", WingsReviveSmithingRecipe.Serializer::new);

    public static void register(IEventBus eventBus) {
        RECIPE_SERIALIZERS.register(eventBus);
    }
}