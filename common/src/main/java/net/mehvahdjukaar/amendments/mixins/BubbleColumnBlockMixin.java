package net.mehvahdjukaar.amendments.mixins;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.mehvahdjukaar.amendments.configs.CommonConfigs;
import net.mehvahdjukaar.amendments.reg.ModTags;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.BubbleColumnBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(BubbleColumnBlock.class)
public abstract class BubbleColumnBlockMixin {

    @Unique
    private static boolean amendments$isWaterloggedGrate(BlockState state) {
        return CommonConfigs.BUBBLE_COLUMN_GRATES.get() && state.is(ModTags.BUBBLE_COLUMN_PASSABLE)
                && state.getFluidState().is(FluidTags.WATER);
    }


    @ModifyReturnValue(method = "canOccupy", at = @At("RETURN"))
    private static boolean amendments$occupyThroughGrates(boolean original, Block bubbleColumn, BlockState occupyState) {
        return original || amendments$isWaterloggedGrate(occupyState);
    }


    @WrapOperation(method = "updateColumn(Lnet/minecraft/world/level/block/Block;Lnet/minecraft/world/level/LevelAccessor;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/world/level/block/state/BlockState;)V",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/LevelAccessor;setBlock(Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;I)Z"))
    private static boolean amendments$dontReplaceGrates(LevelAccessor level, BlockPos pos, BlockState state,
                                                        int flags, Operation<Boolean> original) {
        if (amendments$isWaterloggedGrate(level.getBlockState(pos))) return true;
        return original.call(level, pos, state, flags);
    }

    @ModifyVariable(method = "updateColumn(Lnet/minecraft/world/level/block/Block;Lnet/minecraft/world/level/LevelAccessor;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/world/level/block/state/BlockState;)V",
            at = @At("HEAD"), argsOnly = true, ordinal = 1)
    private static BlockState amendments$seeBelowGrates(BlockState belowState, Block bubbleColumn, LevelAccessor level, BlockPos occupyAt, BlockState occupyState) {
        if (!amendments$isWaterloggedGrate(belowState)) return belowState;

        BlockPos.MutableBlockPos pos = occupyAt.mutable().move(0, -2, 0);
        BlockState state = level.getBlockState(pos);
        while (amendments$isWaterloggedGrate(state)) {
            pos.move(0, -1, 0);

            state = level.getBlockState(pos);
        }
        return state;
    }

    @ModifyReturnValue(method = "canSurvive", at = @At("RETURN"))
    private boolean amendments$surviveAboveGrates(boolean original, BlockState state, LevelReader level, BlockPos pos) {
        if (original || !amendments$isWaterloggedGrate(level.getBlockState(pos.below()))) return original;
        BlockPos.MutableBlockPos lowestGrate = pos.below().mutable();
        while (amendments$isWaterloggedGrate(level.getBlockState(lowestGrate.below()))) {
            lowestGrate.move(0, -1, 0);

        }

        return state.canSurvive(level, lowestGrate);
    }
}
