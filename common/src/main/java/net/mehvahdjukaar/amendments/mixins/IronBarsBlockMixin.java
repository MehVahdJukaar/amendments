package net.mehvahdjukaar.amendments.mixins;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.mehvahdjukaar.amendments.Amendments;
import net.mehvahdjukaar.amendments.configs.CommonConfigs;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.IronBarsBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(IronBarsBlock.class)
public abstract class IronBarsBlockMixin {

    @ModifyReturnValue(method = "attachsTo", at = @At("RETURN"))
    private boolean amendments$attachToFences(boolean original, BlockState state, boolean solidSide) {
        return original || (CommonConfigs.FENCES_TO_WALLS_AND_BARS.get() && state.is(BlockTags.FENCES));
    }

    @WrapOperation(method = {"getStateForPlacement", "updateShape"}, at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/level/block/state/BlockState;isFaceSturdy(Lnet/minecraft/world/level/BlockGetter;Lnet/minecraft/core/BlockPos;Lnet/minecraft/core/Direction;)Z"))
    private boolean amendments$attachToWallSigns(BlockState state, BlockGetter level, BlockPos pos, Direction side, Operation<Boolean> original) {
        return original.call(state, level, pos, side) || Amendments.isWallSignAttachedTo(state, side);
    }
}
