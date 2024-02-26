package net.mehvahdjukaar.amendments.common.block;

import net.mehvahdjukaar.amendments.common.tile.ToolHookBlockTile;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

public class ToolHookBlock extends Block implements EntityBlock{
    public static final DirectionProperty FACING = HorizontalDirectionalBlock.FACING;
    public static final VoxelShape NORTH_AABB = Block.box(5.0, 0.0, 10.0, 11.0, 10.0, 16.0);
    public static final VoxelShape SOUTH_AABB = Block.box(5.0, 0.0, 0.0, 11.0, 10.0, 6.0);
    public static final VoxelShape WEST_AABB = Block.box(10.0, 0.0, 5.0, 16.0, 10.0, 11.0);
    public static final VoxelShape EAST_AABB = Block.box(0.0, 0.0, 5.0, 6.0, 10.0, 11.0);

    public ToolHookBlock(BlockBehaviour.Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.NORTH));
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return switch (state.getValue(FACING)) {
            default -> EAST_AABB;
            case WEST -> WEST_AABB;
            case SOUTH -> SOUTH_AABB;
            case NORTH -> NORTH_AABB;
        };
    }

    @Override
    public boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        Direction direction = state.getValue(FACING);
        BlockPos blockPos = pos.relative(direction.getOpposite());
        BlockState blockState = level.getBlockState(blockPos);
        return direction.getAxis().isHorizontal() && blockState.isFaceSturdy(level, blockPos, direction);
    }

    @Override
    public BlockState updateShape(BlockState state, Direction direction, BlockState neighborState, LevelAccessor level, BlockPos pos, BlockPos neighborPos) {
        return direction.getOpposite() == state.getValue(FACING) && !state.canSurvive(level, pos) ? Blocks.AIR.defaultBlockState() : super.updateShape(state, direction, neighborState, level, pos, neighborPos);
    }

    @Nullable
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        BlockState blockState = this.defaultBlockState();
        LevelReader levelReader = context.getLevel();
        BlockPos blockPos = context.getClickedPos();
        Direction[] directions = context.getNearestLookingDirections();
        Direction[] var6 = directions;
        int var7 = directions.length;

        for (int var8 = 0; var8 < var7; ++var8) {
            Direction direction = var6[var8];
            if (direction.getAxis().isHorizontal()) {
                Direction direction2 = direction.getOpposite();
                blockState = blockState.setValue(FACING, direction2);
                if (blockState.canSurvive(levelReader, blockPos)) {
                    return blockState;
                }
            }
        }
        return null;
    }


    private void emitState(Level level, BlockPos pos, boolean attached, boolean powered, boolean wasAttached, boolean wasPowered) {
        if (powered && !wasPowered) {
            level.playSound((Player) null, pos, SoundEvents.TRIPWIRE_CLICK_ON, SoundSource.BLOCKS, 0.4F, 0.6F);
            level.gameEvent((Entity) null, GameEvent.BLOCK_ACTIVATE, pos);
        } else if (!powered && wasPowered) {
            level.playSound((Player) null, pos, SoundEvents.TRIPWIRE_CLICK_OFF, SoundSource.BLOCKS, 0.4F, 0.5F);
            level.gameEvent((Entity) null, GameEvent.BLOCK_DEACTIVATE, pos);
        } else if (attached && !wasAttached) {
            level.playSound((Player) null, pos, SoundEvents.TRIPWIRE_ATTACH, SoundSource.BLOCKS, 0.4F, 0.7F);
            level.gameEvent((Entity) null, GameEvent.BLOCK_ATTACH, pos);
        } else if (!attached && wasAttached) {
            level.playSound((Player) null, pos, SoundEvents.TRIPWIRE_DETACH, SoundSource.BLOCKS, 0.4F, 1.2F / (level.random.nextFloat() * 0.2F + 0.9F));
            level.gameEvent((Entity) null, GameEvent.BLOCK_DETACH, pos);
        }
    }

    @Override
    public BlockState rotate(BlockState state, Rotation rotation) {
        return state.setValue(FACING, rotation.rotate(state.getValue(FACING)));
    }

    @Override
    public BlockState mirror(BlockState state, Mirror mirror) {
        return state.rotate(mirror.getRotation(state.getValue(FACING)));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new ToolHookBlockTile(pos, state);
    }
}
