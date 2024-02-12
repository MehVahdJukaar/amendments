package net.mehvahdjukaar.amendments.common.block;


import dev.architectury.injectables.annotations.PlatformOnly;
import net.mehvahdjukaar.amendments.common.tile.CarpetedBlockTile;
import net.mehvahdjukaar.amendments.reg.ModBlockProperties;
import net.mehvahdjukaar.amendments.reg.ModRegistry;
import net.mehvahdjukaar.moonlight.api.block.IBlockHolder;
import net.mehvahdjukaar.moonlight.api.platform.ForgeHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class CarpetSlabBlock extends SlabBlock implements EntityBlock {

    public static final IntegerProperty LIGHT_LEVEL = ModBlockProperties.LIGHT_LEVEL;
    public static final BooleanProperty SOLID = ModBlockProperties.SOLID;
    protected static final VoxelShape BOTTOM_AABB = Block.box(0.0, 0.0, 0.0, 16.0, 9.0, 16.0);

    public CarpetSlabBlock(Block block) {
        super(Properties.copy(block)
                .lightLevel(state -> Math.max(0, state.getValue(LIGHT_LEVEL))));
        this.registerDefaultState(this.defaultBlockState().setValue(SOLID, true).setValue(LIGHT_LEVEL, 0));
    }


    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return BOTTOM_AABB;
    }

    @Override
    public VoxelShape getOcclusionShape(BlockState state, BlockGetter level, BlockPos pos) {
        return state.getValue(SOLID) ? super.getOcclusionShape(state, level, pos) : Shapes.empty();
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
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(LIGHT_LEVEL, SOLID);
    }

    @Override
    public float getDestroyProgress(BlockState state, Player player, BlockGetter worldIn, BlockPos pos) {
        if (worldIn.getBlockEntity(pos) instanceof IBlockHolder tile) {
            BlockState mimicState = tile.getHeldBlock();
            //prevent infinite recursion
            if (!mimicState.isAir() && !(mimicState.getBlock() instanceof CarpetSlabBlock))
                return mimicState.getDestroyProgress(player, worldIn, pos);
        }
        return super.getDestroyProgress(state, player, worldIn, pos);
    }

    //might cause lag when breaking?
    //@Override
    @PlatformOnly(PlatformOnly.FORGE)
    public SoundType getSoundType(BlockState state, LevelReader world, BlockPos pos, Entity entity) {
        if (world.getBlockEntity(pos) instanceof CarpetedBlockTile tile) {
            SoundType mixed = tile.getSoundType();
            if (mixed != null) return mixed;
        }
        return super.getSoundType(state);
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

    @PlatformOnly(PlatformOnly.FORGE)
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

}
