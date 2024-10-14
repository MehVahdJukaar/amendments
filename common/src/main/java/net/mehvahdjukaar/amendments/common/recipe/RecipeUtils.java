package net.mehvahdjukaar.amendments.common.recipe;

import com.mojang.datafixers.util.Pair;
import net.mehvahdjukaar.amendments.common.item.DyeBottleItem;
import net.mehvahdjukaar.moonlight.api.fluids.SoftFluid;
import net.mehvahdjukaar.moonlight.api.fluids.SoftFluidStack;
import net.minecraft.client.gui.screens.inventory.CraftingScreen;
import net.minecraft.world.item.DyeItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;

public class RecipeUtils {

    public static Pair<ItemStack, Float> craftWithFluidAndDye(Level level, SoftFluidStack fluid, ItemStack toRecolor) {
        var c = RecipeUtils.craftWithFluid(level, fluid, toRecolor, true);
        if (c != null) return c;


        Item dyeItem = DyeItem.byColor(DyeBottleItem.getClosestDye(fluid));
        //first we try normal dye recipes then we try dye bottle one
        ItemStack recolored = RecipeUtils.simulateCrafting(level, toRecolor, dyeItem.getDefaultInstance(), false);
        if (recolored != null) return Pair.of(recolored, 1f);

        return null;
    }

    public static Pair<ItemStack, Float> craftWithFluid(Level level, SoftFluidStack fluidStack, ItemStack playerItem,
                                                        boolean try9x9) {
        if (playerItem.isEmpty()) return null;
        SoftFluid sf = fluidStack.fluid();
        for (var category : sf.getContainerList().getCategories()) {
            int capacity = category.getCapacity();
            if (capacity > fluidStack.getCount()) continue;
            var p = fluidStack.toItem(category.getEmptyContainer().getDefaultInstance(), true);
            if (p != null) {
                ItemStack crafted = simulateCrafting(level, p.getFirst(), playerItem, false);
                if (crafted != null) {
                    return Pair.of(crafted, (float) capacity);
                }
                if (try9x9) {
                    ItemStack crafted8 = simulateCrafting(level, p.getFirst(), playerItem, true);
                    if (crafted8 != null) {
                        int actualCapacity = 8 / crafted8.getCount();
                        if (actualCapacity > 0) {
                            return Pair.of(crafted8, (float) actualCapacity);
                        }
                    }
                }
            }
        }
        return null;
    }


    public static ItemStack simulateCrafting(Level level, ItemStack dye, ItemStack playerItem, boolean surround) {
        CraftingInput container = surround ? CauldronRecipeInput.surround(dye.copy(), playerItem.copy()) :
                CauldronRecipeInput.of(dye.copy(), playerItem.copy());
        var recipes = level.getRecipeManager().getRecipesFor(RecipeType.CRAFTING, container, level);
        for (var r : recipes) {
            var recipe = r.value();
            ItemStack recolored = recipe.assemble(container, level.registryAccess());
            if (!recolored.isEmpty() && !playerItem.equals(recolored)) {
                var remainingItems = recipe.getRemainingItems(container);
                remainingItems.remove(Items.GLASS_BOTTLE.getDefaultInstance());
                if (remainingItems.stream().noneMatch(i -> !i.isEmpty() && !i.is(Items.GLASS_BOTTLE))) {
                    return recolored;
                }
            }
        }
        return null;
    }

}
