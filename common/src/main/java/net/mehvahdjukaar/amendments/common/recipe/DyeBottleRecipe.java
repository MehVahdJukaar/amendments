package net.mehvahdjukaar.amendments.common.recipe;


import net.mehvahdjukaar.amendments.common.item.DyeBottleItem;
import net.mehvahdjukaar.amendments.reg.ModRegistry;
import net.mehvahdjukaar.moonlight.api.set.BlocksColorAPI;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponents;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.DyedItemColor;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;

public class DyeBottleRecipe extends CustomRecipe {


    public DyeBottleRecipe(CraftingBookCategory category) {
        super(category);
    }

    @Override
    public boolean matches(CraftingInput input, Level level) {
        boolean hasDyableItem = false;
        ItemStack dyeBottle = ItemStack.EMPTY;
        for (ItemStack itemstack : input.items()) {
            if (!itemstack.isEmpty()) {
                Item item = itemstack.getItem();
                if (item == ModRegistry.DYE_BOTTLE_ITEM.get()) {
                    if (!dyeBottle.isEmpty()) return false;
                    dyeBottle = itemstack;
                }
            }
        }
        if (dyeBottle.isEmpty()) return false;
        for (var itemstack : input.items()) {
            if (!itemstack.isEmpty()) {
                Item item = itemstack.getItem();
                if (itemstack.is(ItemTags.DYEABLE) ||
                        (BlocksColorAPI.changeColor(item,
                                DyeBottleItem.getClosestDye(dyeBottle)) != null)) {
                    if (hasDyableItem) return false;
                    else hasDyableItem = true;
                }
            }
        }
        return hasDyableItem;
    }

    @Override
    public ItemStack assemble(CraftingInput input, HolderLookup.Provider registries) {
        ItemStack leather = ItemStack.EMPTY;
        ItemStack dyeBottle = ItemStack.EMPTY;
        for (ItemStack itemstack : input.items()) {
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

        if (leather.is(ItemTags.DYEABLE)) {
            result = leather.copy();

            var colorComponent = leather.get(DataComponents.DYED_COLOR);
            if (colorComponent != null) {
                int mixedColor = DyeBottleItem.mixColor(dyeBottle.get(DataComponents.DYED_COLOR).rgb(),
                        colorComponent.rgb(), 1, 1);
                result.set(DataComponents.DYED_COLOR, new DyedItemColor(mixedColor, true));
            } else {
                result.set(DataComponents.DYED_COLOR, new DyedItemColor(dyeBottle.get(DataComponents.DYED_COLOR).rgb(), true));
            }
        } else {
            result = BlocksColorAPI.changeColor(leather.getItem(),
                    DyeBottleItem.getClosestDye(dyeBottle)).getDefaultInstance();
            result.applyComponents(leather.getComponents());
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


}

