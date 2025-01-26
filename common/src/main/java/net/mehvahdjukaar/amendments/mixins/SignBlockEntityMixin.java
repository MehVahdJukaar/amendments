package net.mehvahdjukaar.amendments.mixins;

import net.mehvahdjukaar.amendments.common.ExtendedHangingSign;
import net.mehvahdjukaar.amendments.configs.ClientConfigs;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.SignBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(SignBlockEntity.class)
public abstract class SignBlockEntityMixin extends BlockEntity{
    public SignBlockEntityMixin(BlockEntityType<?> type, BlockPos pos, BlockState blockState) {
        super(type, pos, blockState);
    }

    /**
     * Swings hanging signs that override getTicker
     */
    @Inject(method = "tick", at = @At("HEAD"))
    private static void amendments$swingingSign(Level level, BlockPos pos, BlockState state, SignBlockEntity sign, CallbackInfo ci){
        if (sign.getType() == BlockEntityType.HANGING_SIGN) {
            // We're already swinging via CeilingHangingSignBlockMixin
            return;
        }
        if (level.isClientSide && ClientConfigs.SWINGING_SIGNS.get() && sign instanceof ExtendedHangingSign te) {
            te.amendments$getExtension().clientTick(level, pos, state);
        }
    }
}
