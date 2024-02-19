package net.mehvahdjukaar.amendments.recipe;


import net.mehvahdjukaar.amendments.common.item.DyeBottleItem;
import net.mehvahdjukaar.amendments.reg.ModRegistry;
import net.mehvahdjukaar.moonlight.api.fluids.SoftFluidStack;
import net.mehvahdjukaar.moonlight.api.set.BlocksColorAPI;
import net.minecraft.core.RegistryAccess;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.player.StackedContents;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.*;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;

import java.util.ArrayList;
import java.util.List;

public class DyeBottleRecipe extends CustomRecipe {

    private static DyeBottleRecipe INSTANCE_HACK;

    public DyeBottleRecipe(ResourceLocation resourceLocation, CraftingBookCategory category) {
        super(resourceLocation, category);
        INSTANCE_HACK = this;
    }

    @Override
    public boolean matches(CraftingContainer craftingContainer, Level level) {
        boolean hasDye = false;
        boolean hasDyableItem = false;

        for (ItemStack itemstack : craftingContainer.getItems()) {
            if (!itemstack.isEmpty()) {
                Item item = itemstack.getItem();
                if (item == ModRegistry.DYE_BOTTLE_ITEM.get()) {
                    if (hasDye) return false;
                    else hasDye = true;
                } else if (item instanceof DyeableLeatherItem || BlocksColorAPI.getKey(item) != null) {
                    if (hasDyableItem) return false;
                    else hasDyableItem = true;
                }
            }
        }
        return hasDye && hasDyableItem;
    }

    @Override
    public ItemStack assemble(CraftingContainer craftingContainer, RegistryAccess registryAccess) {
        ItemStack leather = ItemStack.EMPTY;
        ItemStack dyeBottle = ItemStack.EMPTY;
        for (ItemStack itemstack : craftingContainer.getItems()) {
            if (!itemstack.isEmpty()) {
                Item item = itemstack.getItem();
                if (item == ModRegistry.DYE_BOTTLE_ITEM.get()) {
                    dyeBottle = itemstack;
                } else {
                    leather = itemstack;
                }
            }
        }
        ItemStack result;

        if (leather.getItem() instanceof DyeableLeatherItem l) {
            result = leather.copy();
            l.setColor(result, DyeBottleItem.getColor(dyeBottle));
        } else {
            result = BlocksColorAPI.changeColor(leather.getItem(),
                    DyeBottleItem.getClosestDye(dyeBottle)).getDefaultInstance();

        }
        return result;
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return width * height > 2;
    }

    @Override
    public String getGroup() {
        return "dye_bottle";
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return ModRegistry.DYE_BOTTLE_RECIPE.get();
    }


    public static ItemStack tryRecoloringWithRecipe(Level level, SoftFluidStack fluid, ItemStack toRecolor) {
        CompoundTag tag = fluid.getTag();
        if (tag == null) return ItemStack.EMPTY;
        ItemStack dyeBottle = DyeBottleItem.fromFluidStack(fluid);

        //first we try normal dye recipes then we try dye bottle one
        ColorContainer dyeContainer = new ColorContainer(DyeItem.byColor(DyeBottleItem.getClosestDye(dyeBottle))
                .getDefaultInstance(), toRecolor.copy());
        var recipes = level.getRecipeManager().getRecipesFor(RecipeType.CRAFTING, dyeContainer, level);
        for (var r : recipes) {
            ItemStack recolored = r.assemble(dyeContainer, level.registryAccess());
            if (!recolored.isEmpty()) {
                var remainingItems = r.getRemainingItems(dyeContainer);
                if (remainingItems.size() != 0) {
                    return recolored;
                }
            }
        }
        // try with the dye one. No need to get it when we know we want this one
        // probably not needed
        ColorContainer dyeBottleContainer = new ColorContainer(dyeBottle, toRecolor.copy());
        if (INSTANCE_HACK.matches(dyeBottleContainer, level)) {
            ItemStack recolored = INSTANCE_HACK.assemble(dyeBottleContainer, level.registryAccess());
            if (!recolored.isEmpty()) {
                return recolored;
            }
        }

        return ItemStack.EMPTY;
    }

    private static class ColorContainer implements CraftingContainer {

        private final List<ItemStack> items = new ArrayList<>();

        public ColorContainer(ItemStack... it) {
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

