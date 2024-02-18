package net.mehvahdjukaar.amendments.common.item;

import com.google.common.collect.HashBiMap;
import net.mehvahdjukaar.amendments.reg.ModRegistry;
import net.mehvahdjukaar.moonlight.api.fluids.SoftFluidStack;
import net.mehvahdjukaar.moonlight.api.fluids.SoftFluidTank;
import net.mehvahdjukaar.moonlight.api.util.math.ColorUtils;
import net.mehvahdjukaar.moonlight.api.util.math.colors.HSLColor;
import net.mehvahdjukaar.moonlight.api.util.math.colors.RGBColor;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.Sheep;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.SignBlockEntity;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;
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

    //TODO: change
    public static void fillCauldron(SoftFluidTank tank, DyeColor color, int amount) {
        CompoundTag tag = new CompoundTag();
        tag.putInt(COLOR_TAG, getDyeInt(color));
        tank.addFluid(new SoftFluidStack(ModRegistry.DYE_SOFT_FLUID.getHolder(), amount, tag));
    }

    @SuppressWarnings("ConstantConditions")
    @NotNull
    private static Integer getDyeInt(DyeColor color) {
        return COLOR_TO_DIFFUSE.get(color);
    }

    public static DyeColor getClosestDye(ItemStack stack) {
        return getClosestDye(stack.getOrCreateTag().getInt(COLOR_TAG));
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
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> list, TooltipFlag isAdvanced) {
        CompoundTag tag = stack.getOrCreateTag();
        int col = tag.getInt(COLOR_TAG);
        DyeColor color = COLOR_TO_DIFFUSE.inverse().get(col);
        if (color != null) {
            list.add(Component.translatable("item.amendments.dye_bottle." + color.getName()).withStyle(ChatFormatting.GRAY));
        } else {
            list.add(Component.translatable("item.color", String.format(Locale.ROOT, "#%06X",col)).withStyle(ChatFormatting.GRAY));
        }
        super.appendHoverText(stack, level, list, isAdvanced);
    }

    @Override
    public InteractionResult interactLivingEntity(ItemStack stack, Player player, LivingEntity interactionTarget, InteractionHand usedHand) {
        if (interactionTarget instanceof Sheep sheep) {
            DyeColor dye = getClosestDye(stack);
            if (sheep.isAlive() && !sheep.isSheared() && sheep.getColor() != dye) {
                sheep.level().playSound(player, sheep, SoundEvents.DYE_USE, SoundSource.PLAYERS, 1.0F, 1.0F);
                if (!player.level().isClientSide) {
                    sheep.setColor(dye);
                    stack.shrink(1);
                }
                return InteractionResult.sidedSuccess(player.level().isClientSide);
            }
        }

        return InteractionResult.PASS;
    }



}
