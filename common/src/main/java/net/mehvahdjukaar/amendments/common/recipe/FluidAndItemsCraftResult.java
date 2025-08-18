package net.mehvahdjukaar.amendments.common.recipe;

import net.mehvahdjukaar.moonlight.api.fluids.SoftFluidStack;
import net.minecraft.world.item.ItemStack;

import java.util.List;

public record FluidAndItemsCraftResult( List<ItemStack> craftedItems, SoftFluidStack currentFluid) {

    public static FluidAndItemsCraftResult of(List<ItemStack> craftedItem, SoftFluidStack resultFluid){
        return new FluidAndItemsCraftResult(craftedItem, resultFluid);
    }
}