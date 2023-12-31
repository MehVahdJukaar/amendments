package net.mehvahdjukaar.amendments.integration;

import net.mehvahdjukaar.amendments.common.block.CeilingBannerBlock;
import net.mehvahdjukaar.supplementaries.common.block.IRopeConnection;
import net.mehvahdjukaar.supplementaries.common.block.blocks.EndermanSkullBlock;
import net.mehvahdjukaar.supplementaries.common.block.blocks.GunpowderBlock;
import net.mehvahdjukaar.supplementaries.common.block.blocks.RopeBlock;
import net.mehvahdjukaar.supplementaries.common.block.blocks.StickBlock;
import net.mehvahdjukaar.supplementaries.common.utils.BlockUtil;
import net.mehvahdjukaar.supplementaries.common.utils.MiscUtils;
import net.mehvahdjukaar.supplementaries.configs.ClientConfigs;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SkullBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public class SuppCompat {

    public static boolean canBannerAttachToRope(BlockState state, BlockState above) {
        if (above.getBlock() instanceof RopeBlock) {
            if (!above.getValue(RopeBlock.DOWN)) {
                Direction dir = state.getValue(CeilingBannerBlock.FACING);
                return above.getValue(RopeBlock.FACING_TO_PROPERTY_MAP.get(dir.getClockWise())) &&
                        above.getValue(RopeBlock.FACING_TO_PROPERTY_MAP.get(dir.getCounterClockWise()));
            }
        }
        return false;
    }

    public static boolean isVerticalStick(BlockState state, Direction facing) {
        return state.getBlock() instanceof StickBlock &&
                (facing.getAxis() == Direction.Axis.X ?
                        !state.getValue(StickBlock.AXIS_X) :
                        !state.getValue(StickBlock.AXIS_Z));
    }

    public static boolean isRope(Block block) {
        return block instanceof RopeBlock;
    }

    public static void spawnCakeParticles(Level level, BlockPos pos, RandomSource rand) {
        if (MiscUtils.FESTIVITY.isStValentine()) {
            if (rand.nextFloat() > 0.8) {
                double d0 = (pos.getX() + 0.5 + (rand.nextFloat() - 0.5));
                double d1 = (pos.getY() + 0.5 + (rand.nextFloat() - 0.5));
                double d2 = (pos.getZ() + 0.5 + (rand.nextFloat() - 0.5));
                level.addParticle(ParticleTypes.HEART, d0, d1, d2, 0, 0, 0);
            }
        }
    }

    public static void addOptionalOwnership(Level world, BlockPos pos, @Nullable LivingEntity entity) {
        BlockUtil.addOptionalOwnership(entity, world, pos);
    }

    public static float getSignColorMult() {
        return ClientConfigs.getSignColorMult();
    }

    public static boolean isSupportingCeiling(BlockState upState, BlockPos pos, LevelReader world) {
        return IRopeConnection.isSupportingCeiling(upState, pos, world);
    }

    public static void createMiniExplosion(Level level, BlockPos pos, boolean b) {
        GunpowderBlock.createMiniExplosion(level, pos, b);
    }

    public static boolean canConnectDown(BlockState neighborState) {
        return true;
    }

    public static boolean isEndermanHead(SkullBlock skull) {
        return skull.getType() == EndermanSkullBlock.TYPE;
    }
}
