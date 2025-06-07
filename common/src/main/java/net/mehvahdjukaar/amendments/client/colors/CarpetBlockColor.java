package net.mehvahdjukaar.amendments.client.colors;

import net.mehvahdjukaar.amendments.common.tile.CarpetedBlockTile;
import net.minecraft.client.Minecraft;
import net.minecraft.client.color.block.BlockColor;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public class CarpetBlockColor implements BlockColor {

    @Override
    public int getColor(BlockState state, @Nullable BlockAndTintGetter world, @Nullable BlockPos pos, int tint) {
        return col(state, world, pos, tint);
    }

    public static int col(BlockState state, BlockAndTintGetter level, BlockPos pos, int tint) {
        if (level != null && pos != null) {
            if (level.getBlockEntity(pos) instanceof CarpetedBlockTile tile) {
                if (tint == 16) {
                    BlockState carpet = tile.getCarpet();
                    if (carpet != null && !carpet.hasBlockEntity()) {
                        return Minecraft.getInstance().getBlockColors().getColor(carpet, level, pos, tint);
                    }
                }
                BlockState mimic = tile.getHeldBlock();
                if (mimic != null && !mimic.hasBlockEntity()) {
                   return Minecraft.getInstance().getBlockColors().getColor(mimic, level, pos, tint);
                }
            }
        }
        return -1;
    }
}