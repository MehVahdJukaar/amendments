package net.mehvahdjukaar.amendments.integration;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.mehvahdjukaar.amendments.common.block.CeilingBannerBlock;
import net.mehvahdjukaar.amendments.common.tile.LiquidCauldronBlockTile;
import net.mehvahdjukaar.amendments.reg.ModRegistry;
import net.mehvahdjukaar.moonlight.api.fluids.SoftFluidStack;
import net.mehvahdjukaar.moonlight.api.fluids.SoftFluidTank;
import net.mehvahdjukaar.supplementaries.client.ModMaterials;
import net.mehvahdjukaar.supplementaries.common.block.IRopeConnection;
import net.mehvahdjukaar.supplementaries.common.block.blocks.*;
import net.mehvahdjukaar.supplementaries.common.block.faucet.FaucetBehaviorsManager;
import net.mehvahdjukaar.supplementaries.common.block.faucet.FaucetTarget;
import net.mehvahdjukaar.supplementaries.common.block.faucet.FluidOffer;
import net.mehvahdjukaar.supplementaries.common.block.tiles.FaucetBlockTile;
import net.mehvahdjukaar.supplementaries.common.misc.explosion.GunpowderExplosion;
import net.mehvahdjukaar.supplementaries.common.utils.BlockUtil;
import net.mehvahdjukaar.supplementaries.common.utils.MiscUtils;
import net.mehvahdjukaar.supplementaries.configs.ClientConfigs;
import net.mehvahdjukaar.supplementaries.reg.ModEntities;
import net.minecraft.client.renderer.entity.EntityRenderers;
import net.minecraft.client.resources.model.Material;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.BannerPatternItem;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.List;

import static net.mehvahdjukaar.amendments.events.behaviors.CauldronConversion.getNewState;


public class SuppCompat {

    public static void setup() {
        FaucetBehaviorsManager.addRegisterFaucetInteraction(() -> {
            FaucetBlockTile.registerInteraction(new FaucetCauldronConversion());
        });

    }

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

    public static void createMiniExplosion(ServerLevel level, BlockPos pos, boolean b) {
        GunpowderBlock.createMiniExplosion(level, pos, b);
    }

    public static boolean canConnectDown(BlockState neighborState) {
        return IRopeConnection.canConnectDown(neighborState);
    }

    @Environment(EnvType.CLIENT)
    @Nullable
    public static Material getFlagMaterial(Level l, BannerPatternItem bannerPatternItem) {
        return ModMaterials.getFlagMaterialForPatternItem(bannerPatternItem);
    }

    public static boolean isSconce(Block block) {
        if (block instanceof SconceLeverBlock) return true;
        return block instanceof SconceBlock && !(block instanceof SconceWallBlock);
    }

    public static boolean isCandleHolder(Block block) {
        return block instanceof CandleHolderBlock;
    }

    public static EntityType<? extends Entity> getSlimeBall() {
        return ModEntities.THROWABLE_SLIMEBALL.get();
    }


    public static class FaucetCauldronConversion implements FaucetTarget.BlState {

        @Override
        public Integer fill(Level level, BlockPos pos, BlockState target, FluidOffer offer) {
            if (target.is(Blocks.CAULDRON)) {
                SoftFluidStack fluid = offer.fluid();
                int minAmount = offer.minAmount();
                BlockState newState = getNewState(pos, level, fluid);
                if (newState != null) {
                    level.setBlockAndUpdate(pos, newState);
                    if (level.getBlockEntity(pos) instanceof LiquidCauldronBlockTile te) {
                        SoftFluidTank tank = te.getSoftFluidTank();
                        tank.setFluid(fluid.copyWithCount(minAmount));
                        return minAmount;
                    }
                }
            }
            return null;
        }
    }


    public static Vec3 getCandleHolderParticleOffset(BlockState state) {
        if (state.getBlock() instanceof CandleHolderBlock cb) {
            try{
                @SuppressWarnings("unchecked")
                Function<BlockState, List<Vec3>> offsets = (Function<BlockState, List<Vec3>>) OFFSETS.get(cb);
                List<Vec3> particleOffsets = offsets.apply(state);
                if (!particleOffsets.isEmpty()) {
                    return particleOffsets.getFirst().subtract(0.5, 0.5, 0.5); //center it
                }
            }catch (Exception ignored){

            }
            //return cb.particleOffsets.apply(state).get(0);
        }
        return Vec3.ZERO;
    }
}
