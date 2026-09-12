package net.mehvahdjukaar.amendments.client.colors;

import net.mehvahdjukaar.amendments.common.tile.WaterloggedLilyBlockTile;
import net.minecraft.client.color.block.BlockColors;
import net.minecraft.client.color.block.BlockTintSource;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

public record LilyBlockColor(int tint) implements BlockTintSource {

    @Override
    public int color(BlockState state) {
        return BlockColors.LILY_PAD_DEFAULT;
    }

    @Override
    public int colorInWorld(BlockState state, BlockAndTintGetter level, BlockPos pos) {
        if (level != null && pos != null) {
            if (level.getBlockEntity(pos) instanceof WaterloggedLilyBlockTile te) {
                BlockState mimic = te.getHeldBlock();
                if (mimic != null) {
                    return MimicBlockColor.tintOf(mimic, level, pos, this.tint);
                }
            }
            return BlockColors.LILY_PAD_IN_WORLD;
        }
        return BlockColors.LILY_PAD_DEFAULT;
    }
}
