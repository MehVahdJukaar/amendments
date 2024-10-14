package net.mehvahdjukaar.amendments.common.recipe;


import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.CraftingInput;

import java.util.List;

public class CauldronRecipeInput {

    public static CraftingInput surround(ItemStack dye, ItemStack toRecolor) {
        List<ItemStack> items = List.of(
                toRecolor.copyWithCount(1),
                toRecolor.copyWithCount(1),
                toRecolor.copyWithCount(1),
                toRecolor.copyWithCount(1),
                dye.copyWithCount(1),
                toRecolor.copyWithCount(1),
                toRecolor.copyWithCount(1),
                toRecolor.copyWithCount(1),
                toRecolor.copyWithCount(1));
        return CraftingInput.of(3,3,items);
    }

    public static CraftingInput of(ItemStack... items) {
        return of(List.of(items));
    }

    public static CraftingInput of(List<ItemStack> items) {
        return CraftingInput.of(1, items.size(), items);
    }

}
