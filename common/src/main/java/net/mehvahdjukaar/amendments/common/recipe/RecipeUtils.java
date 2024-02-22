package net.mehvahdjukaar.amendments.common.recipe;

import com.mojang.datafixers.util.Pair;
import net.mehvahdjukaar.amendments.common.item.DyeBottleItem;
import net.mehvahdjukaar.moonlight.api.fluids.SoftFluid;
import net.mehvahdjukaar.moonlight.api.fluids.SoftFluidStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.player.StackedContents;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.DyeItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;

import java.util.ArrayList;
import java.util.List;

public class RecipeUtils {

    public static Pair<ItemStack, Float> craftWithFluidAndDye(Level level, SoftFluidStack fluid, ItemStack toRecolor) {
        CompoundTag tag = fluid.getTag();
        if (tag == null) return null;


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
        SoftFluid sf = fluidStack.getFluid().value();
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
                    ItemStack crafted9 = simulateCrafting(level, p.getFirst(), playerItem, true);
                    if (crafted9 != null) {
                        return Pair.of(crafted9, (float) capacity);
                    }
                }
            }
        }
        return null;
    }


    public static ItemStack simulateCrafting(Level level, ItemStack dye, ItemStack playerItem, boolean surround) {
        DummyContainer container = new DummyContainer(dye.copy(), playerItem.copy(), surround);
        var recipes = level.getRecipeManager().getRecipesFor(RecipeType.CRAFTING, container, level);
        for (var r : recipes) {
            ItemStack recolored = r.assemble(container, level.registryAccess());
            if (!recolored.isEmpty() && !playerItem.equals(recolored)) {
                var remainingItems = r.getRemainingItems(container);
                remainingItems.remove(Items.GLASS_BOTTLE.getDefaultInstance());
                if (remainingItems.stream().noneMatch(i -> !i.isEmpty() && !i.is(Items.GLASS_BOTTLE))) {
                    return recolored;
                }
            }
        }
        return null;
    }

    private static class DummyContainer implements CraftingContainer {

        private final List<ItemStack> items = new ArrayList<>();
        private final boolean surround;

        public DummyContainer(ItemStack dye, ItemStack toRecolor, boolean surround) {
            this.surround = surround;
            if (surround) {
                items.add(toRecolor);
                items.add(toRecolor);
                items.add(toRecolor);

                items.add(toRecolor);
                items.add(dye);
                items.add(toRecolor);

                items.add(toRecolor);
                items.add(toRecolor);
                items.add(toRecolor);
            } else {
                items.add(dye);
                items.add(toRecolor);
            }
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
            if (slot >= this.getContainerSize()) return ItemStack.EMPTY;
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
            return surround ? 3 : 2;
        }

        @Override
        public int getHeight() {
            return surround ? 3 : 2;
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
