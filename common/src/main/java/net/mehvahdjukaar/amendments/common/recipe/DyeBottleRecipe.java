package net.mehvahdjukaar.amendments.common.recipe;


import net.mehvahdjukaar.amendments.common.item.DyeBottleItem;
import net.mehvahdjukaar.amendments.reg.ModRegistry;
import net.mehvahdjukaar.moonlight.api.set.BlocksColorAPI;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.DyedItemColor;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;

public class DyeBottleRecipe extends CustomRecipe {


    public DyeBottleRecipe() {
    }

    @Override
    public boolean matches(CraftingInput input, Level level) {
        ItemStack dyeBottle = ItemStack.EMPTY;
        ItemStack otherItem = ItemStack.EMPTY;
        for (ItemStack itemstack : input.items()) {
            if (!itemstack.isEmpty()) {
                Item item = itemstack.getItem();
                if (item == ModRegistry.DYE_BOTTLE_ITEM.get()) {
                    if (!dyeBottle.isEmpty()) return false;
                    dyeBottle = itemstack;
                } else {
                    if (!otherItem.isEmpty()) return false;
                    otherItem = itemstack;
                }
            }
        }
        if (dyeBottle.isEmpty() || otherItem.isEmpty()) return false;
        return otherItem.has(DataComponents.DYED_COLOR) ||
                (BlocksColorAPI.changeColor(otherItem.getItem(),
                        DyeBottleItem.getClosestDye(dyeBottle)) != null);
    }

    @Override
    public ItemStack assemble(CraftingInput input) {
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

        if (leather.has(DataComponents.DYED_COLOR)) {
            result = leather.copy();

            var colorComponent = leather.get(DataComponents.DYED_COLOR);
            if (colorComponent != null) {
                int mixedColor = DyeBottleItem.mixColor(dyeBottle.get(DataComponents.DYED_COLOR).rgb(),
                        colorComponent.rgb(), 1, 1);
                result.set(DataComponents.DYED_COLOR, new DyedItemColor(mixedColor));
            } else {
                result.set(DataComponents.DYED_COLOR, new DyedItemColor(dyeBottle.get(DataComponents.DYED_COLOR).rgb()));
            }
        } else {
            DyeColor dye = DyeBottleItem.getClosestDye(dyeBottle);
            Item changed = BlocksColorAPI.changeColor(leather.getItem(), dye);
            if (changed == null) {
                return ItemStack.EMPTY;
            }
            result = changed.getDefaultInstance();
            result.applyComponents(leather.getComponents());
        }
        return result;
    }

    @Override
    public String group() {
        return "dye_bottle";
    }

    @Override
    public RecipeSerializer<DyeBottleRecipe> getSerializer() {
        return ModRegistry.DYE_BOTTLE_RECIPE.get();
    }


}

