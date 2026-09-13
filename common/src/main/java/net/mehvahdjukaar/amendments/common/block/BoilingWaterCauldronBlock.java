package net.mehvahdjukaar.amendments.common.block;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.mehvahdjukaar.amendments.reg.ModBlockProperties;
import net.mehvahdjukaar.moonlight.api.fluids.MLBuiltinSoftFluids;
import net.mehvahdjukaar.moonlight.api.fluids.SoftFluidStack;
import net.minecraft.client.color.block.BlockTintSource;
import net.minecraft.client.renderer.BiomeColors;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.cauldron.CauldronInteractions;
import net.minecraft.core.cauldron.CauldronInteraction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ScheduledTickAccess;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.LayeredCauldronBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.entity.InsideBlockEffectApplier;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.BlockHitResult;

public class BoilingWaterCauldronBlock extends LayeredCauldronBlock {
    public static final MapCodec<BoilingWaterCauldronBlock> CODEC = RecordCodecBuilder.mapCodec((i) -> i.group(
            Biome.Precipitation.CODEC.fieldOf("precipitation").forGetter((c) -> c.precipitationType),
            CauldronInteractions.CODEC.fieldOf("interactions").forGetter((c) -> c.interactions),
            propertiesCodec()).apply(i, BoilingWaterCauldronBlock::new));

    public static final BooleanProperty BOILING = ModBlockProperties.BOILING;
    private final Biome.Precipitation precipitationType;

    public BoilingWaterCauldronBlock(Biome.Precipitation precipitationType,
                                     CauldronInteraction.Dispatcher interactions,
                                     Properties properties) {
        super(precipitationType, interactions, properties);
        this.precipitationType = precipitationType;
        this.registerDefaultState(this.defaultBlockState().setValue(BOILING, false));
    }

    @SuppressWarnings("all")
    @Override
    public MapCodec codec() {
        return CODEC;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(BOILING);
    }

    @Override
    public void entityInside(BlockState state, Level level, BlockPos pos, Entity entity, InsideBlockEffectApplier effectApplier, boolean isPrecise) {
        super.entityInside(state, level, pos, entity, effectApplier, isPrecise);
        CommonCauldronCode.entityInside(state, level, pos, entity, () -> this.getContentHeight(state));
    }

    @Override
    public BlockState updateShape(BlockState state, LevelReader level, ScheduledTickAccess ticks, BlockPos currentPos, Direction direction, BlockPos neighborPos, BlockState neighborState, RandomSource randomSource) {
        BlockState newState = super.updateShape(state, level, ticks, currentPos, direction, neighborPos, neighborState, randomSource);
        return CommonCauldronCode.updateBoilingState(direction, neighborState, level, neighborPos, newState, currentPos);
    }

    // No block entity here, so water-level changes (bottle, dripstone, rain, vanilla interactions)
    // never pass through updateShape. onPlace fires on every server-side state change at this pos
    // (see LevelChunk#setBlockState), so recompute boiling here against the current heat source.
    @Override
    public void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean movedByPiston) {
        super.onPlace(state, level, pos, oldState, movedByPiston);
        if (level.isClientSide()) return;
        BlockPos below = pos.below();
        boolean shouldBoil = CommonCauldronCode.shouldBoil(level.getBlockState(below),
                SoftFluidStack.of(MLBuiltinSoftFluids.WATER.getHolder(level), state.getValue(LEVEL)), level, below);
        if (state.getValue(BOILING) != shouldBoil) {
            // re-entrant setBlock is safe (no BE to desync); the guard makes the recursive onPlace a no-op
            level.setBlock(pos, state.setValue(BOILING, shouldBoil), Block.UPDATE_ALL);
        }
    }

    @Override
    public void fallOn(Level level, BlockState state, BlockPos pos, Entity entity, double fallDistance) {
        if (CommonCauldronCode.isEntityInsideContent(pos, entity, this.getContentHeight(state))) {
            CommonCauldronCode.onEntityFallOnContent(level, state, entity, this.getContentHeight(state));
            super.fallOn(level, state, pos, entity, 0);
        } else super.fallOn(level, state, pos, entity, fallDistance);
    }


    public static int getWaterColor(BlockState state, BlockAndTintGetter level, BlockPos pos, int i) {
        return i == 1 && level != null && pos != null ? BiomeColors.getAverageWaterColor(level, pos) : -1;
    }

    public static BlockTintSource waterColor() {
        return new BlockTintSource() {
            @Override
            public int color(BlockState state) {
                return -1;
            }

            @Override
            public int colorInWorld(BlockState state, BlockAndTintGetter level, BlockPos pos) {
                return BiomeColors.getAverageWaterColor(level, pos);
            }
        };
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        super.animateTick(state, level, pos, random);
        if (state.getValue(BOILING) && level instanceof BlockAndTintGetter tintGetter) {
            CommonCauldronCode.playBubblingAnimation(level, pos, getContentHeight(state), random,
                    getWaterColor(state, tintGetter, pos, 1), 0);
        }
    }


    @Override
    protected InteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hitResult) {
        if (CommonCauldronCode.attemptPlayerCrafting(state, level, pos, player, hand, 3,
                SoftFluidStack.of(MLBuiltinSoftFluids.WATER.getHolder(level), state.getValue(LEVEL)))) {
            return InteractionResult.SUCCESS;
        }
        return super.useItemOn(stack, state, level, pos, player, hand, hitResult);

    }
}

