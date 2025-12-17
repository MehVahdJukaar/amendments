package net.mehvahdjukaar.amendments.common.recipe;


import com.google.common.base.Preconditions;
import com.google.common.collect.Multimap;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.mojang.serialization.JsonOps;
import net.mehvahdjukaar.amendments.reg.ModRegistry;
import net.mehvahdjukaar.moonlight.api.fluids.*;
import net.minecraft.Util;
import net.minecraft.core.NonNullList;
import net.minecraft.util.Mth;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.List;

public class CauldronCraftingContainer implements RecipeInput{

    private final List<ItemStack> originalItems;
    private final NonNullList<ItemStack> items;
    private final SoftFluidStack fluid;
    private final int fluidContainerSize;
    private final Multimap<FluidContainerList.Category, ItemStack> equivalentFluidContainers;
    private final int dimension;
    private final int fluidPosition;
    private final boolean isBoiling;

    private CauldronCraftingContainer(int fluidContainerSize, SoftFluidStack fluidStack, Collection<ItemStack> items,
                                      int fluidPosition, boolean isBoiling) {
        this.dimension = Mth.ceil(Math.sqrt(items.size() + 1));
        this.originalItems = List.copyOf(items);
        this.items = NonNullList.withSize((dimension * dimension), ItemStack.EMPTY);
        this.fluid = fluidStack;
        //fill all containers that it can fill.
        this.equivalentFluidContainers = fluidStack.toAllPossibleFilledItems();
        if (fluidStack.is(MLBuiltinSoftFluids.WATER)) {
            equivalentFluidContainers.put(DUMMY_WATER_BOWL_CATEGORY, Items.BOWL.getDefaultInstance());
        }
        this.fluidPosition = fluidPosition;
        Preconditions.checkArgument(fluidPosition <= items.size());
        this.fluidContainerSize = fluidContainerSize;
        this.isBoiling = isBoiling;
    }

    private static final FluidContainerList.Category DUMMY_WATER_BOWL_CATEGORY = Util.make(() -> {
        JsonElement j = JsonParser.parseString(
                """
                        {
                            "capacity": """ + SoftFluid.BOWL_COUNT + "," + """
                                "empty": "minecraft:bowl",
                                "filled": [
                                "minecraft:mushroom_stew"
                                ]
                            }
                        """);

        return FluidContainerList.Category.CODEC.decode(JsonOps.INSTANCE, j).getOrThrow().getFirst();
    });

    //hack
    public static CauldronCraftingContainer surround8(boolean boiling, int fluidContainerSize, SoftFluidStack fluid, ItemStack item) {
        /*4*/
        return new CauldronCraftingContainer(fluidContainerSize, fluid, List.of(
                item, item, item,
                item,/*4*/ item,
                item, item, item
        ), 4, boiling);
    }

    public static CauldronCraftingContainer of(boolean boiling, int fluidContainerSize, SoftFluidStack fluid, ItemStack... items) {
        return new CauldronCraftingContainer(fluidContainerSize, fluid, List.of(items), items.length, boiling);
    }

    public static CauldronCraftingContainer of(boolean boiling, int fluidContainerSize, SoftFluidStack fluid, Collection<ItemStack> items) {
        return new CauldronCraftingContainer(fluidContainerSize, fluid, items, items.size(), boiling);
    }

    @Override
    public int size() {
        return items.size();
    }

    @Override
    public boolean isEmpty() {
        return items.isEmpty();
    }

    @Override
    public ItemStack getItem(int slot) {
        if (slot >= this.size()) return ItemStack.EMPTY;
        return items.get(slot);
    }

    public SoftFluidStack getFluid() {
        return fluid;
    }

    public int getMaxAllowedFluidCount() {
        return fluidContainerSize;
    }

    protected CraftingInput makeCraftingInput() {
        return CraftingInput.of(this.dimension, this.dimension, this.items);
    }

    @Nullable
    public FluidAndItemCraftResult craftWithCauldronRecipes(Level level) {
        for (int j = 0; j < this.originalItems.size(); j++) {
            this.items.set(j, this.originalItems.get(j));
        }
        List<RecipeHolder<CauldronRecipe>> recipes = level.getRecipeManager()
                .getRecipesFor(ModRegistry.CAULDRON_RECIPE_TYPE.get(), this, level);
        for (var h : recipes) {
            CauldronRecipe r = h.value();
            if (!r.matches(this, level)) continue;
            FluidAndItemCraftResult resultFluid = r.assembleFluid(this, level.registryAccess());
            var remainingItems = r.getRemainingItems(this);
            //doesnt support remaining items
            if (remainingItems.stream().allMatch(ItemStack::isEmpty)) {
                //is this correct?
                return resultFluid;
            }
        }
        return null;
    }


    @Nullable
    public FluidAndItemCraftResult craftWithCraftingRecipes(Level level) {
        for (var cont : equivalentFluidContainers.entries()) {
            var category = cont.getKey();
            ItemStack fluidInBottle = cont.getValue();
            setupFluidItem(fluidInBottle);
            CraftingInput input = this.makeCraftingInput();
            List<RecipeHolder<CraftingRecipe>> recipes = level.getRecipeManager().getRecipesFor(RecipeType.CRAFTING, input, level);
            for (var h : recipes) {
                CraftingRecipe r = h.value();
                if (!r.matches(input, level)) continue;
                int newFluidCount = fluid.getCount() - category.getCapacity();
                if (newFluidCount < 0 || newFluidCount > fluidContainerSize) continue;
                ItemStack craftedItem = r.assemble(input, level.registryAccess());
                if (!craftedItem.isEmpty()) {
                    var remainingItems = r.getRemainingItems(input);
                    //is this correct?
                    Item emptyContainer = category.getEmptyContainer();

                    if (remainingItems.stream().allMatch(ItemStack::isEmpty)) {
                        if(fluidInBottle.is(Items.LINGERING_POTION)){
                            return FluidAndItemCraftResult.of(craftedItem, fluid.copyWithCount(newFluidCount));
                        }
                        ///aaa lingering pots dont give back a bottle
                        var equivalentFluid = SoftFluidStack.fromItem(craftedItem, level.registryAccess());
                        if (equivalentFluid != null) {
                            FluidContainerList.Category catt = equivalentFluid.getSecond();
                            if (catt.getEmptyContainer() == emptyContainer) {
                                SoftFluidStack f = equivalentFluid.getFirst();
                                //yes this will give some fluid for free
                                return FluidAndItemCraftResult.of(ItemStack.EMPTY,
                                        f.copyWithCount(this.fluid.getCount()));
                            }
                        }
                    } else if (remainingItems.stream().allMatch(i -> i.isEmpty() || i.getItem() == emptyContainer)) {
                        return FluidAndItemCraftResult.of(craftedItem, fluid.copyWithCount(newFluidCount));
                    }
                }
            }

        }
        return null;
    }

    private void setupFluidItem(ItemStack filledFluidBottle) {
        this.items.clear();
        int j = 0;
        for (ItemStack item : this.originalItems) {
            if (j == fluidPosition) {
                j++;
            }
            this.items.set(j, item);
            j++;
        }
        this.items.set(fluidPosition, filledFluidBottle);
    }


    public boolean isBoiling() {
        return isBoiling;
    }
}
