package net.mehvahdjukaar.amendments.events.behaviors;

import net.mehvahdjukaar.amendments.common.block.CommonCauldronCode;
import net.mehvahdjukaar.amendments.common.block.LiquidCauldronBlock;
import net.mehvahdjukaar.amendments.common.block.ModCauldronBlock;
import net.mehvahdjukaar.amendments.common.tile.LiquidCauldronBlockTile;
import net.mehvahdjukaar.amendments.configs.CommonConfigs;
import net.mehvahdjukaar.amendments.integration.CompatHandler;
import net.mehvahdjukaar.amendments.reg.ModRegistry;
import net.mehvahdjukaar.amendments.reg.ModTags;
import net.mehvahdjukaar.moonlight.api.fluids.BuiltInSoftFluids;
import net.mehvahdjukaar.moonlight.api.fluids.SoftFluidStack;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CauldronBlock;
import net.minecraft.world.level.block.LayeredCauldronBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;

public class CauldronConversion implements BlockUse {

    //block use as it has way too many items that could trigger

    @Override
    public boolean isEnabled() {
        return CommonConfigs.LIQUID_CAULDRON.get();
    }

    @Override
    public boolean appliesToBlock(Block block) {
        return Blocks.CAULDRON == block;
    }

    //maybe move to mixin
    @Override
    public InteractionResult tryPerformingAction(BlockState state, BlockPos pos, Level level, Player player, InteractionHand hand, ItemStack stack, BlockHitResult hit) {
        if (player.isSecondaryUseActive()) return InteractionResult.PASS;
        return convert(state, pos, level, player, hand, stack, true);
    }


    //called by mixin
    public static InteractionResult convert(BlockState state, BlockPos pos, Level level, Player player, InteractionHand hand,
                                            ItemStack stack, boolean checkCauldronInteractions) {
        BlockState newState = getNewState(pos, level, stack, checkCauldronInteractions);
        if (newState != null && level.setBlockAndUpdate(pos, newState)) {

            if (level.getBlockEntity(pos) instanceof LiquidCauldronBlockTile te) {
                if (te.handleInteraction(player, hand)) {
                    return InteractionResult.sidedSuccess(level.isClientSide);
                } else {
                    level.setBlockAndUpdate(pos, state);
                    return InteractionResult.PASS;
                }
            }
        }
        return InteractionResult.PASS;
    }

    public static BlockState getNewState(BlockPos pos, Level level, ItemStack stack) {
        return getNewState(pos, level, stack, true);
    }

    @Nullable
    public static BlockState getNewState(BlockPos pos, Level level, ItemStack fluidBottle, boolean checkCauldronInteractions) {
        var fluid = SoftFluidStack.fromItem(fluidBottle);
        if (fluid == null) return null;
        SoftFluidStack first = fluid.getFirst();
        if (first.is(BuiltInSoftFluids.WATER)) return null;

        if (checkCauldronInteractions && ((CauldronBlock) Blocks.CAULDRON).interactions.containsKey(fluidBottle.getItem())
                && !first.is(BuiltInSoftFluids.POTION)) return null;
        if (CompatHandler.RATS && fluidBottle.is(Items.MILK_BUCKET)) return null;
        return getNewState(pos, level, first);
    }

    @Nullable
    public static BlockState getNewState(BlockPos pos, Level level, SoftFluidStack fluid) {
        if (fluid.isEmpty()) {
            return Blocks.CAULDRON.defaultBlockState();
        }
        //compat stuff here?
        if (fluid.is(BuiltInSoftFluids.WATER)) {
            return Blocks.WATER_CAULDRON.defaultBlockState().setValue(LayeredCauldronBlock.LEVEL,
                            Math.min(3, fluid.getCount()))
                    .setValue(LiquidCauldronBlock.BOILING,
                            CommonCauldronCode.shouldBoil(level.getBlockState(pos.below()), fluid, level, pos.below()));
        } else if (fluid.is(BuiltInSoftFluids.POWDERED_SNOW) && fluid.getCount() == 4) {
            return Blocks.POWDER_SNOW_CAULDRON.defaultBlockState();
        } else if (fluid.is(BuiltInSoftFluids.LAVA) && fluid.getCount() == 4) {
            return Blocks.LAVA_CAULDRON.defaultBlockState();
        }
        if (!fluid.is(ModTags.CAULDRON_BLACKLIST)) {
            BlockState newState;
            if (fluid.is(ModRegistry.DYE_SOFT_FLUID)) {
                newState = ModRegistry.DYE_CAULDRON.get().defaultBlockState();
            } else {
                newState = ModRegistry.LIQUID_CAULDRON.get().defaultBlockState();
            }
            BlockPos belowPos = pos.below();
            return newState.setValue(LiquidCauldronBlock.BOILING,
                    CommonCauldronCode.shouldBoil(level.getBlockState(belowPos), fluid, level, belowPos));
        }
        return null;
    }


    public static void setCorrectCauldronStateAndTile(BlockState state, Level level, BlockPos pos, SoftFluidStack resultFluid) {
        BlockState newState = getNewState(pos, level, resultFluid);
        if (newState != null) {
            if (state != newState) level.setBlockAndUpdate(pos, newState);
            if (level.getBlockEntity(pos) instanceof LiquidCauldronBlockTile te && te.getBlockState().getBlock() instanceof ModCauldronBlock mc) {
                te.getSoftFluidTank().setFluid(resultFluid);
                te.setChanged();
            }
        }
    }

}
