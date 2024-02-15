package net.mehvahdjukaar.amendments.common.block;

import dev.architectury.injectables.annotations.PlatformOnly;
import net.mehvahdjukaar.amendments.common.CakeRegistry;
import net.mehvahdjukaar.amendments.configs.CommonConfigs;
import net.mehvahdjukaar.amendments.integration.CompatHandler;
import net.mehvahdjukaar.amendments.integration.SuppCompat;
import net.mehvahdjukaar.amendments.reg.ModRegistry;
import net.mehvahdjukaar.moonlight.api.platform.ForgeHelper;
import net.mehvahdjukaar.moonlight.api.util.math.MthUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

import java.util.Arrays;

public class DoubleCakeBlock extends DirectionalCakeBlock {
    protected static final VoxelShape[] SHAPES_NORTH = new VoxelShape[]{
            Shapes.or(box(2, 8, 2, 14, 16, 14),
                    box(1, 0, 1, 15, 8, 15)),
            Shapes.or(box(2, 8, 3, 14, 16, 14),
                    box(1, 0, 1, 15, 8, 15)),
            Shapes.or(box(2, 8, 5, 14, 16, 14),
                    box(1, 0, 1, 15, 8, 15)),
            Shapes.or(box(2, 8, 7, 14, 16, 14),
                    box(1, 0, 1, 15, 8, 15)),
            Shapes.or(box(2, 8, 9, 14, 16, 14),
                    box(1, 0, 1, 15, 8, 15)),
            Shapes.or(box(2, 8, 11, 14, 16, 14),
                    box(1, 0, 1, 15, 8, 15)),
            Shapes.or(box(2, 8, 13, 14, 16, 14),
                    box(1, 0, 1, 15, 8, 15))};
    protected static final VoxelShape[] SHAPES_WEST = Arrays.stream(SHAPES_NORTH).map(
            s -> MthUtils.rotateVoxelShape(s, Direction.WEST)
    ).toArray(VoxelShape[]::new);
    protected static final VoxelShape[] SHAPES_SOUTH = Arrays.stream(SHAPES_NORTH).map(
            s -> MthUtils.rotateVoxelShape(s, Direction.SOUTH)
    ).toArray(VoxelShape[]::new);
    protected static final VoxelShape[] SHAPES_EAST = Arrays.stream(SHAPES_NORTH).map(
            s -> MthUtils.rotateVoxelShape(s, Direction.EAST)
    ).toArray(VoxelShape[]::new);
    private final BlockState mimic;

    public DoubleCakeBlock(CakeRegistry.CakeType type) {
        super(type);
        this.mimic = type.cake.defaultBlockState();
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter worldIn, BlockPos pos, CollisionContext context) {
        return switch (state.getValue(FACING)) {
            default -> SHAPES_WEST[state.getValue(BITES)];
            case EAST -> SHAPES_EAST[state.getValue(BITES)];
            case SOUTH -> SHAPES_SOUTH[state.getValue(BITES)];
            case NORTH -> SHAPES_NORTH[state.getValue(BITES)];
        };
    }

    @Override
    public void removeSlice(BlockState state, BlockPos pos, LevelAccessor level, Direction dir) {
        int i = state.getValue(BITES);
        if (i < 6) {
            if (i == 0 && CommonConfigs.DIRECTIONAL_CAKE.get()) state = state.setValue(FACING, dir);
            level.setBlock(pos, state.setValue(BITES, i + 1), 3);
        } else {
            if (this.type == CakeRegistry.VANILLA && state.getValue(WATERLOGGED) && CommonConfigs.DIRECTIONAL_CAKE.get()) {
                level.setBlock(pos, ModRegistry.DIRECTIONAL_CAKE.get().defaultBlockState()
                        .setValue(FACING, state.getValue(FACING)).setValue(WATERLOGGED, state.getValue(WATERLOGGED)), 3);
            } else {
                level.setBlock(pos, type.cake.defaultBlockState(), 3);
            }
        }
    }

    @Override
    public void animateTick(BlockState stateIn, Level level, BlockPos pos, RandomSource rand) {
        if (CompatHandler.SUPPLEMENTARIES) SuppCompat.spawnCakeParticles(level, pos, rand);
        super.animateTick(stateIn, level, pos, rand);
        mimic.getBlock().animateTick(mimic, level, pos, rand);
    }


    @Override
    public float getDestroyProgress(BlockState state, Player player, BlockGetter worldIn, BlockPos pos) {
        return Math.min(super.getDestroyProgress(state, player, worldIn, pos),
                mimic.getDestroyProgress(player, worldIn, pos));
    }

    //@Override
    @PlatformOnly(PlatformOnly.FORGE)
    public SoundType getSoundType(BlockState state, LevelReader world, BlockPos pos, Entity entity) {
        return mimic.getSoundType();
    }

    @Override
    public SoundType getSoundType(BlockState state) {
        return mimic.getSoundType();
    }

    //@Override
    @PlatformOnly(PlatformOnly.FORGE)
    public float getExplosionResistance(BlockState state, BlockGetter level, BlockPos pos, Explosion explosion) {
        return level instanceof Level l ? Math.max(ForgeHelper.getExplosionResistance(mimic, l, pos, explosion),
                state.getBlock().getExplosionResistance()) : super.getExplosionResistance();
    }

    @Override
    public ItemStack getCloneItemStack(BlockGetter level, BlockPos pos, BlockState state) {
        return mimic.getBlock().getCloneItemStack(level, pos, state);
    }


    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand handIn, BlockHitResult hit) {
        //hack
        if (!player.getItemInHand(handIn).is(ItemTags.CANDLES)) {
            BlockState newState = type.cake.withPropertiesOf(state);
            level.setBlock(pos, newState, Block.UPDATE_INVISIBLE);
            var res = newState.use(level, player, handIn, hit);
            level.setBlockAndUpdate(pos, state);
            if (res.consumesAction()) {
                if (!level.isClientSide()) {
                    this.removeSlice(state, pos, level, getHitDir(player, hit));
                }
                return res;
            }
        }

        return super.use(state, level, pos, player, handIn, hit);
    }

}