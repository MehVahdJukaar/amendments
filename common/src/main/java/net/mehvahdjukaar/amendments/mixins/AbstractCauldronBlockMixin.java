package net.mehvahdjukaar.amendments.mixins;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.sugar.Local;
import net.mehvahdjukaar.amendments.common.block.BoilingWaterCauldronBlock;
import net.mehvahdjukaar.amendments.common.block.LiquidCauldronBlock;
import net.mehvahdjukaar.amendments.configs.CommonConfigs;
import net.mehvahdjukaar.amendments.events.behaviors.CauldronConversion;
import net.mehvahdjukaar.moonlight.api.fluids.BuiltInSoftFluids;
import net.mehvahdjukaar.moonlight.api.fluids.SoftFluidStack;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.AbstractCauldronBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(AbstractCauldronBlock.class)
public class AbstractCauldronBlockMixin extends Block {

    public AbstractCauldronBlockMixin(Properties properties) {
        super(properties);
    }

    // why is this not an event? because it's better for compatibility
    @ModifyReturnValue(method = "use", at = @At("RETURN"))
    public InteractionResult use(InteractionResult original,
                                 @Local(argsOnly = true) BlockState state, @Local(argsOnly = true) Level level,
                                 @Local(argsOnly = true) BlockPos pos, @Local(argsOnly = true) Player player,
                                 @Local(argsOnly = true) InteractionHand hand, @Local ItemStack stack) {
        // do something
        if (original == InteractionResult.PASS && this == Blocks.CAULDRON && CommonConfigs.LIQUID_CAULDRON.get()) {
            return CauldronConversion.convert(state, pos, level, player, hand, stack, false);
        }
        BlockState newState = level.getBlockState(pos);
        if (newState.getBlock() instanceof BoilingWaterCauldronBlock) {
            BlockPos belowPos = pos.below();
            boolean isFire = LiquidCauldronBlock.shouldBoil(level.getBlockState(belowPos),
                    SoftFluidStack.of(BuiltInSoftFluids.WATER.getHolder()), level, belowPos);
            if (isFire) {
                level.setBlockAndUpdate(pos, newState.setValue(BoilingWaterCauldronBlock.BOILING, true));
            }
        }

        return original;
    }

}
