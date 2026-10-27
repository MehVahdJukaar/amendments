package net.mehvahdjukaar.amendments.integration;

import net.mehvahdjukaar.amendments.common.block.RedstoneWallLanternBlock;
import net.mehvahdjukaar.amendments.common.block.WallLanternBlock;
import net.mehvahdjukaar.amendments.common.tile.CandleSkullBlockTile;
import net.mehvahdjukaar.amendments.common.tile.LiquidCauldronBlockTile;
import net.mehvahdjukaar.amendments.reg.ModRegistry;
import net.mehvahdjukaar.moonlight.api.fluids.SoftFluidTank;
import net.mehvahdjukaar.polytone.api.VoxelCell;
import net.mehvahdjukaar.polytone.api.VoxelVolumeApi;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

public class PolytoneCompat {

    public static void setupClient() {
        VoxelVolumeApi.registerCallback(VoxelVolumeApi.Activation.ONLY_WITH_COLORED_LIGHT_PACKS, event -> {
            for (WallLanternBlock wallLantern : ModRegistry.WALL_LANTERNS.values()) {
                //aliases dont chain so this cant just point at the lantern
                if (wallLantern instanceof RedstoneWallLanternBlock) {
                    event.addBlockAlias(wallLantern, s -> s.getValue(WallLanternBlock.LIT) ? Blocks.REDSTONE_TORCH.defaultBlockState() : s);
                } else {
                    event.addBlockAlias(wallLantern, wallLantern::getLanternState);
                }
            }
            event.addBlockEntityCallback(ModRegistry.SKULL_CANDLE_TILE.get(), PolytoneCompat::lightFromCandle);
            event.addBlockEntityCallback(ModRegistry.LIQUID_CAULDRON_TILE.get(), PolytoneCompat::colorFromFluid);
        });
    }

    private static void lightFromCandle(CandleSkullBlockTile tile, VoxelCell cell) {
        BlockState candle = tile.getCandle();
        if (candle.isAir()) return;
        cell.copyFrom(candle.getBlock().withPropertiesOf(tile.getBlockState()));
    }

    private static void colorFromFluid(LiquidCauldronBlockTile tile, VoxelCell cell) {
        SoftFluidTank tank = tile.getSoftFluidTank();
        if (cell.lightLevel() == 0 || tank.isEmpty()) return;
        cell.setLight(tank.getCachedParticleColor(tile.getLevel(), tile.getBlockPos()), cell.lightLevel());
    }
}
