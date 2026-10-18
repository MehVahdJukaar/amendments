package net.mehvahdjukaar.amendments.mixins;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.mehvahdjukaar.amendments.Amendments;
import net.mehvahdjukaar.amendments.configs.CommonConfigs;
import net.minecraft.core.Direction;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.block.WallBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(WallBlock.class)
public abstract class WallBlockMixin {

    @ModifyReturnValue(method = "connectsTo", at = @At("RETURN"))
    private boolean amendments$extraConnections(boolean original, BlockState state, boolean sideSolid, Direction direction) {
        if (original) return true;
        if (CommonConfigs.FENCES_TO_WALLS_AND_BARS.get() && state.is(BlockTags.FENCES)) return true;
        return Amendments.isWallSignAttachedTo(state, direction);
    }
}
