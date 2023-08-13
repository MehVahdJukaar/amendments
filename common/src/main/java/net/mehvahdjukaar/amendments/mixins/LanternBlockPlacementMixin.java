package net.mehvahdjukaar.amendments.mixins;

import net.mehvahdjukaar.amendments.common.entity.FallingLanternEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.LanternBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LanternBlock.class)
public abstract class LanternBlockPlacementMixin {

    @Inject(method = {"canSurvive"}, at = {@At("HEAD")}, cancellable = true)
    private void isValidPosition(BlockState state, LevelReader worldIn, BlockPos pos, CallbackInfoReturnable<Boolean> callbackInfoReturnable) {
        if (state.getValue(LanternBlock.HANGING) && FallingLanternEntity.canSurviveCeilingAndMaybeFall(state, pos, worldIn)) {
            callbackInfoReturnable.setReturnValue(true);
        }
    }

}

