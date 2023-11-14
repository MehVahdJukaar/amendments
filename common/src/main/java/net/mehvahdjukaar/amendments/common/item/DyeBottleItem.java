package net.mehvahdjukaar.amendments.common.item;

import net.mehvahdjukaar.amendments.reg.ModRegistry;
import net.mehvahdjukaar.moonlight.api.fluids.SoftFluidTank;
import net.mehvahdjukaar.moonlight.api.util.math.colors.HSLColor;
import net.mehvahdjukaar.moonlight.api.util.math.colors.RGBColor;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class DyeBottleItem extends Item {

    public static final String COLOR_TAG = "color";

    public DyeBottleItem(Properties properties) {
        super(properties);
    }

    public static int getColor(ItemStack stack) {
        CompoundTag compoundTag = stack.getTag();
        if (compoundTag != null) {
            return compoundTag.getInt(COLOR_TAG);
        }
        return 0;
    }

    public static void fillCauldron(SoftFluidTank tank, DyeColor color, int amount) {
        CompoundTag tag = new CompoundTag();
        tag.putInt(COLOR_TAG, color.getFireworkColor());
        tank.tryAddingFluid(ModRegistry.DYE_SOFT_FLUID.get(), amount, tag);
    }

    public static DyeColor getClosestDye(int tintColor) {
        HSLColor color = new RGBColor(tintColor).asHSL();
        double minDist = Double.MAX_VALUE;
        DyeColor minColor = null;
        for (DyeColor dyeColor : DyeColor.values()) {
            HSLColor c2 = new RGBColor(dyeColor.getFireworkColor()).asHSL();
            double dist = c2.distTo(color);
            if (dist < minDist) {
                minDist = dist;
                minColor = dyeColor;
            }
        }
        return minColor;
    }

    @Override
    public ItemStack getDefaultInstance() {
        ItemStack stack = super.getDefaultInstance();
        stack.getOrCreateTag().putInt(COLOR_TAG, DyeColor.RED.getFireworkColor());
        return stack;
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltipComponents, TooltipFlag isAdvanced) {
        CompoundTag tag = stack.getOrCreateTag();
        tag.putInt(COLOR_TAG, DyeColor.RED.getFireworkColor());
        int col = tag.getInt(COLOR_TAG);
        DyeColor color = DyeColor.byFireworkColor(col);
        if (color != null) {
            tooltipComponents.add(Component.translatable("item.amendments.dye_bottle." + color.getName()).withStyle(ChatFormatting.GRAY));
        } else {
            tooltipComponents.add(Component.translatable("item.amendments.dye_bottle.custom", Integer.toHexString(col)).withStyle(ChatFormatting.GRAY));
        }
        super.appendHoverText(stack, level, tooltipComponents, isAdvanced);
    }
}
