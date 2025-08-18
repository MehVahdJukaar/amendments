package net.mehvahdjukaar.amendments.common.recipe;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.mojang.serialization.JsonOps;
import net.mehvahdjukaar.amendments.reg.ModRegistry;
import net.mehvahdjukaar.moonlight.api.fluids.SoftFluidStack;
import net.mehvahdjukaar.moonlight.api.util.Utils;
import net.minecraft.core.NonNullList;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.RegistryOps;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.entity.player.StackedContents;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;

public class CauldronRecipe implements Recipe<CauldronCraftingContainer> {
    private final ResourceLocation id;
    private final String group;
    private final NonNullList<Ingredient> inputItems;
    private final SoftFluidIngredient inputFluid; //amount is unused

    private final SoftFluidIngredient outputFluid; //amount is unused
    private final ItemStack outputItem;

    private final int fluidAmountDifference; //amount difference from before and after crafting

    protected CauldronRecipe(ResourceLocation id, String group, SoftFluidIngredient inputFluid,
                             NonNullList<Ingredient> inputItems, SoftFluidIngredient outputFluid, ItemStack outputItem,
                             int fluidAmountDifference) {
        this.id = id;
        this.group = group;
        this.inputItems = inputItems;
        this.inputFluid = inputFluid;
        this.outputItem = outputItem;
        this.outputFluid = outputFluid;
        this.fluidAmountDifference = fluidAmountDifference;
    }

    @Override
    public ResourceLocation getId() {
        return this.id;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return ModRegistry.CAULDRON_RECIPE_SERIALIZER.get();
    }

    @Override
    public RecipeType<?> getType() {
        return ModRegistry.CAULDRON_RECIPE_TYPE.get();
    }

    @Override
    public String getGroup() {
        return this.group;
    }

    @Override
    public ItemStack getResultItem(RegistryAccess registryAccess) {
        return outputItem;
    }

    @Override
    public NonNullList<Ingredient> getIngredients() {
        return this.inputItems;
    }

    @Override
    public boolean matches(CauldronCraftingContainer inv, Level level) {
        StackedContents stackedContents = new StackedContents();
        int i = 0;

        for (int j = 0; j < inv.getContainerSize(); j++) {
            ItemStack itemStack = inv.getItem(j);
            if (!itemStack.isEmpty()) {
                i++;
                stackedContents.accountStack(itemStack, 1);
            }
        }
        SoftFluidStack tankFluid = inv.getFluid();
        int newCount = tankFluid.getCount() + this.fluidAmountDifference;
        if (!this.inputFluid.matches(tankFluid) && newCount >= 0 && newCount <= inv.getFluidContainerSize()) {
            return false;
        }

        return i == this.inputItems.size() && stackedContents.canCraft(this, null);
    }

    @Deprecated
    @Override
    public ItemStack assemble(CauldronCraftingContainer container, RegistryAccess registryAccess) {
        return getResultItem(registryAccess);
    }

    public FluidAndItemCraftResult assembleFluid(CauldronCraftingContainer container, RegistryAccess registryAccess) {
        SoftFluidStack tankFluid = container.getFluid();

        ItemStack craftedItem = outputItem.copy();
        SoftFluidStack newTankFluid = outputFluid.isEmpty() ? tankFluid : outputFluid.createStack();
        newTankFluid.setCount(tankFluid.getCount());

        return FluidAndItemCraftResult.of(craftedItem, newTankFluid);
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return width * height >= this.inputItems.size();
    }

    public static class Serializer implements RecipeSerializer<CauldronRecipe> {

        @Override
        public CauldronRecipe fromJson(ResourceLocation recipeId, JsonObject json) {
            String group = GsonHelper.getAsString(json, "group", "");
            NonNullList<Ingredient> inputItems = itemsFromJson(GsonHelper.getAsJsonArray(json, "input_items"));
            if (inputItems.isEmpty()) {
                throw new JsonParseException("No ingredients for shapeless recipe");
            } else {
                var ops = RegistryOps.create(JsonOps.INSTANCE, Utils.hackyGetRegistryAccess());
                SoftFluidStack inputFluid = SoftFluidStack.CODEC.decode(ops, GsonHelper.getAsJsonObject(json, "input_fluid"))
                        .getOrThrow(false, (s) -> {
                        }).getFirst();
                var fiJson = GsonHelper.getAsJsonObject(json, "output_fluid", null);
                SoftFluidStack outputFluid = fiJson == null ? SoftFluidStack.empty() :
                        SoftFluidStack.CODEC.decode(ops, fiJson)
                                .getOrThrow(false, (s) -> {
                                }).getFirst();
                var oiJson = GsonHelper.getAsJsonObject(json, "output_item", null);
                ItemStack outputItem = oiJson == null ? ItemStack.EMPTY : ItemStack.CODEC.decode(ops, oiJson)
                        .getOrThrow(false, s -> {
                        })
                        .getFirst();
                int fluidDifference = GsonHelper.getAsInt(json, "fluid_amount_difference", 0);
                return new CauldronRecipe(recipeId, group, SoftFluidIngredient.containing(inputFluid), inputItems,
                        SoftFluidIngredient.containing(outputFluid), outputItem, fluidDifference);
            }
        }

        private static NonNullList<Ingredient> itemsFromJson(JsonArray ingredientArray) {
            NonNullList<Ingredient> nonNullList = NonNullList.create();

            for (int i = 0; i < ingredientArray.size(); i++) {
                Ingredient ingredient = Ingredient.fromJson(ingredientArray.get(i), false);
                if (!ingredient.isEmpty()) {
                    nonNullList.add(ingredient);
                }
            }

            return nonNullList;
        }

        @Override
        public CauldronRecipe fromNetwork(ResourceLocation recipeId, FriendlyByteBuf buffer) {
            String string = buffer.readUtf();
            int i = buffer.readVarInt();

            NonNullList<Ingredient> inputItems = NonNullList.withSize(i, Ingredient.EMPTY);
            inputItems.replaceAll(ignored -> Ingredient.fromNetwork(buffer));

            SoftFluidIngredient inputFluid = SoftFluidIngredient.loadFromBuffer(buffer);
            SoftFluidIngredient outputIFluid = SoftFluidIngredient.loadFromBuffer(buffer);
            ItemStack outputItem = buffer.readItem();
            int differenceAmount = buffer.readVarInt();

            return new CauldronRecipe(recipeId, string, inputFluid, inputItems, outputIFluid, outputItem, differenceAmount);
        }

        @Override
        public void toNetwork(FriendlyByteBuf buffer, CauldronRecipe recipe) {
            buffer.writeUtf(recipe.group);
            buffer.writeVarInt(recipe.inputItems.size());

            for (Ingredient ingredient : recipe.inputItems) {
                ingredient.toNetwork(buffer);
            }

            recipe.outputFluid.saveToBuffer(buffer);
            recipe.inputFluid.saveToBuffer(buffer);
            buffer.writeItem(recipe.outputItem);
            buffer.writeVarInt(recipe.fluidAmountDifference);

        }
    }
}