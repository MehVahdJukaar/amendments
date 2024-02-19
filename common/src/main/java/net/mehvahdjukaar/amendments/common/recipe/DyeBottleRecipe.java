package net.mehvahdjukaar.amendments.common.recipe;


import com.ibm.icu.impl.Pair;
import net.mehvahdjukaar.amendments.common.item.DyeBottleItem;
import net.mehvahdjukaar.amendments.reg.ModRegistry;
import net.mehvahdjukaar.moonlight.api.fluids.SoftFluidStack;
import net.mehvahdjukaar.moonlight.api.set.BlocksColorAPI;
import net.minecraft.core.RegistryAccess;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.DyeItem;
import net.minecraft.world.item.DyeableLeatherItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;

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
                } else if (item instanceof DyeableLeatherItem ||
                        (BlocksColorAPI.getKey(item) != null && BlocksColorAPI.isDefaultColor(item))) {
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





}

