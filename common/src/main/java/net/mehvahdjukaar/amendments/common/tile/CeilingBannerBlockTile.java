package net.mehvahdjukaar.amendments.common.tile;

import net.mehvahdjukaar.amendments.reg.ModRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.block.entity.BannerBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;


public class CeilingBannerBlockTile extends BannerBlockEntity {

    public CeilingBannerBlockTile(BlockPos pos, BlockState state) {
        this(pos, state, DyeColor.WHITE);
    }

    public CeilingBannerBlockTile(BlockPos pos, BlockState state, DyeColor color) {
        super(pos, state, color);
    }

    @Override
    public BlockEntityType<?> getType() {
        return ModRegistry.CEILING_BANNER_TILE.get();
    }
}

