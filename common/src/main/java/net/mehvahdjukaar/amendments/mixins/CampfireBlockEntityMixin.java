package net.mehvahdjukaar.amendments.mixins;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import net.mehvahdjukaar.amendments.configs.ClientConfigs;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.AbstractCauldronBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.CampfireBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(CampfireBlockEntity.class)
public abstract class CampfireBlockEntityMixin extends BlockEntity {

    public CampfireBlockEntityMixin(BlockEntityType<?> type, BlockPos pos, BlockState blockState) {
        super(type, pos, blockState);
    }

    @ModifyExpressionValue(method = "particleTick", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/RandomSource;nextFloat()F",
            ordinal = 0, shift = At.Shift.AFTER))
    private static float amendments$preventSmokeInCauldron(float original, @Local(argsOnly = true) Level level, @Local(argsOnly = true) BlockPos pos) {
        BlockState aboveState = level.getBlockState(pos.above());
        if (aboveState.getBlock() instanceof AbstractCauldronBlock || (aboveState.isSolid() && ClientConfigs.CAMPFIRE_SMOKE.get())) {
            return 0;
        }
        return original;
    }
}
