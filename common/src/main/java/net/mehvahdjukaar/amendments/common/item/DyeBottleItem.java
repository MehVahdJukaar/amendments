package net.mehvahdjukaar.amendments.common.item;

import com.google.common.collect.HashBiMap;
import net.mehvahdjukaar.amendments.reg.ModRegistry;
import net.mehvahdjukaar.moonlight.api.fluids.SoftFluidStack;
import net.mehvahdjukaar.moonlight.api.util.math.colors.LABColor;
import net.mehvahdjukaar.moonlight.api.util.math.colors.RGBColor;
import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.Sheep;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.DyedItemColor;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.function.Function;
import java.util.stream.Collectors;

public class DyeBottleItem extends Item {

    private static final DyedItemColor DEFAULT_COLOR = new DyedItemColor(getDyeInt(DyeColor.WHITE), false);
    public static final DyedItemColor RED_COLOR = new DyedItemColor(getDyeInt(DyeColor.RED), false);

    protected static final HashBiMap<DyeColor, Integer> COLOR_TO_DIFFUSE = Arrays.stream(DyeColor.values())
            .collect(Collectors.toMap(Function.identity(), DyeColor::getTextureDiffuseColor,
                    (color, color2) -> color2, HashBiMap::create));

    public DyeBottleItem(Properties properties) {
        super(properties);
    }

    public static SoftFluidStack createFluidStack(DyeColor color, int amount) {
        SoftFluidStack stack = SoftFluidStack.of(ModRegistry.DYE_SOFT_FLUID.getHolder(), amount);
        stack.set(DataComponents.DYED_COLOR, new DyedItemColor(getDyeInt(color), true));
        return stack;
    }

    public static ItemStack fromFluidStack(SoftFluidStack stack) {
        ItemStack item = new ItemStack(ModRegistry.DYE_BOTTLE_ITEM.get());
        item.set(DataComponents.DYED_COLOR, stack.getOrDefault(DataComponents.DYED_COLOR, DEFAULT_COLOR));
        return item;
    }

    public static int mixColor(int oldColor, int newColor, int oldAmount, int newAmount) {
        return new RGBColor(oldColor).asHCL()
                .mixWith(new RGBColor(newColor).asHCL(), (float) newAmount / (oldAmount + newAmount))
                .asRGB().toInt();
    }

    @SuppressWarnings("ConstantConditions")
    @NotNull
    private static Integer getDyeInt(DyeColor color) {
        return COLOR_TO_DIFFUSE.get(color);
    }

    public static DyeColor getClosestDye(SoftFluidStack stack) {
        return getClosestDye(stack.getOrDefault(DataComponents.DYED_COLOR, DEFAULT_COLOR).rgb());
    }

    public static DyeColor getClosestDye(ItemStack stack) {
        return getClosestDye(stack.get(DataComponents.DYED_COLOR).rgb());
    }

    public static DyeColor getClosestDye(int tintColor) {
        LABColor color = new RGBColor(tintColor).asLAB();
        double minDist = Double.MAX_VALUE;
        DyeColor minColor = null;
        for (DyeColor dyeColor : DyeColor.values()) {
            //hsl distance is broken
            LABColor c2 = new RGBColor(getDyeInt(dyeColor)).asLAB();
            double dist = c2.distTo(color);
            if (dist < minDist) {
                minDist = dist;
                minColor = dyeColor;
            }
        }
        return minColor;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> list, TooltipFlag tooltipFlag) {
        super.appendHoverText(stack, context, list, tooltipFlag);

        DyedItemColor color = stack.get(DataComponents.DYED_COLOR);
        if (color == null) return;
        DyeColor dye = COLOR_TO_DIFFUSE.inverse().get(color.rgb());
        if (dye != null) {
            list.add(Component.translatable("item.amendments.dye_bottle." + dye.getName()).withStyle(ChatFormatting.GRAY));
        } else {
            if (tooltipFlag.isAdvanced()) {
                list.add(Component.translatable("item.color", String.format(Locale.ROOT, "#%06X", color.rgb())).withStyle(ChatFormatting.GRAY));
            } else {
                list.add(Component.translatable("item.dyed").withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC));
            }
        }
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
