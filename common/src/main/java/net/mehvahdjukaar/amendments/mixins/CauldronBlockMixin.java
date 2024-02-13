package net.mehvahdjukaar.amendments.mixins;

import net.mehvahdjukaar.amendments.common.tile.LiquidCauldronBlockTile;
import net.mehvahdjukaar.amendments.reg.ModRegistry;
import net.mehvahdjukaar.moonlight.api.fluids.SoftFluidRegistry;
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
public class CauldronBlockMixin {

    @Inject(method = "receiveStalactiteDrip", at = @At("TAIL"))
    protected void handleModdedFluid(BlockState state, Level level, BlockPos pos, Fluid fluid, CallbackInfo ci) {
        level.setBlockAndUpdate(pos, ModRegistry.LIQUID_CAULDRON.get().defaultBlockState());
        if(level.getBlockEntity(pos) instanceof LiquidCauldronBlockTile te){
            te.getSoftFluidTank().tryAddingFluid(SoftFluidRegistry.fromVanillaFluid(fluid), 1);
        }
    }
}
