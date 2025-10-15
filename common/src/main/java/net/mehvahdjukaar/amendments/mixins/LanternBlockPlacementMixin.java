package net.mehvahdjukaar.amendments.mixins;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.mehvahdjukaar.amendments.common.entity.FallingLanternEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.LanternBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(LanternBlock.class)
public abstract class LanternBlockPlacementMixin {

    @ModifyReturnValue(method = {"canSurvive"}, at = {@At("RETURN")})
    private boolean isValidPosition(boolean original, BlockState state, LevelReader level, BlockPos pos) {
        if (state.getValue(LanternBlock.HANGING) && FallingLanternEntity.maybeFall(original, state, pos, level)) {
            return true; //keep to be destroyed by falling entity
        }
        return original;
    }

}

