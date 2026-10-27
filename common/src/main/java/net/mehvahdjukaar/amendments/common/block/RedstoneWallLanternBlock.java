package net.mehvahdjukaar.amendments.common.block;

import com.mojang.serialization.MapCodec;
import net.mehvahdjukaar.amendments.common.LanternRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.redstone.Orientation;
import org.jetbrains.annotations.Nullable;

public class RedstoneWallLanternBlock extends WallLanternBlock {

    public static final MapCodec<RedstoneWallLanternBlock> CODEC = simpleCodec(RedstoneWallLanternBlock::new);

    public RedstoneWallLanternBlock(Properties properties, LanternRegistry.LanternType type) {
        super(properties, type);
    }

    //TODO: add when times comes. this is worng
    @Deprecated
    public RedstoneWallLanternBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected MapCodec<? extends WallLanternBlock> codec() {
        return CODEC;
    }

    @Override
    public BlockState getLanternState(BlockState wallState) {
        BlockState lantern = super.getLanternState(wallState);
        if (lantern.hasProperty(BlockStateProperties.POWERED)) {
            lantern = lantern.setValue(BlockStateProperties.POWERED, !wallState.getValue(LIT));
        }
        return lantern;
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        BlockState state = super.getStateForPlacement(context);
        if (state == null) return null;
        return state.setValue(LIT, !hasWallSignal(context.getLevel(), context.getClickedPos(), state));
    }

    private boolean hasWallSignal(Level level, BlockPos pos, BlockState state) {
        Direction toWall = state.getValue(FACING).getOpposite();
        return level.hasSignal(pos.relative(toWall), toWall);
    }

    @Override
    protected void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean movedByPiston) {
        super.onPlace(state, level, pos, oldState, movedByPiston);
        for (Direction direction : Direction.values()) {
            level.updateNeighborsAt(pos.relative(direction), this, null);
        }

        if (state.getValue(LIT) == hasWallSignal(level, pos, state)) {
            level.scheduleTick(pos, this, 2);
        }
    }

    @Override
    protected void affectNeighborsAfterRemoval(BlockState state, ServerLevel level, BlockPos pos, boolean movedByPiston) {
        if (!movedByPiston) {
            for (Direction direction : Direction.values()) {
                level.updateNeighborsAt(pos.relative(direction), this, null);
            }
        }
    }

    @Override
    public void neighborChanged(BlockState state, Level level, BlockPos pos, Block block, @Nullable Orientation orientation, boolean movedByPiston) {
        if (state.getValue(LIT) == this.hasWallSignal(level, pos, state) && !level.getBlockTicks().willTickThisTick(pos, this)) {
            level.scheduleTick(pos, this, 2);
        }
    }

    @Override
    public void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource rand) {
        boolean shouldBeLit = !this.hasWallSignal(level, pos, state);
        if (shouldBeLit != state.getValue(LIT)) {
            level.setBlock(pos, state.setValue(LIT, shouldBeLit), 3);
        }
    }

    @Override
    protected int getSignal(BlockState state, BlockGetter level, BlockPos pos, Direction side) {
        return state.getValue(LIT) && side.getAxis().isHorizontal() && side != state.getValue(FACING) ? 15 : 0;
    }

    @Override
    protected boolean isSignalSource(BlockState state) {
        return true;
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        if (!state.getValue(LIT)) return;
        Direction facing = state.getValue(FACING);
        double cx = pos.getX() + 0.5 - facing.getStepX() * 0.125;
        double cz = pos.getZ() + 0.5 - facing.getStepZ() * 0.125;

        Direction dir = Direction.Plane.HORIZONTAL.getRandomDirection(random);
        double side = (random.nextDouble() - 0.5) * 0.25;
        double x = cx + dir.getStepX() * 0.21 + dir.getStepZ() * side;
        double z = cz + dir.getStepZ() * 0.21 + dir.getStepX() * side;
        double y = pos.getY() + (3 - type.attachmentOffset * 16) / 16d + random.nextDouble() * 5 / 16d;
        level.addParticle(DustParticleOptions.REDSTONE, x, y, z, 0, 0, 0);
    }
}
