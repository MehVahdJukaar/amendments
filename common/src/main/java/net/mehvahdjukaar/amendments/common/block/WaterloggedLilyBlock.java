package net.mehvahdjukaar.amendments.common.block;

import dev.architectury.injectables.annotations.PlatformOnly;
import net.mehvahdjukaar.amendments.common.tile.WaterloggedLilyBlockTile;
import net.mehvahdjukaar.moonlight.api.block.IBlockHolder;
import net.mehvahdjukaar.moonlight.api.platform.ForgeHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class WaterloggedLilyBlock extends WaterlilyBlock implements LiquidBlockContainer, EntityBlock {

    protected static final VoxelShape AABB = Block.box(1.0D, 15.0D, 1.0D, 15.0D, 16D, 15.0D);
    protected static final VoxelShape AABB_SUPPORT = Block.box(0.0D, 15.0D, 0.0D, 16.0D, 16D, 16.0D);

    public WaterloggedLilyBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.defaultBlockState());
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext collisionContext) {
        return AABB;
    }

    @Override
    public VoxelShape getBlockSupportShape(BlockState state, BlockGetter reader, BlockPos pos) {
        return AABB_SUPPORT;
    }

    @Override
    public FluidState getFluidState(BlockState state) {
        return Fluids.WATER.getSource(false);
    }

    @Override
    public BlockState updateShape(BlockState stateIn, Direction facing, BlockState facingState, LevelAccessor worldIn, BlockPos currentPos, BlockPos facingPos) {
        worldIn.scheduleTick(currentPos, Fluids.WATER, Fluids.WATER.getTickDelay(worldIn));
        return stateIn;
    }

    @Override
    public void neighborChanged(BlockState state, Level world, BlockPos pos, Block neighborBlock, BlockPos fromPos, boolean moving) {
        super.neighborChanged(state, world, pos, neighborBlock, fromPos, moving);
        if (pos.above().equals(fromPos)) {
            maybeConvertToVanilla(world, pos);
        }
    }

    @Override
    public void tick(BlockState state, ServerLevel serverLevel, BlockPos pos, RandomSource random) {
        if(!maybeConvertToVanilla(serverLevel, pos)){
            //updates clients
           // serverLevel.sendBlockUpdated(pos, state, state, 3);
        }
        super.tick(state, serverLevel, pos, random);
    }

    private boolean maybeConvertToVanilla(LevelAccessor serverLevel, BlockPos pos) {
        if (serverLevel.getBlockState(pos.above()).isAir() && serverLevel.getBlockEntity(pos) instanceof WaterloggedLilyBlockTile te) {
            serverLevel.setBlock(pos, Blocks.WATER.defaultBlockState(), 3);
            serverLevel.setBlock(pos.above(), te.getHeldBlock(), 3);
            for(var e : serverLevel.getEntitiesOfClass(Entity.class, AABB_SUPPORT.bounds().move(pos).move(0,1/16f,0))){
                if (e.getPistonPushReaction() != PushReaction.IGNORE) {
                    e.move(MoverType.SHULKER_BOX, new Vec3(0, 3/32f,0));
                }
            }
            return true;
        }
        return false;
    }

    @Override
    public boolean canPlaceLiquid(BlockGetter level, BlockPos pos, BlockState state, Fluid fluid) {
        return false;
    }

    @Override
    public boolean placeLiquid(LevelAccessor level, BlockPos pos, BlockState state, FluidState fluidState) {
        return false;
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        return InteractionResult.PASS;
    }

    /**
     * Return a random long to be passed to {@link net.minecraft.client.resources.model.BakedModel#getQuads}, used for
     * random model rotations
     */
    @Override
    public long getSeed(BlockState pState, BlockPos pPos) {
        return Mth.getSeed(pPos.above());
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new WaterloggedLilyBlockTile(pos, state);
    }





    //THIS IS DANGEROUS
    @Override
    public float getDestroyProgress(BlockState state, Player player, BlockGetter worldIn, BlockPos pos) {
        if (worldIn.getBlockEntity(pos) instanceof IBlockHolder tile) {
            BlockState mimicState = tile.getHeldBlock();
            //prevent infinite recursion
            if (!mimicState.isAir() && !(mimicState.getBlock() instanceof WaterloggedLilyBlock))
                return Math.min(super.getDestroyProgress(state, player, worldIn, pos),
                        mimicState.getDestroyProgress(player, worldIn, pos));
        }
        return super.getDestroyProgress(state, player, worldIn, pos);
    }

    //might cause lag when breaking?
    //@Override
    @PlatformOnly(PlatformOnly.FORGE)
    public SoundType getSoundType(BlockState state, LevelReader world, BlockPos pos, Entity entity) {
        if (world.getBlockEntity(pos) instanceof IBlockHolder tile) {
            BlockState mimicState = tile.getHeldBlock();
            if (!mimicState.isAir()) return mimicState.getSoundType();
        }
        return super.getSoundType(state);
    }

    @Override
    public List<ItemStack> getDrops(BlockState state, LootParams.Builder builder) {
        List<ItemStack> drops = super.getDrops(state, builder);
        if (builder.getOptionalParameter(LootContextParams.BLOCK_ENTITY) instanceof IBlockHolder tile) {
            //checks again if the content itself can be mined
            BlockState heldState = tile.getHeldBlock();
            if (builder.getOptionalParameter(LootContextParams.THIS_ENTITY) instanceof ServerPlayer player) {
                if (!ForgeHelper.canHarvestBlock(heldState, builder.getLevel(),
                        BlockPos.containing(builder.getParameter(LootContextParams.ORIGIN)), player)) {
                    return drops;
                }
            }
            List<ItemStack> newDrops = heldState.getDrops(builder);
            drops.addAll(newDrops);
        }
        return drops;
    }

    @Override
    public boolean isPathfindable(BlockState state, BlockGetter worldIn, BlockPos pos, PathComputationType type) {
        return false;
    }

    //@Override
    @PlatformOnly(PlatformOnly.FORGE)
    public float getExplosionResistance(BlockState state, BlockGetter world, BlockPos pos, Explosion explosion) {
        if (world.getBlockEntity(pos) instanceof IBlockHolder tile) {
            BlockState mimicState = tile.getHeldBlock();
            if (!mimicState.isAir() && !(mimicState.getBlock() instanceof WaterloggedLilyBlock)) {
                return Math.max(ForgeHelper.getExplosionResistance(mimicState, (Level) world, pos, explosion),
                        state.getBlock().getExplosionResistance());
            }
        }
        return 2;
    }

    @Override
    public ItemStack getCloneItemStack(BlockGetter level, BlockPos pos, BlockState state) {
        if (level.getBlockEntity(pos) instanceof IBlockHolder tile) {
            BlockState mimic = tile.getHeldBlock();
            return mimic.getBlock().getCloneItemStack(level, pos, state);
        }
        return super.getCloneItemStack(level, pos, state);
    }

}
