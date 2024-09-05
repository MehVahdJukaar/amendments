package net.mehvahdjukaar.amendments.common.recipe;


import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingInput;

import java.util.List;

public class CauldronRecipeInput {

    public static CraftingInput surround(ItemStack dye, ItemStack toRecolor) {
        List<ItemStack> items = List.of(
                toRecolor,
                toRecolor,
                toRecolor,
                toRecolor,
                dye,
                toRecolor,
                toRecolor,
                toRecolor,
                toRecolor);
        return of(items);
    }

    public static CraftingInput of(ItemStack... items) {
        return of(List.of(items));
    }

    public static CraftingInput of(List<ItemStack> items) {
        int dimension = Mth.ceil(Math.sqrt(items.size()));
        return CraftingInput.of(dimension, dimension, items);
    }

}
