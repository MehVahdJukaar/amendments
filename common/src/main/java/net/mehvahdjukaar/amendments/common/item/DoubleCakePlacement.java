package net.mehvahdjukaar.amendments.common.item;

import net.mehvahdjukaar.amendments.common.CakeRegistry;
import net.mehvahdjukaar.amendments.common.block.DoubleCakeBlock;
import net.mehvahdjukaar.amendments.configs.CommonConfigs;
import net.mehvahdjukaar.amendments.reg.ModRegistry;
import net.mehvahdjukaar.moonlight.api.item.additional_placements.AdditionalItemPlacement;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CakeBlock;
import net.minecraft.world.level.block.state.BlockState;

public class DoubleCakePlacement extends AdditionalItemPlacement {

    public DoubleCakePlacement(Block placeable) {
        super(placeable);
    }

    @Override
    public BlockPlaceContext overrideUpdatePlacementContext(BlockPlaceContext pContext) {
        BlockPos pos = pContext.getClickedPos().relative(pContext.getClickedFace());
        Level level = pContext.getLevel();
        BlockState state = level.getBlockState(pos);
        if (isValidCake(state)) {
            return BlockPlaceContext.at(pContext, pos, pContext.getClickedFace());
        }
        return null;
    }

    private boolean isValidCake(BlockState state) {
        if (!CommonConfigs.DOUBLE_CAKES.get()) return false;
        Block block = state.getBlock();
        return (block == Blocks.CAKE || block == ModRegistry.DIRECTIONAL_CAKE.get()) && state.getValue(CakeBlock.BITES) == 0;
    }

    @Override
    public BlockState overrideGetPlacementState(BlockPlaceContext pContext) {
        BlockPos pos = pContext.getClickedPos();
        Level level = pContext.getLevel();
        BlockState state = level.getBlockState(pos);
        if (isValidCake(state)) {
            return ModRegistry.DOUBLE_CAKES.get(CakeRegistry.VANILLA).withPropertiesOf(state).setValue(DoubleCakeBlock.FACING,pContext.getHorizontalDirection());
        }
        return null;
    }

}
