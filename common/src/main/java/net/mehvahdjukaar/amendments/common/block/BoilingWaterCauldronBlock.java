package net.mehvahdjukaar.amendments.common.block;

import net.mehvahdjukaar.amendments.reg.ModBlockProperties;
import net.mehvahdjukaar.moonlight.api.fluids.BuiltInSoftFluids;
import net.mehvahdjukaar.moonlight.api.fluids.SoftFluidStack;
import net.minecraft.client.renderer.BiomeColors;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.cauldron.CauldronInteraction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.LayeredCauldronBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.BlockHitResult;

import java.util.Map;
import java.util.function.Predicate;

public class BoilingWaterCauldronBlock extends LayeredCauldronBlock {

    public static final BooleanProperty BOILING = ModBlockProperties.BOILING;

    public BoilingWaterCauldronBlock(Properties properties, Predicate<Biome.Precipitation> fillPredicate,
                                     Map<Item, CauldronInteraction> interactions) {
        super(properties, fillPredicate, interactions);
        this.registerDefaultState(this.defaultBlockState().setValue(BOILING, false));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(BOILING);
    }

    @Override
    public void entityInside(BlockState state, Level level, BlockPos pos, Entity entity) {
        super.entityInside(state, level, pos, entity);
        CommonCauldronCode.entityInside(state, level, pos, entity, () -> this.getContentHeight(state));
    }

    @Override
    public BlockState updateShape(BlockState state, Direction direction, BlockState neighborState, LevelAccessor level, BlockPos currentPos, BlockPos neighborPos) {
        BlockState newState = super.updateShape(state, direction, neighborState, level, currentPos, neighborPos);
        return CommonCauldronCode.updateBoilingState(direction, neighborState, level, neighborPos, newState, currentPos);
    }

    @Override
    public void fallOn(Level level, BlockState state, BlockPos pos, Entity entity, float fallDistance) {
        if (this.isEntityInsideContent(state, pos, entity)) {
            CommonCauldronCode.onEntityFallOnContent(level, state, entity, this.getContentHeight(state));
            super.fallOn(level, state, pos, entity, 0);
        } else super.fallOn(level, state, pos, entity, fallDistance);
    }


    public static int getWaterColor(BlockState state, BlockAndTintGetter level, BlockPos pos, int i) {
        return i == 1 && level != null && pos != null ? BiomeColors.getAverageWaterColor(level, pos) : -1;
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        super.animateTick(state, level, pos, random);
        if (state.getValue(BOILING)) {
            CommonCauldronCode.playBubblingAnimation(level, pos, getContentHeight(state), random,
                    getWaterColor(state, level, pos, 1), 0);
        }
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        if (CommonCauldronCode.attemptPlayerCrafting(state, level, pos, player, hand, 3,
                SoftFluidStack.of(BuiltInSoftFluids.WATER.getHolder(), state.getValue(LEVEL)))) {
            return InteractionResult.sidedSuccess(level.isClientSide);
        }
        return super.use(state, level, pos, player, hand, hit);
    }
}

