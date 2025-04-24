package net.mehvahdjukaar.amendments.common.block;

import com.google.common.base.Preconditions;
import com.google.common.base.Suppliers;
import net.mehvahdjukaar.amendments.common.tile.CeilingBannerBlockTile;
import net.mehvahdjukaar.amendments.integration.CompatHandler;
import net.mehvahdjukaar.amendments.integration.SuppCompat;
import net.mehvahdjukaar.moonlight.api.map.ExpandedMapData;
import net.mehvahdjukaar.moonlight.api.set.BlocksColorAPI;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.MapItem;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.Supplier;

public class CeilingBannerBlock extends AbstractBannerBlock {
    public static final BooleanProperty ATTACHED = BlockStateProperties.ATTACHED;
    public static final DirectionProperty FACING = HorizontalDirectionalBlock.FACING;
    private static final VoxelShape SHAPE_X = Block.box(7.0D, 0.0D, 0.0D, 9.0D, 16.0D, 16.0D);
    private static final VoxelShape SHAPE_Z = Block.box(0.0D, 0.0D, 7.0D, 16.0D, 16.0D, 9.0D);

    public CeilingBannerBlock(DyeColor color, Properties properties) {
        super(color, properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.NORTH).setValue(ATTACHED, false));
    }

    @Override
    public List<ItemStack> getDrops(BlockState blockState, LootParams.Builder builder) {
        return BannerBlock.byColor(this.getColor()).getDrops(blockState, builder);
    }

    @Override
    public boolean canSurvive(BlockState state, LevelReader world, BlockPos pos) {
        BlockState above = world.getBlockState(pos.above());
        if (state.getValue(ATTACHED)) {
            return this.canAttach(state, above);
        }
        return above.isSolid();
    }

    private boolean canAttach(BlockState state, BlockState above) {
        if (CompatHandler.SUPPLEMENTARIES) {
            if (SuppCompat.canBannerAttachToRope(state, above)) return true;
        }
        return false;
    }


    @Override
    public BlockState updateShape(BlockState myState, Direction direction, BlockState otherState, LevelAccessor world, BlockPos myPos, BlockPos otherPos) {
        return direction == Direction.UP && !myState.canSurvive(world, myPos) ? Blocks.AIR.defaultBlockState() : super.updateShape(myState, direction, myState, world, myPos, otherPos);
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext collisionContext) {
        return state.getValue(FACING).getAxis() == Direction.Axis.X ? SHAPE_X : SHAPE_Z;
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        if (context.getClickedFace() == Direction.DOWN) {
            BlockState blockstate = this.defaultBlockState();
            LevelReader world = context.getLevel();
            BlockPos blockpos = context.getClickedPos();
            blockstate = blockstate.setValue(FACING, context.getHorizontalDirection().getOpposite());
            boolean attached = this.canAttach(blockstate, world.getBlockState(blockpos.above()));
            blockstate = blockstate.setValue(ATTACHED, attached);
            if (blockstate.canSurvive(world, blockpos)) {
                return blockstate;
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
        builder.add(FACING, ATTACHED);
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pPos, BlockState pState) {
        return new CeilingBannerBlockTile(pPos, pState, this.getColor());
    }

    @Override
    public void setPlacedBy(Level pLevel, BlockPos pPos, BlockState pState, @Nullable LivingEntity pPlacer, ItemStack pStack) {
        if (pStack.hasCustomHoverName()) {
            if (pLevel.getBlockEntity(pPos) instanceof CeilingBannerBlockTile tile) {
                tile.setCustomName(pStack.getHoverName());
            }
        }
    }

    @Override
    public InteractionResult use(BlockState pState, Level pLevel, BlockPos pPos, Player pPlayer, InteractionHand pHand, BlockHitResult pHit) {
        ItemStack itemstack = pPlayer.getItemInHand(pHand);
        Item item = itemstack.getItem();

        //put post on map
        if (item instanceof MapItem) {
            if (!pLevel.isClientSide) {
                if (MapItem.getSavedData(itemstack, pLevel) instanceof ExpandedMapData data) {
                    data.toggleCustomDecoration(pLevel, pPos);
                }
            }
            return InteractionResult.sidedSuccess(pLevel.isClientSide);
        }
        return super.use(pState, pLevel, pPos, pPlayer, pHand, pHit);
    }


    private String descriptionId;

    @Override
    public String getDescriptionId() {
        if (this.descriptionId == null) {
            Block baseBlock = BlocksColorAPI.getColoredBlock("banner", getColor());
            if (baseBlock != null) {
                this.descriptionId = baseBlock.getDescriptionId();
            } else return "block.amendments.ceiling_banner"; //should never happen

        }
        return descriptionId;
    }
}
