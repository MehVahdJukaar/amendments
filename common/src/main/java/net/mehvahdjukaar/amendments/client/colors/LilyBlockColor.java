package net.mehvahdjukaar.amendments.client.colors;

import net.mehvahdjukaar.amendments.common.tile.WaterloggedLilyBlockTile;
import net.minecraft.client.Minecraft;
import net.minecraft.client.color.block.BlockColor;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public class LilyBlockColor implements BlockColor {

    @Override
    public int getColor(BlockState blockState, @Nullable BlockAndTintGetter getter, @Nullable BlockPos pos, int i) {
        if (getter != null && pos != null) {
            if (getter.getBlockEntity(pos) instanceof WaterloggedLilyBlockTile te) {
                BlockState mimic = te.getHeldBlock();
                if (mimic != null) {
                    return Minecraft.getInstance().getBlockColors().getColor(mimic, getter, pos, i);
                }
            }
            return 2129968;
        }
        return 7455580;
    }
}