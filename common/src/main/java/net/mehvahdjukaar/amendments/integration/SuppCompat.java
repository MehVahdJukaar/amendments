package net.mehvahdjukaar.amendments.integration;

import net.mehvahdjukaar.amendments.common.tile.WallLanternBlockTile;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public class SuppCompat {
    public static boolean canBannerAttachToRope(BlockState state, BlockState above) {

        if (b instanceof RopeBlock) {
            if (!above.getValue(RopeBlock.DOWN)) {
                Direction dir = state.getValue(FACING);
                return above.getValue(RopeBlock.FACING_TO_PROPERTY_MAP.get(dir.getClockWise())) &&
                        above.getValue(RopeBlock.FACING_TO_PROPERTY_MAP.get(dir.getCounterClockWise()));
            }
        }
        return false;
    }

    //TODO: move to supp2
    @Nullable
    public static InteractionResult lightUpLantern(Level pLevel, BlockPos pPos, Player pPlayer, InteractionHand pHand, WallLanternBlockTile te, BlockState lantern) {
        if (lantern.getBlock() instanceof LightableLanternBlock) {
            var opt = LightableLanternBlock.toggleLight(lantern, pLevel, pPos, pPlayer, pHand);
            if (opt.isPresent()) {
                te.setHeldBlock(opt.get());
                return InteractionResult.sidedSuccess(pLevel.isClientSide);
            }
        }
        return null;
    }
}
