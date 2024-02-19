package net.mehvahdjukaar.amendments.common.recipe;


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
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class DyeBottleRecipe extends CustomRecipe {

    public DyeBottleRecipe(ResourceLocation resourceLocation, CraftingBookCategory category) {
        super(resourceLocation, category);
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

        DyeItem.byColor(DyeBottleItem.getClosestDye(second)
        //first we try normal dye recipes then we try dye bottle one
        ItemStack recolored = RecipeUtils.simulateCraf(level, toRecolor, dyeBottle);
        if (recolored != null) return recolored;
        // try with the dye one. No need to get it when we know we want this one
        // probably not needed

        return ItemStack.EMPTY;
    }



}

