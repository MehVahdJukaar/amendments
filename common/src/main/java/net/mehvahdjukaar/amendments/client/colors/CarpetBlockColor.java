package net.mehvahdjukaar.amendments.client.colors;

import net.mehvahdjukaar.amendments.common.tile.CarpetedBlockTile;
import net.minecraft.client.color.block.BlockTintSource;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

import java.util.ArrayList;
import java.util.List;

public record CarpetBlockColor(int tint) implements BlockTintSource {

    public static final int CARPET_TINT = 16;

    public static List<BlockTintSource> layers() {
        List<BlockTintSource> list = new ArrayList<>();
        for (int i = 0; i <= CARPET_TINT; i++) list.add(new CarpetBlockColor(i));
        return list;
    }

    @Override
    public int color(BlockState state) {
        return -1;
    }

    @Override
    public int colorInWorld(BlockState state, BlockAndTintGetter level, BlockPos pos) {
        if (level == null || pos == null) return -1;
        if (!(level.getBlockEntity(pos) instanceof CarpetedBlockTile tile)) return -1;

        if (this.tint == CARPET_TINT) {
            BlockState carpet = tile.getCarpet();
            if (carpet != null && !carpet.hasBlockEntity()) {
                return MimicBlockColor.tintOf(carpet, level, pos, this.tint);
            }
        }
        BlockState mimic = tile.getHeldBlock();
        if (mimic != null && !mimic.hasBlockEntity()) {
            return MimicBlockColor.tintOf(mimic, level, pos, this.tint);
        }
        return -1;
    }
}
