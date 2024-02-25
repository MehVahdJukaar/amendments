package net.mehvahdjukaar.amendments.integration;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.mehvahdjukaar.amendments.common.block.CeilingBannerBlock;
import net.mehvahdjukaar.amendments.common.tile.LiquidCauldronBlockTile;
import net.mehvahdjukaar.amendments.events.behaviors.CauldronConversion;
import net.mehvahdjukaar.moonlight.api.fluids.BuiltInSoftFluids;
import net.mehvahdjukaar.moonlight.api.fluids.SoftFluidStack;
import net.mehvahdjukaar.moonlight.api.fluids.SoftFluidTank;
import net.mehvahdjukaar.supplementaries.client.ModMaterials;
import net.mehvahdjukaar.supplementaries.common.block.IRopeConnection;
import net.mehvahdjukaar.supplementaries.common.block.blocks.EndermanSkullBlock;
import net.mehvahdjukaar.supplementaries.common.block.blocks.GunpowderBlock;
import net.mehvahdjukaar.supplementaries.common.block.blocks.RopeBlock;
import net.mehvahdjukaar.supplementaries.common.block.blocks.StickBlock;
import net.mehvahdjukaar.supplementaries.common.block.faucet.IFaucetBlockTarget;
import net.mehvahdjukaar.supplementaries.common.utils.BlockUtil;
import net.mehvahdjukaar.supplementaries.common.utils.MiscUtils;
import net.mehvahdjukaar.supplementaries.configs.ClientConfigs;
import net.mehvahdjukaar.supplementaries.integration.CompatHandler;
import net.mehvahdjukaar.supplementaries.integration.InspirationCompat;
import net.minecraft.client.resources.model.Material;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.BannerPatternItem;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SkullBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
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
        return IRopeConnection.canConnectDown(neighborState);
    }

    public static boolean isEndermanHead(SkullBlock skull) {
        return skull.getType() == EndermanSkullBlock.TYPE;
    }


    @Environment(EnvType.CLIENT)
    @Nullable
    public static Material getFlagMaterial(BannerPatternItem bannerPatternItem){
        return ModMaterials.getFlagMaterialForPatternItem(bannerPatternItem);
    }
/*
   public static class FaucetCauldronConversion implements IFaucetBlockTarget {

        @Override
        public InteractionResult tryFill(Level level, SoftFluidTank faucetTank, BlockPos pos, BlockState state) {
            if (state.getBlock() == Blocks.CAULDRON) {
                SoftFluidStack fluid = faucetTank.getFluid();

                BlockState newState = CauldronConversion. getNewState(pos, level, fluid);
                if (newState != null) {
                    level.setBlockAndUpdate(pos, newState);
                    if (level.getBlockEntity(pos) instanceof LiquidCauldronBlockTile te) {
                        if (te.handleInteraction(player, hand)) {
                            return InteractionResult.sidedSuccess(level.isClientSide);
                        } else {
                            level.setBlockAndUpdate(pos, state);
                        }
                    }
                    //TODO: allow lava
                }

                if (CompatHandler.INSPIRATIONS) {
                    return InspirationCompat.tryAddFluid(level.getBlockEntity(pos), faucetTank);
                } else if (fluid.is(BuiltInSoftFluids.WATER.get())) {
                    if (state.is(Blocks.WATER_CAULDRON)) {
                        int levels = state.getValue(BlockStateProperties.LEVEL_CAULDRON);
                        if (levels < 3) {
                            level.setBlock(pos, state.setValue(BlockStateProperties.LEVEL_CAULDRON, levels + 1), 3);
                            return InteractionResult.SUCCESS;
                        }
                        return InteractionResult.FAIL;
                    } else if (state.is(Blocks.CAULDRON)) {
                        level.setBlock(pos, Blocks.WATER_CAULDRON.defaultBlockState().setValue(BlockStateProperties.LEVEL_CAULDRON, 1), 3);
                        return InteractionResult.SUCCESS;
                    }
                }
            }
            return InteractionResult.PASS;
        }


    }*/


}
