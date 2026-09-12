package net.mehvahdjukaar.amendments.client.colors;

import net.mehvahdjukaar.amendments.reg.ModTags;
import net.mehvahdjukaar.moonlight.api.block.ISoftFluidTankProvider;
import net.minecraft.client.color.block.BlockTintSource;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

import java.util.List;

public class SoftFluidColor implements BlockTintSource {

    public static List<BlockTintSource> layers() {
        return List.of(BLANK, new SoftFluidColor());
    }

    private static final BlockTintSource BLANK = state -> -1;

    @Override
    public int color(BlockState state) {
        return -1;
    }

    @Override
    public int colorInWorld(BlockState state, BlockAndTintGetter level, BlockPos pos) {
        if (level != null && pos != null && level.getBlockEntity(pos) instanceof ISoftFluidTankProvider bh) {
            var tank = bh.getSoftFluidTank();
            if (!tank.getFluid().is(ModTags.NO_TINT_IN_CAULDRON)) {
                return tank.getCachedStillColor(level, pos);
            }
        }
        return -1;
    }
}
