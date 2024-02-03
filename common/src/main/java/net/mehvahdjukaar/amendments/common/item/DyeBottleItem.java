package net.mehvahdjukaar.amendments.common.item;

import com.google.common.collect.HashBiMap;
import net.mehvahdjukaar.amendments.reg.ModRegistry;
import net.mehvahdjukaar.moonlight.api.fluids.SoftFluidTank;
import net.mehvahdjukaar.moonlight.api.util.math.ColorUtils;
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
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

public class DyeBottleItem extends Item {

    public static final String COLOR_TAG = "color";

    protected static final HashBiMap<DyeColor,Integer> COLOR_TO_DIFFUSE = Arrays.stream(DyeColor.values())
            .collect(Collectors.toMap(Function.identity(), color ->
                    ColorUtils.pack(color.getTextureDiffuseColors()),
                    (color, color2) -> color2, HashBiMap::create));

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
        tag.putInt(COLOR_TAG, getDyeInt(color));
        tank.tryAddingFluid(ModRegistry.DYE_SOFT_FLUID.get(), amount, tag);
    }

    @SuppressWarnings("ConstantConditions")
    @NotNull
    private static Integer getDyeInt(DyeColor color) {
        return COLOR_TO_DIFFUSE.get(color);
    }

    public static DyeColor getClosestDye(int tintColor) {
        HSLColor color = new RGBColor(tintColor).asHSL();
        double minDist = Double.MAX_VALUE;
        DyeColor minColor = null;
        for (DyeColor dyeColor : DyeColor.values()) {
            HSLColor c2 = new RGBColor(getDyeInt(dyeColor)).asHSL();
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
        stack.getOrCreateTag().putInt(COLOR_TAG, getDyeInt(DyeColor.RED));
        return stack;
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltipComponents, TooltipFlag isAdvanced) {
        CompoundTag tag = stack.getOrCreateTag();
        int col = tag.getInt(COLOR_TAG);
        DyeColor color = COLOR_TO_DIFFUSE.inverse().get(col);
        if (color != null) {
            tooltipComponents.add(Component.translatable("item.amendments.dye_bottle." + color.getName()).withStyle(ChatFormatting.GRAY));
        } else {
            tooltipComponents.add(Component.translatable("item.amendments.dye_bottle.custom", Integer.toHexString(col)).withStyle(ChatFormatting.GRAY));
        }
        super.appendHoverText(stack, level, tooltipComponents, isAdvanced);
    }
}
