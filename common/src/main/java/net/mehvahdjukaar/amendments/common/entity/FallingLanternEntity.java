package net.mehvahdjukaar.amendments.common.entity;

import net.mehvahdjukaar.amendments.Amendments;
import net.mehvahdjukaar.amendments.configs.CommonConfigs;
import net.mehvahdjukaar.amendments.integration.CompatHandler;
import net.mehvahdjukaar.amendments.integration.SuppCompat;
import net.mehvahdjukaar.amendments.reg.ModRegistry;
import net.mehvahdjukaar.moonlight.api.entity.ImprovedFallingBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.item.FallingBlockEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;

public class FallingLanternEntity extends ImprovedFallingBlockEntity {

    public FallingLanternEntity(EntityType<FallingLanternEntity> type, Level level) {
        super(type, level);
    }

    public FallingLanternEntity(Level level) {
        super(ModRegistry.FALLING_LANTERN.get(), level);
    }

    public FallingLanternEntity(Level level, BlockPos pos, BlockState blockState, double yOffset) {
        super(ModRegistry.FALLING_LANTERN.get(), level, pos, blockState, false);
        this.yo = pos.getY() + yOffset;
    }

    public static FallingBlockEntity fall(Level level, BlockPos pos, BlockState state, double yOffset) {
        FallingLanternEntity entity = new FallingLanternEntity(level, pos, state, yOffset);
        level.setBlock(pos, state.getFluidState().createLegacyBlock(), 3);
        level.addFreshEntity(entity);
        return entity;
    }

    @Override
    public boolean causeFallDamage(float height, float amount, DamageSource source) {
        boolean r = super.causeFallDamage(height, amount, source);
        if (CommonConfigs.FALLING_LANTERNS.get().hasFire() && this.getDeltaMovement().lengthSqr() > 0.4 * 0.4) {
            BlockState state = this.getBlockState();

            BlockPos pos = BlockPos.containing(this.getX(), this.getY() + 0.25, this.getZ());
            //break event
            Level level = level();
            level.levelEvent(null, LevelEvent.PARTICLES_DESTROY_BLOCK, pos, Block.getId(state));
            if (state.getLightEmission() != 0) {

                if (CompatHandler.SUPPLEMENTARIES && level instanceof ServerLevel l) {
                    SuppCompat.createMiniExplosion(l, pos, true);
                } else if (level.getBlockState(pos).isAir()) {
                    if (BaseFireBlock.canBePlacedAt(level, pos, Direction.DOWN)) {
                        level.setBlockAndUpdate(pos, BaseFireBlock.getState(level, pos));
                    }
                }
            } else {
                this.spawnAtLocation(state.getBlock());
            }
            this.setCancelDrop(true);
            this.discard();
        }
        return r;
    }


    //TODO: hitting sounds
    //called by mixin
    public static boolean canSurviveCeilingAndMaybeFall(BlockState state, BlockPos pos, LevelReader worldIn) {
        if (!Amendments.isSupportingCeiling(pos.above(), worldIn) && worldIn instanceof Level l) {
            if (CommonConfigs.FALLING_LANTERNS.get().isOn() && l.getBlockState(pos).is(state.getBlock())) {
                return createFallingLantern(state, pos, l);
            }
            return false;
        }
        return true;
    }

    public static boolean createFallingLantern(BlockState state, BlockPos pos, Level level) {
        if (FallingBlock.isFree(level.getBlockState(pos.below())) && pos.getY() >= level.getMinBuildHeight()) {
            if (state.hasProperty(LanternBlock.HANGING)) {
                double maxY = state.getShape(level, pos).bounds().maxY;
                state = state.setValue(LanternBlock.HANGING, false);
                double yOffset = maxY - state.getShape(level, pos).bounds().maxY;
                FallingLanternEntity.fall(level, pos, state, yOffset);
                return true;
            }
        }
        return false;
    }

    public enum FallMode {
        ON,
        OFF,
        NO_FIRE;

        public boolean hasFire() {
            return this != NO_FIRE;
        }

        public boolean isOn() {
            return this != OFF;
        }
    }

}
