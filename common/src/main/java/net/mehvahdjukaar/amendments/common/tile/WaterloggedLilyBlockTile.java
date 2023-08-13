package net.mehvahdjukaar.amendments.common.tile;

import net.mehvahdjukaar.amendments.reg.ModRegistry;
import net.mehvahdjukaar.moonlight.api.block.MimicBlockTile;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

public class WaterloggedLilyBlockTile extends MimicBlockTile {
    public WaterloggedLilyBlockTile(BlockPos blockPos, BlockState blockState) {
        super(ModRegistry.WATERLILY_TILE.get(), blockPos, blockState);
    }
}
