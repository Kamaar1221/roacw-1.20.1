package net.kamaarion.roacw.recipes;

import com.google.gson.JsonObject;
import net.kamaarion.roacw.items.curios.wings_of_rebirth.WingsOfRebirth;
import net.kamaarion.roacw.registeries.ROACWRecipeRegistry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.SmithingRecipe;
import net.minecraft.world.level.Level;

public class WingsReviveSmithingRecipe implements SmithingRecipe {
    private final ResourceLocation id;
    private final Ingredient base;
    private final Ingredient addition;

    public WingsReviveSmithingRecipe(ResourceLocation id, Ingredient base, Ingredient addition) {
        this.id = id;
        this.base = base;
        this.addition = addition;
    }

    @Override
    public boolean matches(Container container, Level level) {
        ItemStack wings = container.getItem(1);
        return container.getItem(0).isEmpty()
                && base.test(wings)
                && !WingsOfRebirth.isCharged(wings)
                && addition.test(container.getItem(2));
    }

    @Override
    public ItemStack assemble(Container container, RegistryAccess access) {
        ItemStack result = container.getItem(1).copy();
        WingsOfRebirth.setCharged(result, true);
        return result;
    }

    @Override
    public ItemStack getResultItem(RegistryAccess access) {
        ItemStack[] options = base.getItems();
        ItemStack result = options.length > 0 ? options[0].copy() : ItemStack.EMPTY;
        if (!result.isEmpty()) WingsOfRebirth.setCharged(result, true);
        return result;
    }

    @Override
    public boolean isTemplateIngredient(ItemStack stack) {
        return stack.isEmpty();
    }

    @Override
    public boolean isBaseIngredient(ItemStack stack) {
        return base.test(stack);
    }

    @Override
    public boolean isAdditionIngredient(ItemStack stack) {
        return addition.test(stack);
    }

    @Override
    public boolean isSpecial() {
        return true;
    }

    @Override
    public ResourceLocation getId() {
        return id;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return ROACWRecipeRegistry.WINGS_REVIVE_SMITHING.get();
    }

    public static class Serializer implements RecipeSerializer<WingsReviveSmithingRecipe> {
        @Override
        public WingsReviveSmithingRecipe fromJson(ResourceLocation id, JsonObject json) {
            Ingredient base = Ingredient.fromJson(GsonHelper.getNonNull(json, "base"));
            Ingredient addition = Ingredient.fromJson(GsonHelper.getNonNull(json, "addition"));
            return new WingsReviveSmithingRecipe(id, base, addition);
        }

        @Override
        public WingsReviveSmithingRecipe fromNetwork(ResourceLocation id, FriendlyByteBuf buf) {
            return new WingsReviveSmithingRecipe(id, Ingredient.fromNetwork(buf), Ingredient.fromNetwork(buf));
        }

        @Override
        public void toNetwork(FriendlyByteBuf buf, WingsReviveSmithingRecipe recipe) {
            recipe.base.toNetwork(buf);
            recipe.addition.toNetwork(buf);
        }
    }
}