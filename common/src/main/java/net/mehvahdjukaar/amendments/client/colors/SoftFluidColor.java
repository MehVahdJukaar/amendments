package net.mehvahdjukaar.amendments.client.colors;

import net.mehvahdjukaar.moonlight.api.block.ISoftFluidTankProvider;
import net.minecraft.client.color.block.BlockColor;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public class SoftFluidColor implements BlockColor {

    @Override
    public int getColor(BlockState state, @Nullable BlockAndTintGetter level, @Nullable BlockPos pos, int tint) {
        if (level != null && pos != null) {
            if (level.getBlockEntity(pos) instanceof ISoftFluidTankProvider bh) {
                if (tint == 1) {
                    var tank = bh.getSoftFluidTank();
                    return tank.getTintColor(level, pos);
                }
            }
        }
        return -1;
    }
}
