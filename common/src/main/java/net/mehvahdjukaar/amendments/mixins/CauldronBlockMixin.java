package net.mehvahdjukaar.amendments.mixins;

import net.mehvahdjukaar.amendments.common.block.LiquidCauldronBlock;
import net.mehvahdjukaar.amendments.reg.ModRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.CauldronBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(CauldronBlock.class)
public abstract class CauldronBlockMixin {

    @Inject(method = "receiveStalactiteDrip", at = @At("TAIL"))
    protected void handleModdedFluid(BlockState state, Level level, BlockPos pos, Fluid fluid, CallbackInfo ci) {
        if (level.getBlockState(pos) == state) {
            //if it failed converting to water or lava
            LiquidCauldronBlock block = ModRegistry.LIQUID_CAULDRON.get();
            level.setBlockAndUpdate(pos, block.defaultBlockState());
            block.receiveStalactiteDrip(block.defaultBlockState(), level, pos, fluid);
        }

    }
}
