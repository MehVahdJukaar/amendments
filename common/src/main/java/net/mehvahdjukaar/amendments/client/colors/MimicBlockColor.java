package net.mehvahdjukaar.amendments.client.colors;

import net.mehvahdjukaar.moonlight.api.block.IBlockHolder;
import net.minecraft.client.Minecraft;
import net.minecraft.client.color.block.BlockTintSource;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

import java.util.ArrayList;
import java.util.List;

public record MimicBlockColor(int tint) implements BlockTintSource {

    public static final int LAYERS = CarpetBlockColor.CARPET_TINT + 1;

    public static List<BlockTintSource> layers() {
        List<BlockTintSource> list = new ArrayList<>();
        for (int i = 0; i < LAYERS; i++) list.add(new MimicBlockColor(i));
        return list;
    }

    @Override
    public int color(BlockState state) {
        return -1;
    }

    @Override
    public int colorInWorld(BlockState state, BlockAndTintGetter level, BlockPos pos) {
        return col(level, pos, this.tint);
    }

    public static int col(BlockAndTintGetter level, BlockPos pos, int tint) {
        if (level == null || pos == null) return -1;
        if (level.getBlockEntity(pos) instanceof IBlockHolder tile) {
            BlockState mimic = tile.getHeldBlock();
            if (mimic != null && !mimic.hasBlockEntity()) {
                return tintOf(mimic, level, pos, tint);
            }
        }
        return -1;
    }

    public static int tintOf(BlockState state, BlockAndTintGetter level, BlockPos pos, int tint) {
        return Minecraft.getInstance().getBlockColors().getTintSource(state, tint).colorInWorld(state, level, pos);
    }
}
