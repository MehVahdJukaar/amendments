package net.mehvahdjukaar.amendments.common.block;


import dev.architectury.injectables.annotations.PlatformOnly;
import net.mehvahdjukaar.amendments.common.tile.CandleSkullBlockTile;
import net.mehvahdjukaar.amendments.common.tile.CarpetedBlockTile;
import net.mehvahdjukaar.amendments.reg.ModBlockProperties;
import net.mehvahdjukaar.amendments.reg.ModRegistry;
import net.mehvahdjukaar.moonlight.api.block.IBlockHolder;
import net.mehvahdjukaar.moonlight.api.block.IRecolorable;
import net.mehvahdjukaar.moonlight.api.block.ModStairBlock;
import net.mehvahdjukaar.moonlight.api.misc.ForgeOverride;
import net.mehvahdjukaar.moonlight.api.platform.ForgeHelper;
import net.mehvahdjukaar.moonlight.api.set.BlocksColorAPI;
import net.mehvahdjukaar.moonlight.api.util.math.MthUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.Half;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.stream.IntStream;

public class CarpetStairBlock extends ModStairBlock implements EntityBlock, IRecolorable {

    public static final IntegerProperty LIGHT_LEVEL = ModBlockProperties.LIGHT_LEVEL;
    public static final BooleanProperty SOLID = ModBlockProperties.SOLID;

    public CarpetStairBlock(Block block) {
        super(() -> block, Properties.copy(block)
                .lightLevel(state -> Math.max(0, state.getValue(LIGHT_LEVEL))));
        this.registerDefaultState(this.defaultBlockState().setValue(SOLID, true).setValue(LIGHT_LEVEL, 0));
    }

    protected static final VoxelShape BOTTOM_AABB = Block.box(0.0, 0.0, -1.0, 16.0, 9.0, 16.0);
    protected static final VoxelShape OCTET_NPN = Block.box(0.0, 8.0, 0.0, 9.0, 17.0, 9.0);
    protected static final VoxelShape OCTET_NPP = Block.box(0.0, 8.0, 7.0, 9.0, 17.0, 16.0);
    protected static final VoxelShape OCTET_PPN = Block.box(7.0, 8.0, 0.0, 16.0, 17.0, 9.0);
    protected static final VoxelShape OCTET_PPP = Block.box(7.0, 8.0, 7.0, 16.0, 17.0, 16.0);
    protected static final VoxelShape[] BOTTOM_SHAPES = makeShapes();
    private static final int[] SHAPE_BY_STATE = new int[]{12, 5, 3, 10, 14, 13, 7, 11, 13, 7, 11, 14, 8, 4, 1, 2, 4, 1, 2, 8};

    private static VoxelShape[] makeShapes() {
        return IntStream.range(0, 16)
                .mapToObj(CarpetStairBlock::makeStairShape)
                .toArray(VoxelShape[]::new);
    }

    private static VoxelShape makeStairShape(int bitfield) {
        Direction dir = switch (bitfield % 4) {
            default -> Direction.NORTH;
            case 1 -> Direction.EAST;
            case 2 -> Direction.WEST;
            case 3 -> Direction.SOUTH;
        };

        VoxelShape voxelShape = MthUtils.rotateVoxelShape(BOTTOM_AABB, dir);
        if ((bitfield & 1) != 0) {
            voxelShape = Shapes.or(voxelShape, OCTET_NPN);
        }
        if ((bitfield & 2) != 0) {
            voxelShape = Shapes.or(voxelShape, OCTET_PPN);
        }
        if ((bitfield & 4) != 0) {
            voxelShape = Shapes.or(voxelShape, OCTET_NPP);
        }
        if ((bitfield & 8) != 0) {
            voxelShape = Shapes.or(voxelShape, OCTET_PPP);
        }
        return voxelShape;
    }

    @Override
    public boolean placeLiquid(LevelAccessor level, BlockPos pos, BlockState state, FluidState fluidState) {
        if (!state.getValue(BlockStateProperties.WATERLOGGED) && fluidState.getType() == Fluids.WATER) {
            if (!level.isClientSide() && level.getBlockEntity(pos) instanceof CarpetedBlockTile te && level instanceof Level l) {
                Block.popResource(l, pos, te.getCarpet().getBlock().asItem().getDefaultInstance());
                level.setBlock(pos, te.getSlab().getBlock()
                        .withPropertiesOf(state)
                        .setValue(BlockStateProperties.WATERLOGGED, true), 3);
                level.scheduleTick(pos, fluidState.getType(), fluidState.getType().getTickDelay(level));
            }
            return true;
        } else {
            return false;
        }
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        if (state.getValue(HALF) == Half.BOTTOM) return BOTTOM_SHAPES[SHAPE_BY_STATE[(getShapeIndex(state))]];
        return super.getShape(state, level, pos, context);
    }

    @Override
    public VoxelShape getOcclusionShape(BlockState state, BlockGetter level, BlockPos pos) {
        return state.getValue(SOLID) ? super.getOcclusionShape(state, level, pos) : Shapes.empty();
    }

    private static int getShapeIndex(BlockState state) {
        return state.getValue(SHAPE).ordinal() * 4 + state.getValue(FACING).get2DDataValue();
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(LIGHT_LEVEL, SOLID);
    }

    @Override
    public float getDestroyProgress(BlockState state, Player player, BlockGetter worldIn, BlockPos pos) {
        if (worldIn.getBlockEntity(pos) instanceof IBlockHolder tile) {
            BlockState mimicState = tile.getHeldBlock();
            //prevent infinite recursion
            if (!mimicState.isAir() && !(mimicState.getBlock() instanceof CarpetStairBlock))
                return mimicState.getDestroyProgress(player, worldIn, pos);
        }
        return super.getDestroyProgress(state, player, worldIn, pos);
    }

    //might cause lag when breaking?
    //@Override
    @ForgeOverride
    public SoundType getSoundType(BlockState state, LevelReader world, BlockPos pos, Entity entity) {
        if (world.getBlockEntity(pos) instanceof CarpetedBlockTile tile) {
            SoundType mixed = tile.getSoundType();
            if (mixed != null) return mixed;
        }
        return super.getSoundType(state);
    }

    @Override
    public void destroy(LevelAccessor level, BlockPos pos, BlockState state) {
        super.destroy(level, pos, state);

    }

    @Override
    protected void spawnDestroyParticles(Level level, Player player, BlockPos pos, BlockState state) {
        super.spawnDestroyParticles(level, player, pos, state);
        if (level.getBlockEntity(pos) instanceof IBlockHolder tile) {
            BlockState mimicState = tile.getHeldBlock(1);
            if (!mimicState.isAir()) {
                var sound = mimicState.getSoundType();
                level.playSound(null, pos, sound.getBreakSound(), SoundSource.BLOCKS,
                        (sound.getVolume() + 1.0F) / 2.0F, soundType.getPitch() * 0.8F);

            }
        }
    }

    @Override
    public List<ItemStack> getDrops(BlockState state, LootParams.Builder builder) {
        List<ItemStack> drops = super.getDrops(state, builder);
        if (builder.getOptionalParameter(LootContextParams.BLOCK_ENTITY) instanceof IBlockHolder tile) {
            //checks again if the content itself can be mined
            BlockState heldState = tile.getHeldBlock(0);
            BlockState carpet = tile.getHeldBlock(1);
            if (builder.getOptionalParameter(LootContextParams.THIS_ENTITY) instanceof ServerPlayer player) {
                if (ForgeHelper.canHarvestBlock(heldState, builder.getLevel(), BlockPos.containing(builder.getParameter(LootContextParams.ORIGIN)), player)) {
                    drops.addAll(heldState.getDrops(builder));
                }
            }
            drops.addAll(carpet.getDrops(builder));
        }
        return drops;
    }

    @ForgeOverride
    //@Override
    public ItemStack getCloneItemStack(BlockState state, HitResult target, BlockGetter level, BlockPos pos, Player player) {
        if (level.getBlockEntity(pos) instanceof CarpetedBlockTile tile) {
            if (target instanceof BlockHitResult hs && hs.getDirection() == Direction.UP) {
                return tile.getHeldBlock(1).getBlock().getCloneItemStack(level, pos, state);
            }
            BlockState mimic = tile.getHeldBlock();
            return mimic.getBlock().getCloneItemStack(level, pos, state);
        }
        return super.getCloneItemStack(level, pos, state);
    }

    @Override
    public ItemStack getCloneItemStack(BlockGetter level, BlockPos pos, BlockState state) {
        if (level.getBlockEntity(pos) instanceof CarpetedBlockTile tile) {
            BlockState mimic = tile.getHeldBlock();
            return mimic.getBlock().getCloneItemStack(level, pos, state);
        }
        return super.getCloneItemStack(level, pos, state);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pPos, BlockState pState) {
        return new CarpetedBlockTile(pPos, pState);
    }

    @Override
    public BlockState updateShape(BlockState state, Direction facing, BlockState facingState, LevelAccessor world, BlockPos currentPos,
                                  BlockPos facingPos) {
        var newState = super.updateShape(state, facing, facingState, world, currentPos, facingPos);
        if (world.getBlockEntity(currentPos) instanceof CarpetedBlockTile tile) {
            BlockState oldHeld = tile.getHeldBlock();

            CarpetedBlockTile otherTile = null;
            if (facingState.is(ModRegistry.CARPET_STAIRS.get())) {
                if (world.getBlockEntity(facingPos) instanceof CarpetedBlockTile te2) {
                    otherTile = te2;
                    facingState = otherTile.getHeldBlock();
                }
            }

            BlockState newHeld = oldHeld.updateShape(facing, facingState, world, currentPos, facingPos);

            //manually refreshTextures facing states

            BlockState newFacing = facingState.updateShape(facing.getOpposite(), newHeld, world, facingPos, currentPos);

            if (newFacing != facingState) {
                if (otherTile != null) {
                    otherTile.setHeldBlock(newFacing);
                    otherTile.setChanged();
                } else {
                    world.setBlock(facingPos, newFacing, 2);
                }
            }

            if (newHeld != oldHeld) {
                tile.setHeldBlock(newHeld);
            }
        }

        return newState;
    }


    @Override
    public boolean tryRecolor(Level level, BlockPos blockPos, BlockState blockState, @Nullable DyeColor dyeColor) {
        if (level.getBlockEntity(blockPos) instanceof CarpetedBlockTile tile) {
            var c = tile.getHeldBlock();
            if (!c.isAir()) {
                Block otherCarpet = BlocksColorAPI.changeColor(c.getBlock(), dyeColor);
                if (otherCarpet != null && !c.is(otherCarpet)) {
                    tile.setHeldBlock(otherCarpet.withPropertiesOf(c));
                    tile.setChanged();
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public boolean isDefaultColor(Level level, BlockPos blockPos, BlockState blockState) {
        if (level.getBlockEntity(blockPos) instanceof CarpetedBlockTile tile) {
            var c = tile.getHeldBlock();
            return BlocksColorAPI.isDefaultColor(c.getBlock());
        }
        return false;
    }
}
