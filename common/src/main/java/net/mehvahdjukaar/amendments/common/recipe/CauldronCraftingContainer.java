package net.mehvahdjukaar.amendments.common.recipe;


import com.google.common.base.Preconditions;
import com.google.common.collect.Multimap;
import net.mehvahdjukaar.amendments.reg.ModRegistry;
import net.mehvahdjukaar.moonlight.api.fluids.FluidContainerList;
import net.mehvahdjukaar.moonlight.api.fluids.SoftFluidStack;
import net.minecraft.core.NonNullList;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.player.StackedContents;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.List;
import java.util.Map;

public class CauldronCraftingContainer implements CraftingContainer {

    private final List<ItemStack> originalItems;
    private final NonNullList<ItemStack> items;
    private final SoftFluidStack fluid;
    private final int fluidContainerSize;
    private final Multimap<FluidContainerList.Category, ItemStack> equivalentFluidContainers;
    private final int dimension;
    private final int fluidPosition;

    private CauldronCraftingContainer(int fluidContainerSize, SoftFluidStack fluidStack, Collection<ItemStack> items, int fluidPosition) {
        this.dimension = Mth.ceil(Math.sqrt(items.size() + 1));
        this.originalItems = List.copyOf(items);
        this.items = NonNullList.withSize((dimension * dimension), ItemStack.EMPTY);
        this.fluid = fluidStack;
        //fill all containers that it can fill.
        this.equivalentFluidContainers = fluidStack.toAllPossibleFilledItems();
        this.fluidPosition = fluidPosition;
        Preconditions.checkArgument(fluidPosition <= items.size());
        this.fluidContainerSize = fluidContainerSize;
    }

    //hack
    public static CauldronCraftingContainer surround8(int fluidContainerSize, SoftFluidStack fluid, ItemStack item) {
        /*4*/
        return new CauldronCraftingContainer(fluidContainerSize, fluid, List.of(
                item, item, item,
                item,/*4*/ item,
                item, item, item
        ), 4);
    }

    public static CauldronCraftingContainer of(int fluidContainerSize, SoftFluidStack fluid, ItemStack... items) {
        return new CauldronCraftingContainer(fluidContainerSize, fluid, List.of(items), items.length);
    }

    public static CauldronCraftingContainer of(int fluidContainerSize, SoftFluidStack fluid, Collection<ItemStack> items) {
        return new CauldronCraftingContainer(fluidContainerSize, fluid, items, items.size());
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
    public int getWidth() {
        return dimension;
    }

    @Override
    public int getHeight() {
        return dimension;
    }

    @Override
    public List<ItemStack> getItems() {
        return items;
    }


    public SoftFluidStack getFluid() {
        return fluid;
    }

    public int getFluidContainerSize() {
        return fluidContainerSize;
    }

    //why is this stuff here??
    @Override
    public ItemStack removeItem(int slot, int amount) {
        return ItemStack.EMPTY;
    }

    @Override
    public ItemStack removeItemNoUpdate(int slot) {
        return ItemStack.EMPTY;
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
    public void fillStackedContents(StackedContents helper) {
    }

    @Nullable
    public FluidAndItemCraftResult craftWithCauldronRecipes(Level level) {
        List<CauldronRecipe> recipes = level.getRecipeManager().getRecipesFor(ModRegistry.CAULDRON_RECIPE_TYPE.get(), this, level);
        for (var r : recipes) {
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
        for (var c : equivalentFluidContainers.entries()) {
            var category = c.getKey();
            setupFluidItem(c);
            List<CraftingRecipe> recipes = level.getRecipeManager().getRecipesFor(RecipeType.CRAFTING, this, level);
            for (var r : recipes) {
                if (!r.matches(this, level)) continue;
                int newFluidCount = fluid.getCount() - category.getCapacity();
                if(newFluidCount >= 0 && newFluidCount <= fluidContainerSize)continue;
                ItemStack craftedItem = r.assemble(this, level.registryAccess());
                if (!craftedItem.isEmpty()) {
                    var remainingItems = r.getRemainingItems(this);
                    //is this correct?
                    Item emptyContainer = category.getEmptyContainer();
                    remainingItems.remove(emptyContainer.getDefaultInstance());
                    if (remainingItems.stream().allMatch(ItemStack::isEmpty) ) {
                        return FluidAndItemCraftResult.of(craftedItem, fluid.copyWithCount(newFluidCount));
                    }
                }
            }

        }
        return null;
    }

    private void setupFluidItem(Map.Entry<FluidContainerList.Category, ItemStack> c) {
        this.items.clear();
        this.items.addAll(this.originalItems);
        this.items.add(fluidPosition, c.getValue());
    }


}
