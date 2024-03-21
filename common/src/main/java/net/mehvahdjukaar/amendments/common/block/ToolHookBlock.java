package net.mehvahdjukaar.amendments.common.block;

import net.mehvahdjukaar.amendments.common.tile.ToolHookBlockTile;
import net.mehvahdjukaar.moonlight.api.block.ItemDisplayTile;
import net.mehvahdjukaar.moonlight.api.misc.ForgeOverride;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
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
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

public class ToolHookBlock extends Block implements EntityBlock {
    public static final DirectionProperty FACING = HorizontalDirectionalBlock.FACING;
    public static final VoxelShape NORTH_AABB = Block.box(5.0, 5.0, 10.0, 11.0, 15.0, 16.0);
    public static final VoxelShape SOUTH_AABB = Block.box(5.0, 5.0, 0.0, 11.0, 15.0, 6.0);
    public static final VoxelShape WEST_AABB = Block.box(10.0, 5.0, 5.0, 16.0, 15.0, 11.0);
    public static final VoxelShape EAST_AABB = Block.box(0.0, 5.0, 5.0, 6.0, 15.0, 11.0);

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
        for (Direction direction : context.getNearestLookingDirections()) {
            if (direction.getAxis().isHorizontal()) {
                Direction opposite = direction.getOpposite();
                blockState = blockState.setValue(FACING, opposite);
                if (blockState.canSurvive(levelReader, blockPos)) {
                    return blockState;
                }
            }
        }
        return null;
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

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        if (level.getBlockEntity(pos) instanceof ToolHookBlockTile tile) {
            return tile.interact(player, hand);
        }
        return super.use(state, level, pos, player, hand, hit);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new ToolHookBlockTile(pos, state);
    }

    @ForgeOverride
    public ItemStack getCloneItemStack(BlockState state, HitResult target, BlockGetter world, BlockPos pos, Player player) {
        if (world.getBlockEntity(pos) instanceof ItemDisplayTile tile) {
            ItemStack i = tile.getDisplayedItem();
            if (!i.isEmpty()) return i;
        }
        return super.getCloneItemStack(world, pos, state);
    }

    @Override
    public void onRemove(BlockState state, Level world, BlockPos pos, BlockState newState, boolean isMoving) {
        if (state.getBlock() != newState.getBlock()) {
            if (world.getBlockEntity(pos) instanceof ItemDisplayTile tile) {
                Containers.dropContents(world, pos, tile);
                world.updateNeighbourForOutputSignal(pos, this);
            }
            super.onRemove(state, world, pos, newState, isMoving);
        }
    }
}
