package net.mehvahdjukaar.amendments.common.block;

import net.mehvahdjukaar.amendments.common.tile.LiquidCauldronBlockTile;
import net.mehvahdjukaar.amendments.reg.ModBlockProperties;
import net.mehvahdjukaar.amendments.reg.ModRegistry;
import net.mehvahdjukaar.moonlight.api.fluids.SoftFluidStack;
import net.mehvahdjukaar.moonlight.api.fluids.SoftFluidTank;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.AbstractCauldronBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

public abstract class ModCauldronBlock extends AbstractCauldronBlock implements EntityBlock {

    public static final BooleanProperty BOILING = ModBlockProperties.BOILING;
    private final int maxLevel;

    public ModCauldronBlock(BlockBehaviour.Properties properties) {
        super(properties, Map.of());
        this.registerDefaultState(this.defaultBlockState()
                .setValue(BOILING, false));
        this.maxLevel = this.getLevelProperty().getPossibleValues().size(); //assumes it counts from 1
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(BOILING);
    }

    @Override
    public boolean isFull(BlockState state) {
        return state.getValue(getLevelProperty()) == maxLevel;
    }

    @Override
    public BlockState updateShape(BlockState state, Direction direction, BlockState neighborState, LevelAccessor level, BlockPos currentPos, BlockPos neighborPos) {
        BlockState newState = super.updateShape(state, direction, neighborState, level, currentPos, neighborPos);
        return CommonCauldronCode.updateBoilingState(direction, neighborState, level, neighborPos, newState, currentPos);
    }

    @Override
    public Item asItem() {
        return Items.CAULDRON;
    }

    public abstract IntegerProperty getLevelProperty();

    @Override
    public int getAnalogOutputSignal(BlockState state, Level level, BlockPos pos) {
        return state.getValue(getLevelProperty());
    }

    @Override
    public void handlePrecipitation(BlockState state, Level level, BlockPos pos, Biome.Precipitation precipitation) {
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new LiquidCauldronBlockTile(pos, state);
    }

    @Override
    protected double getContentHeight(BlockState state) {
        double start = 0.5625;
        double end = 0.9375;
        IntegerProperty levelProperty = getLevelProperty();
        int level = state.getValue(levelProperty);
        if (maxLevel <= 1) return start; // avoid divide-by-zero
        return start + (end - start) * (level - 1) / (maxLevel - 1);
    }

    @Override
    public void fallOn(Level level, BlockState state, BlockPos pos, Entity entity, float fallDistance) {
        if (isEntityInsideContent(state, pos, entity)) {
            CommonCauldronCode.onEntityFallOnContent(level, state, entity, this.getContentHeight(state));

            super.fallOn(level, state, pos, entity, 0);
        } else super.fallOn(level, state, pos, entity, fallDistance);
    }


    @Override
    public void entityInside(BlockState state, Level level, BlockPos pos, Entity entity) {
        if (isEntityInsideContent(state, pos, entity)) {
            CommonCauldronCode.entityInside(state, level, pos, entity, () -> this.getContentHeight(state));

            handleEntityInsideFluidSpecial(state, level, pos, entity);
        }
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        super.animateTick(state, level, pos, random);
        if (state.getValue(BOILING) && level.getBlockEntity(pos) instanceof LiquidCauldronBlockTile te) {
            SoftFluidTank tank = te.getSoftFluidTank();
            int color = tank.getCachedParticleColor(level, pos);
            int light = tank.getFluidValue().getEmissivity();
            CommonCauldronCode.playBubblingAnimation(level, pos, getContentHeight(state), random, color, light);
        }
    }

    protected abstract void handleEntityInsideFluidSpecial(BlockState state, Level level, BlockPos pos, Entity entity);


    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        if (level.getBlockEntity(pos) instanceof LiquidCauldronBlockTile te) {
            if (te.handleInteraction(player, hand)) {
                return InteractionResult.sidedSuccess(level.isClientSide);
            }

            SoftFluidTank tank = te.getSoftFluidTank();

            //TODO: add this but to also boiling cauldron
            //try recoloring
            int tankCapacity = tank.getCapacity();
            SoftFluidStack currentFluid = tank.getFluid();

            if (CommonCauldronCode.attemptPlayerCrafting(state, level, pos, player, hand, tankCapacity, currentFluid)) {
                return InteractionResult.sidedSuccess(level.isClientSide);
            }
        }
        return InteractionResult.PASS;
    }

    public BlockState updateStateOnFluidChange(BlockState state, Level level, BlockPos pos, SoftFluidStack fluid) {
        int height = fluid.getCount();
        if (fluid.isEmpty()) {
            state = Blocks.CAULDRON.defaultBlockState();
        } else {
            state = state.setValue(getLevelProperty(), height);
        }
        return state;
    }

}
