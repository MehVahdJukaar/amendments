package net.mehvahdjukaar.amendments.mixins;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.mehvahdjukaar.amendments.Amendments;
import net.mehvahdjukaar.amendments.configs.CommonConfigs;
import net.minecraft.core.Direction;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.block.AbstractCauldronBlock;
import net.minecraft.world.level.block.FenceBlock;
import net.minecraft.world.level.block.IronBarsBlock;
import net.minecraft.world.level.block.WallBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(FenceBlock.class)
public abstract class FenceBlockMixin {

    @ModifyReturnValue(method = "connectsTo", at = @At("RETURN"))
    public boolean amendments$cauldronConnect(boolean original, BlockState state, boolean isSideSolid, Direction direction) {
        if(!original && CommonConfigs.CONNECT_TO_FENCES.get()){
            if(state.getBlock() instanceof AbstractCauldronBlock){
                return true;
            }
        }
        return original;
    }

    @ModifyReturnValue(method = "connectsTo", at = @At("RETURN"))
    private boolean amendments$extraConnections(boolean original, BlockState state, boolean isSideSolid, Direction direction) {
        if (original) return true;
        boolean isWallOrBars = state.is(BlockTags.WALLS) || state.getBlock() instanceof IronBarsBlock;
        if (isWallOrBars && CommonConfigs.FENCES_TO_WALLS_AND_BARS.get()) return true;
        return Amendments.isWallSignAttachedTo(state, direction);
    }

    @ModifyReturnValue(method = "isSameFence", at = @At("RETURN"))
    private boolean amendments$connectNetherFences(boolean original, BlockState state) {
        return original || (CommonConfigs.WOODEN_TO_NETHER_FENCES.get() && state.is(BlockTags.FENCES));
    }
}
