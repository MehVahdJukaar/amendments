package net.mehvahdjukaar.amendments.common.recipe;

import com.ibm.icu.impl.Pair;
import net.mehvahdjukaar.moonlight.api.fluids.SoftFluid;
import net.mehvahdjukaar.moonlight.api.fluids.SoftFluidStack;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.player.StackedContents;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;

import java.util.ArrayList;
import java.util.List;

public class RecipeUtils {

    public static Pair<ItemStack,Integer> craftWithFluid(Level level, SoftFluidStack fluidStack, ItemStack playerItem) {
        SoftFluid sf = fluidStack.getFluid().value();
        for (var category : sf.getContainerList().getCategories()) {
            int capacity = category.getCapacity();
            if (capacity > fluidStack.getCount()) continue;
            for(Item filled : category.getFilledItems()) {
                ItemStack crafted = simulateCrafting(level, filled.getDefaultInstance(), playerItem);
                if (!crafted.equals(playerItem)) {
                    return Pair.of(crafted, capacity);
                }
            }
        }
        return null;
    }


    public static ItemStack simulateCrafting(Level level, ItemStack first, ItemStack second) {
        DummyContainer dyeContainer = new DummyContainer(second.copy(), first.copy());
        var recipes = level.getRecipeManager().getRecipesFor(RecipeType.CRAFTING, dyeContainer, level);
        for (var r : recipes) {
            ItemStack recolored = r.assemble(dyeContainer, level.registryAccess());
            if (!recolored.isEmpty()) {
                var remainingItems = r.getRemainingItems(dyeContainer);
                remainingItems.remove(Items.GLASS_BOTTLE.getDefaultInstance());
                if (remainingItems.size() != 0) {
                    return recolored;
                }
            }
        }
        return ItemStack.EMPTY;
    }

    private static class DummyContainer implements CraftingContainer {

        private final List<ItemStack> items = new ArrayList<>();

        public DummyContainer(ItemStack... it) {
            items.addAll(List.of(it));
        }

        @Override
        public int getContainerSize() {
            return items.size();
        }

        @Override
        public boolean isEmpty() {
            return items.isEmpty();
        }

        @Override
        public ItemStack getItem(int slot) {
            return items.get(slot);
        }

        @Override
        public ItemStack removeItem(int slot, int amount) {
            return null;
        }

        @Override
        public ItemStack removeItemNoUpdate(int slot) {
            return null;
        }

        @Override
        public void setItem(int slot, ItemStack stack) {
        }

        @Override
        public void setChanged() {
        }

        @Override
        public boolean stillValid(Player player) {
            return true;
        }

        @Override
        public void clearContent() {
        }

        @Override
        public int getWidth() {
            return 2;
        }

        @Override
        public int getHeight() {
            return 2;
        }

        @Override
        public List<ItemStack> getItems() {
            return items;
        }

        @Override
        public void fillStackedContents(StackedContents helper) {

        }
    }
}
