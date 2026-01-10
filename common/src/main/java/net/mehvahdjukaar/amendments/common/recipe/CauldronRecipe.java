package net.mehvahdjukaar.amendments.common.recipe;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.mehvahdjukaar.amendments.reg.ModRegistry;
import net.mehvahdjukaar.moonlight.api.fluids.SoftFluidStack;
import net.mehvahdjukaar.moonlight.api.util.codec.BiggerStreamCodecs;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.entity.player.StackedContents;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;

import java.util.Optional;
import java.util.function.Function;

public class CauldronRecipe implements Recipe<CauldronCraftingContainer> {
    private final String group;
    private final NonNullList<Ingredient> inputItems;
    private final SoftFluidIngredient inputFluid; //amount is unused

    private final Optional<SoftFluidIngredient> outputFluid; //amount is unused
    private final ItemStack outputItem;

    private final boolean requireBoiling; //if true, recipe can only be crafted in boiling cauldron

    private final int fluidAmountDifference; //amount difference from before and after crafting

    protected CauldronRecipe(String group, SoftFluidIngredient inputFluid,
                             NonNullList<Ingredient> inputItems, Optional<SoftFluidIngredient> outputFluid, ItemStack outputItem,
                             int fluidAmountDifference, boolean requireBoiling) {
        this.group = group;
        this.inputItems = inputItems;
        this.inputFluid = inputFluid;
        this.outputItem = outputItem;
        this.outputFluid = outputFluid;
        this.fluidAmountDifference = fluidAmountDifference;
        this.requireBoiling = requireBoiling;
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
    public ItemStack getResultItem(HolderLookup.Provider registries) {
        return outputItem;
    }

    @Override
    public NonNullList<Ingredient> getIngredients() {
        return this.inputItems;
    }

    @Override
    public boolean matches(CauldronCraftingContainer inv, Level level) {
        if (this.requireBoiling && !inv.isBoiling()) {
            return false; //if recipe requires boiling, but cauldron is not boiling
        }
        StackedContents stackedContents = new StackedContents();
        int i = 0;

        for (int j = 0; j < inv.size(); j++) {
            ItemStack itemStack = inv.getItem(j);
            if (!itemStack.isEmpty()) {
                i++;
                stackedContents.accountStack(itemStack, 1);
            }
        }
        SoftFluidStack tankFluid = inv.getFluid();
        int newCount = tankFluid.getCount() + this.fluidAmountDifference;
        if (!this.inputFluid.matches(tankFluid) || newCount < 0 || newCount > inv.getMaxAllowedFluidCount()) {
            return false;
        }

        return i == this.inputItems.size() && stackedContents.canCraft(this, null);
    }

    @Deprecated
    @Override
    public ItemStack assemble(CauldronCraftingContainer input, HolderLookup.Provider registries) {
        return getResultItem(registries);
    }

    public FluidAndItemCraftResult assembleFluid(CauldronCraftingContainer container, HolderLookup.Provider registries) {
        SoftFluidStack tankFluid = container.getFluid();

        ItemStack craftedItem = outputItem.copy();
        var outputFluidStack = this.outputFluid.map(SoftFluidIngredient::createStack).orElseGet(() -> SoftFluidStack.empty(registries));
        SoftFluidStack newTankFluid = outputFluidStack.isEmpty() ? tankFluid : outputFluidStack;
        newTankFluid.setCount(tankFluid.getCount() + fluidAmountDifference);

        return FluidAndItemCraftResult.of(craftedItem, newTankFluid);
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return width * height >= this.inputItems.size();
    }

    private static final Codec<NonNullList<Ingredient>> ING_LIST_CODEC = Ingredient.CODEC_NONEMPTY.listOf()
            .flatXmap((list) -> {
                Ingredient[] ingredients = list.stream().filter((ingredient) -> !ingredient.isEmpty()).toArray(Ingredient[]::new);
                if (ingredients.length == 0) {
                    return DataResult.error(() -> "No ingredients for cauldron recipe");
                } else {
                    return DataResult.success(NonNullList.of(Ingredient.EMPTY, ingredients));
                }
            }, DataResult::success);

    public static class Serializer implements RecipeSerializer<CauldronRecipe> {

        public static final MapCodec<CauldronRecipe> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                Codec.STRING.fieldOf("group").forGetter(recipe -> recipe.group),
                SoftFluidIngredient.CODEC.fieldOf("input_fluid").forGetter(recipe -> recipe.inputFluid),
                ING_LIST_CODEC.fieldOf("input_items").forGetter(recipe -> recipe.inputItems),
                SoftFluidIngredient.CODEC.optionalFieldOf("output_fluid").forGetter(recipe -> recipe.outputFluid),
                ItemStack.CODEC.optionalFieldOf("output_item", ItemStack.EMPTY).forGetter(recipe -> recipe.outputItem),
                Codec.INT.optionalFieldOf("fluid_amount_difference", 0).forGetter(recipe -> recipe.fluidAmountDifference),
                Codec.BOOL.optionalFieldOf("require_boiling", false).orElse(false).forGetter(recipe -> recipe.requireBoiling)
        ).apply(instance, CauldronRecipe::new));

        public static final StreamCodec<RegistryFriendlyByteBuf, CauldronRecipe> STREAM_CODEC = BiggerStreamCodecs.composite(
                ByteBufCodecs.STRING_UTF8, CauldronRecipe::getGroup,
                SoftFluidIngredient.STREAM_CODEC, r -> r.inputFluid,
                Ingredient.CONTENTS_STREAM_CODEC.apply(ByteBufCodecs.list())
                        .map(l -> NonNullList.of(Ingredient.EMPTY, l.toArray(new Ingredient[0])), Function.identity()),
                r -> r.inputItems,
                ByteBufCodecs.optional(SoftFluidIngredient.STREAM_CODEC), r -> r.outputFluid,
                ItemStack.STREAM_CODEC, r -> r.outputItem,
                ByteBufCodecs.VAR_INT, r -> r.fluidAmountDifference,
                ByteBufCodecs.BOOL, r -> r.requireBoiling,
                CauldronRecipe::new
        );

        @Override
        public MapCodec<CauldronRecipe> codec() {
            return CODEC;
        }

        @Override
        public StreamCodec<RegistryFriendlyByteBuf, CauldronRecipe> streamCodec() {
            return STREAM_CODEC;
        }
    }
}