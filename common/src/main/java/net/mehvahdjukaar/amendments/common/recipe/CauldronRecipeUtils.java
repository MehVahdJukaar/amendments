package net.mehvahdjukaar.amendments.common.recipe;

import net.mehvahdjukaar.moonlight.api.fluids.BuiltInSoftFluids;
import net.mehvahdjukaar.moonlight.api.fluids.MLBuiltinSoftFluids;
import net.mehvahdjukaar.moonlight.api.fluids.SoftFluidStack;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.List;

public class CauldronRecipeUtils {

    //like below but craft as many as possible
    @Nullable
    public static FluidAndItemsCraftResult craftMultiple(Level level, boolean boiling, int tankCapacity, SoftFluidStack currentFluid, List<ItemStack> inputItems) {
        boolean success = false;
        CompactItemSet craftedItems = new CompactItemSet();
        FluidAndItemCraftResult currentResult;
        //craft all that it can
        do {
            currentResult = CauldronRecipeUtils.craft(level, boiling, tankCapacity, currentFluid, inputItems);
            if (currentResult != null) {
                success = true;
                currentFluid = currentResult.resultFluid();
                craftedItems.add(currentResult.craftedItem());
            }

        } while (currentResult != null);
        if (!success) return null;
        return FluidAndItemsCraftResult.of(craftedItems.toList(), currentFluid);
    }

    //also alters the input item states
    //decrements and uses input items
    //returns produced item and fluid to replace input fluid
    @Nullable
    public static FluidAndItemCraftResult craft(Level level, boolean boiling, int tankCapacity,
                                                SoftFluidStack fluidStack, List<ItemStack> items) {
        if (fluidStack.isEmpty()) return null;

        FluidAndItemCraftResult crafted = craftFluidSpecial(level, boiling, tankCapacity, fluidStack, items);
        if (crafted != null) return crafted;

        //can only craft crafting table stuff when its boiling
        if (boiling || !fluidStack.is(MLBuiltinSoftFluids.WATER)) {
            crafted = craftItemSingle(level, true, tankCapacity, fluidStack, items);
            if (crafted != null) return crafted;

            if (items.size() == 1) {
                crafted = craftItemSurround(level, true, tankCapacity, fluidStack, items.get(0));
                return crafted;
            }
        }

        return null;
    }


    @Nullable
    private static FluidAndItemCraftResult craftItemSingle(Level level, boolean boiling, int tankCapacity, SoftFluidStack fluid, Collection<ItemStack> item) {
        CauldronCraftingContainer container = CauldronCraftingContainer.of(boiling, tankCapacity, fluid, item);
        FluidAndItemCraftResult result = container.craftWithCraftingRecipes(level);
        if (result != null) {
            for (var i : item) {
                if (!i.isEmpty()) {
                    i.shrink(1);
                }
            }
            return result;
        } else {
            return null;
        }
    }

    @Nullable
    private static FluidAndItemCraftResult craftItemSurround(Level level, boolean boiling, int tankCapacity, SoftFluidStack fluid, ItemStack item) {
        if (item.getCount() < 8) return null;
        CauldronCraftingContainer container = CauldronCraftingContainer.surround8(boiling, tankCapacity, fluid, item);
        FluidAndItemCraftResult result8x = container.craftWithCraftingRecipes(level);
        if (result8x != null) {
            item.shrink(8);
            return result8x;
        }
        return null;
    }

    @Nullable
    public static FluidAndItemCraftResult craftFluidSpecial(Level level, boolean boiling, int tankCapacity, SoftFluidStack softFluidStack, Collection<ItemStack> items) {
        CauldronCraftingContainer container = CauldronCraftingContainer.of(boiling, tankCapacity, softFluidStack, items);
        FluidAndItemCraftResult crafted = container.craftWithCauldronRecipes(level);
        if (crafted != null) {
            for (var i : items) {
                if (!i.isEmpty()) {
                    i.shrink(1);
                }
            }
            return crafted;
        }
        return null;
    }

}
