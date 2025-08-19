package net.mehvahdjukaar.amendments.common.recipe;

import net.mehvahdjukaar.moonlight.api.fluids.SoftFluidStack;
import net.minecraft.world.item.ItemStack;

import java.util.List;

public record FluidAndItemsCraftResult( List<ItemStack> craftedItems, SoftFluidStack resultFluid) {

    public static FluidAndItemsCraftResult of(List<ItemStack> craftedItem, SoftFluidStack newFluid){
        return new FluidAndItemsCraftResult(craftedItem, newFluid);
    }
}