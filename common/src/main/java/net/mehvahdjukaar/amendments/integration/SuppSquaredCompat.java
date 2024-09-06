package net.mehvahdjukaar.amendments.integration;

import net.mehvahdjukaar.amendments.common.tile.WallLanternBlockTile;
import net.mehvahdjukaar.suppsquared.common.LightableLanternBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public class SuppSquaredCompat {
    public static boolean isLightableLantern(Block b) {
        return b instanceof LightableLanternBlock;
    }

    public static ItemInteractionResult lightUpLantern(Level pLevel, BlockPos pPos, Player pPlayer, InteractionHand pHand,
                                                       ItemStack stack, WallLanternBlockTile te, BlockState lantern) {
        if (lantern.getBlock() instanceof LightableLanternBlock) {
            var opt = LightableLanternBlock.toggleLight(lantern, pLevel, pPos, pPlayer, pHand);
            if (opt.isPresent()) {
                te.setHeldBlock(opt.get());
                return ItemInteractionResult.sidedSuccess(pLevel.isClientSide);
            }
        }
        return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
    }


}
