package net.mehvahdjukaar.amendments.mixins;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.mehvahdjukaar.amendments.configs.CommonConfigs;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.AbstractCauldronBlock;
import net.minecraft.world.level.block.FenceBlock;
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
}
