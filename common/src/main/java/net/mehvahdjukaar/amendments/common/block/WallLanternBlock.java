package net.mehvahdjukaar.amendments.common.block;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import net.mehvahdjukaar.amendments.common.network.ClientBoundEntityHitSwayingBlockMessage;
import net.mehvahdjukaar.amendments.common.network.ModNetwork;
import net.mehvahdjukaar.amendments.common.tile.SwayingBlockTile;
import net.mehvahdjukaar.amendments.common.tile.WallLanternBlockTile;
import net.mehvahdjukaar.amendments.configs.CommonConfigs;
import net.mehvahdjukaar.amendments.integration.CompatHandler;
import net.mehvahdjukaar.amendments.integration.SuppCompat;
import net.mehvahdjukaar.amendments.integration.SuppSquaredCompat;
import net.mehvahdjukaar.amendments.integration.ThinAirCompat;
import net.mehvahdjukaar.amendments.reg.ModBlockProperties;
import net.mehvahdjukaar.amendments.reg.ModRegistry;
import net.mehvahdjukaar.moonlight.api.block.IBlockHolder;
import net.mehvahdjukaar.moonlight.api.block.WaterBlock;
import net.mehvahdjukaar.moonlight.api.platform.network.NetworkHelper;
import net.mehvahdjukaar.moonlight.api.util.Utils;
import net.mehvahdjukaar.moonlight.api.util.math.MthUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.*;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class WallLanternBlock extends WaterBlock implements EntityBlock {

    public static final MapCodec<WallLanternBlock> CODEC = simpleCodec(WallLanternBlock::new);

    public static final VoxelShape SHAPE_NORTH = Block.box(5, 2, 6, 11, 15.99, 16);
    public static final VoxelShape SHAPE_SOUTH = MthUtils.rotateVoxelShape(SHAPE_NORTH, Direction.SOUTH);
    public static final VoxelShape SHAPE_WEST = MthUtils.rotateVoxelShape(SHAPE_NORTH, Direction.WEST);
    public static final VoxelShape SHAPE_EAST = MthUtils.rotateVoxelShape(SHAPE_NORTH, Direction.EAST);

    public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;
    public static final EnumProperty<ModBlockProperties.BlockAttachment> ATTACHMENT = ModBlockProperties.BLOCK_ATTACHMENT;
    public static final BooleanProperty LIT = BlockStateProperties.LIT;
    public static final IntegerProperty LIGHT_LEVEL = ModBlockProperties.LIGHT_LEVEL;

    public WallLanternBlock(Properties properties) {
        super(properties.lightLevel(s -> s.getValue(LIT) ? s.getValue(LIGHT_LEVEL) : 0));
        this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.NORTH)
                .setValue(LIGHT_LEVEL, 15).setValue(WATERLOGGED, false).setValue(LIT, true));
    }

    @Override
    protected MapCodec<? extends WallLanternBlock> codec() {
        return CODEC;
    }

    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hitResult) {
        if (level.getBlockEntity(pos) instanceof WallLanternBlockTile te && te.isAccessibleBy(player)) {
            BlockState lantern = te.getHeldBlock();
            if (CompatHandler.SUPPSQUARED) {
                ItemInteractionResult res = SuppSquaredCompat.lightUpLantern(level, pos, player, hand, stack, te, lantern);
                if (res.consumesAction()) return res;
            }
        }
        return super.useItemOn(stack, state, level, pos, player, hand, hitResult);
    }


    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        if (context.getClickedFace().getAxis() == Direction.Axis.Y) return null;
        BlockState state = super.getStateForPlacement(context);

        BlockPos blockpos = context.getClickedPos();
        Level world = context.getLevel();
        Direction dir = context.getClickedFace();
        BlockPos relative = blockpos.relative(dir.getOpposite());
        BlockState facingState = world.getBlockState(relative);

        return getConnectedState(state, facingState, world, relative, dir).setValue(FACING, context.getClickedFace());
    }

    @Override
    public void setPlacedBy(Level world, BlockPos pos, BlockState state, @Nullable LivingEntity entity, ItemStack stack) {
        BlockEntity te = world.getBlockEntity(pos);
        Item i = stack.getItem();
        if (te instanceof WallLanternBlockTile blockHolder && i instanceof BlockItem blockItem) {
            blockHolder.setHeldBlock(blockItem.getBlock().defaultBlockState());
        }
        if (CompatHandler.SUPPLEMENTARIES) SuppCompat.addOptionalOwnership(world, pos, entity);
    }


    @Override
    public BlockState updateShape(BlockState stateIn, Direction facing, BlockState facingState, LevelAccessor worldIn, BlockPos currentPos,
                                  BlockPos facingPos) {
        super.updateShape(stateIn, facing, facingState, worldIn, currentPos, facingPos);
        return facing == stateIn.getValue(FACING).getOpposite() ? !stateIn.canSurvive(worldIn, currentPos)
                ? Blocks.AIR.defaultBlockState()
                : getConnectedState(stateIn, facingState, worldIn, facingPos, facing) : stateIn;
    }

    @Override
    public boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        Direction direction = state.getValue(FACING);
        BlockPos blockpos = pos.relative(direction.getOpposite());
        BlockState blockstate = level.getBlockState(blockpos);
        return ModBlockProperties.BlockAttachment.get(blockstate, blockpos, level, direction) != null;
    }

    @Override
    public BlockState rotate(BlockState state, Rotation rot) {
        return state.setValue(FACING, rot.rotate(state.getValue(FACING)));
    }

    @Override
    public BlockState mirror(BlockState state, Mirror mirrorIn) {
        return state.rotate(mirrorIn.getRotation(state.getValue(FACING)));
    }

    public static BlockState getConnectedState(BlockState state, BlockState facingState, LevelAccessor world, BlockPos pos, Direction dir) {
        ModBlockProperties.BlockAttachment attachment = ModBlockProperties.BlockAttachment.get(facingState, pos, world, dir);
        if (attachment == null) {
            return state;
        }
        return state.setValue(ATTACHMENT, attachment);
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter worldIn, BlockPos pos, CollisionContext context) {
        return switch (state.getValue(FACING)) {
            case NORTH -> SHAPE_NORTH;
            case WEST -> SHAPE_WEST;
            case EAST -> SHAPE_EAST;
            default -> SHAPE_SOUTH;
        };
    }

    @Override
    public ItemStack getCloneItemStack(LevelReader level, BlockPos pos, BlockState state) {
        if (level.getBlockEntity(pos) instanceof WallLanternBlockTile te) {
            return new ItemStack(te.getHeldBlock().getBlock());
        }
        return new ItemStack(Blocks.LANTERN, 1);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(LIGHT_LEVEL, LIT, FACING, ATTACHMENT);
    }

    @Override
    public void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource rand) {
        super.tick(state, level, pos, rand);
        if (level.getBlockEntity(pos) instanceof WallLanternBlockTile te) {
            if (te.isRedstoneLantern()) {
                if (state.getValue(LIT) && !level.hasNeighborSignal(pos)) {
                    level.setBlock(pos, state.cycle(LIT), 2);
                    if (te.getHeldBlock().hasProperty(LIT))
                        te.setHeldBlock(te.getHeldBlock().cycle(LIT));
                }
            }
            if (CompatHandler.THIN_AIR) {
                BlockState lantern = te.getHeldBlock();
                if (ThinAirCompat.isAirLantern(lantern)) {
                    te.setHeldBlock(lantern); //this automatically updates it
                    if (te.getHeldBlock() != lantern) {
                        level.sendBlockUpdated(pos, state, state, 3);
                    }
                }
            }
        }
    }

    //i could reference held lantern block directly but maybe it's more efficient this way idk
    @Override
    public void neighborChanged(BlockState state, Level world, BlockPos pos, Block block, BlockPos fromPos, boolean notify) {
        if (!world.isClientSide) {
            if (world.getBlockEntity(pos) instanceof WallLanternBlockTile tile && tile.isRedstoneLantern()) {
                boolean flag = state.getValue(LIT);
                if (flag != world.hasNeighborSignal(pos)) {
                    if (flag) {
                        world.scheduleTick(pos, this, 4);
                    } else {
                        world.setBlock(pos, state.cycle(LIT), 2);
                        if (tile.getHeldBlock().hasProperty(LIT))
                            tile.setHeldBlock(tile.getHeldBlock().cycle(LIT));
                    }
                }
            }
        }
    }

    @Override
    public List<ItemStack> getDrops(BlockState state, LootParams.Builder builder) {
        if (builder.getOptionalParameter(LootContextParams.BLOCK_ENTITY) instanceof WallLanternBlockTile tile) {
            return tile.getHeldBlock().getDrops(builder);
        }
        return super.getDrops(state, builder);
    }


    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        if (level.getBlockEntity(pos) instanceof WallLanternBlockTile tile) {
            BlockState s = tile.getHeldBlock();
            s.getBlock().animateTick(s, level, pos, random);
        }
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pPos, BlockState pState) {
        return new WallLanternBlockTile(pPos, pState);
    }

    @Override
    public void entityInside(BlockState state, Level level, BlockPos pos, Entity entity) {
        super.entityInside(state, level, pos, entity);
        if (level.isClientSide) {
            if (level.getBlockEntity(pos) instanceof WallLanternBlockTile tile) {
                tile.amendments$getAnimation().hitByEntity(entity, state, pos);
            }
        } else  {
            if (entity.xo != entity.getX() || entity.zo != entity.getZ() || entity.yo != entity.getY()) {
                level.gameEvent(entity, GameEvent.BLOCK_ACTIVATE, pos);
            }
            NetworkHelper.sendToAllClientPlayersTrackingEntity(entity, new ClientBoundEntityHitSwayingBlockMessage(pos, entity.getId()));
        }
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level pLevel, BlockState pState, BlockEntityType<T> pBlockEntityType) {
        return Utils.getTicker(pBlockEntityType, ModRegistry.WALL_LANTERN_TILE.get(), pLevel.isClientSide ? SwayingBlockTile::clientTick : null);
    }

   /* @Override
    public SoundType getSoundType(BlockState state, LevelReader world, BlockPos pos, @Nullable Entity entity) {
        if (world.getBlockEntity(pos) instanceof WallLanternBlockTile te) {
            return te.getHeldBlock().getSoundType();
        }
        return super.getSoundType(state, world, pos, entity);
    } */

    public void placeOn(BlockState lantern, BlockPos onPos, Direction face, Level world) {
        BlockState state = getConnectedState(this.defaultBlockState(), world.getBlockState(onPos), world, onPos, face)
                .setValue(FACING, face);
        BlockPos newPos = onPos.relative(face);
        world.setBlock(newPos, state, 3);
        if (world.getBlockEntity(newPos) instanceof IBlockHolder tile) {
            tile.setHeldBlock(lantern);
        }
    }

    public static boolean isValidBlock(Block b) {
        if (b.asItem() == Items.AIR) return false;
        ResourceLocation id = Utils.getID(b);
        String namespace = id.getNamespace();
        if (CommonConfigs.WALL_LANTERN_WHITELIST.get().contains(id.toString())) {
            return true;
        }
        if (CommonConfigs.WALL_LANTERN_BLACKLIST.get().contains(namespace)) return false;
        if (namespace.equals("skinnedlanterns") || (namespace.equals("twigs") && id.getPath().contains("paper_lantern")))
            return true;
        if (b instanceof LanternBlock) {
            return !b.defaultBlockState().hasBlockEntity() || (CompatHandler.SUPPSQUARED && SuppSquaredCompat.isLightableLantern(b));
        }
        return false;
    }

}