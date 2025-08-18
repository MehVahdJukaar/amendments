package net.mehvahdjukaar.amendments.common.recipe;

import net.mehvahdjukaar.moonlight.api.fluids.SoftFluidStack;
import net.minecraft.world.item.ItemStack;

public record FluidAndItemCraftResult(ItemStack craftedItem, SoftFluidStack resultFluid) {

    public static FluidAndItemCraftResult of(ItemStack craftedItem, SoftFluidStack resultFluid){
        return new FluidAndItemCraftResult(craftedItem, resultFluid);
    }

}
