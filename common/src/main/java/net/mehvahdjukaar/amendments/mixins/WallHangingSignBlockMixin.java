package net.mehvahdjukaar.amendments.mixins;

import com.llamalad7.mixinextras.sugar.Local;
import net.mehvahdjukaar.amendments.reg.ModBlockProperties;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.WallHangingSignBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(WallHangingSignBlock.class)
public abstract class WallHangingSignBlockMixin extends Block implements EntityBlock {

    protected WallHangingSignBlockMixin(Properties properties) {
        super(properties);
    }

    @Inject(method = "canAttachTo",
            at = @At(value = "INVOKE",
                    shift = At.Shift.AFTER,
                    target = "Lnet/minecraft/world/level/block/state/BlockState;is(Lnet/minecraft/tags/TagKey;)Z"),
            cancellable = true)
    public void amendments$canAttachTo(LevelReader level, BlockState state, BlockPos facingPos, Direction direction,
                                       CallbackInfoReturnable<Boolean> cir, @Local(ordinal = 1) BlockState facingState) {
        if (ModBlockProperties.BlockAttachment.get(facingState, facingPos, level, direction) != null) {
            cir.setReturnValue(true);
        }
    }
}
