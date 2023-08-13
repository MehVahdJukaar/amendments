package net.mehvahdjukaar.amendments.integration;

import com.teamabode.cave_enhancements.common.block.entity.SpectacleCandleBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

public class CaveEnhancementsCompat {
    public static void tick(Level level, BlockPos pos, BlockState state) {
        SpectacleCandleBlockEntity.tick(level, pos, state);
    }
}
