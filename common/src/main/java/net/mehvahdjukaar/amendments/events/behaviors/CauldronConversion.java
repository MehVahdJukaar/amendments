package net.mehvahdjukaar.amendments.events.behaviors;

import net.mehvahdjukaar.amendments.common.block.CommonCauldronCode;
import net.mehvahdjukaar.amendments.common.block.LiquidCauldronBlock;
import net.mehvahdjukaar.amendments.common.block.ModCauldronBlock;
import net.mehvahdjukaar.amendments.common.tile.LiquidCauldronBlockTile;
import net.mehvahdjukaar.amendments.configs.CommonConfigs;
import net.mehvahdjukaar.amendments.integration.CompatHandler;
import net.mehvahdjukaar.amendments.reg.ModRegistry;
import net.mehvahdjukaar.amendments.reg.ModTags;
import net.mehvahdjukaar.moonlight.api.fluids.MLBuiltinSoftFluids;
import net.mehvahdjukaar.moonlight.api.fluids.SoftFluidStack;
import net.mehvahdjukaar.moonlight.api.fluids.SoftFluidTank;
import net.mehvahdjukaar.moonlight.api.util.DispenserHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.dispenser.BlockSource;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;

//TODO:
@Deprecated(forRemoval = true)
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
        return null;// convert(state, pos, level, player, hand, stack, true);
    }


    //called by mixin
    public static ItemInteractionResult convert(BlockState state, BlockPos pos, Level level, Player player, InteractionHand hand,
                                                ItemStack stack, boolean checkCauldronInteractions) {
        BlockState newState = getNewState(pos, level, stack, checkCauldronInteractions);
        if (newState != null && level.setBlockAndUpdate(pos, newState)) {

            if (level.getBlockEntity(pos) instanceof LiquidCauldronBlockTile te) {
                if (te.interactWithPlayerItem(player, hand, stack)) {
                    return ItemInteractionResult.sidedSuccess(level.isClientSide);
                } else {
                    level.setBlockAndUpdate(pos, state);
                    return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
                }
            }
        }
        return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
    }

    public static BlockState getNewState(BlockPos pos, Level level, ItemStack stack) {
        return getNewState(pos, level, stack, true);
    }

    @Nullable
    public static BlockState getNewState(BlockPos pos, Level level, ItemStack fluidBottle, boolean checkCauldronInteractions) {
        var fluid = SoftFluidStack.fromItem(fluidBottle);
        if (fluid == null) return null;
        SoftFluidStack first = fluid.getFirst();

        if (checkCauldronInteractions && ((CauldronBlock) Blocks.CAULDRON).interactions.map().containsKey(fluidBottle.getItem())
                && !first.is(MLBuiltinSoftFluids.POTION)) return null;
        if (CompatHandler.RATS && fluidBottle.is(Items.MILK_BUCKET)) return null;
        return getNewState(pos, level, first);
    }

    @Nullable
    public static BlockState getNewState(BlockPos pos, Level level, SoftFluidStack fluid) {
        //compat stuff here?
        if (fluid.is(MLBuiltinSoftFluids.WATER)) {
            return Blocks.WATER_CAULDRON.defaultBlockState().setValue(LayeredCauldronBlock.LEVEL,
                            Math.min(3, fluid.getCount()))
                    .setValue(LiquidCauldronBlock.BOILING,
                            CommonCauldronCode.shouldBoil(level.getBlockState(pos.below()), fluid, level, pos.below()));
        } else if (fluid.is(MLBuiltinSoftFluids.POWDERED_SNOW) && fluid.getCount() == 4) {
            return Blocks.POWDER_SNOW_CAULDRON.defaultBlockState();
        } else if (fluid.is(MLBuiltinSoftFluids.LAVA) && fluid.getCount() == 4) {
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


    public static class DispenserBehavior extends DispenserHelper.AdditionalDispenserBehavior {

        public DispenserBehavior(Item item) {
            super(item);
        }

        @Override
        protected InteractionResultHolder<ItemStack> customBehavior(BlockSource source, ItemStack stack) {
            //this.setSuccessful(false);
            ServerLevel level = source.level();
            BlockState originalState = source.state();
            BlockPos pos = source.pos().relative(originalState.getValue(DispenserBlock.FACING));
            if (!originalState.is(Blocks.CAULDRON)) return InteractionResultHolder.pass(stack);

            BlockState newState = getNewState(pos, level, stack);
            if (newState != null) {
                level.setBlockAndUpdate(pos, newState);
                if (level.getBlockEntity(pos) instanceof LiquidCauldronBlockTile te) {
                    SoftFluidTank tank = te.getSoftFluidTank();
                    ItemStack returnStack = tank.interactWithItem(stack, level, pos, false);
                    if (returnStack != null) {
                        level.gameEvent(null, GameEvent.BLOCK_CHANGE, pos);
                        return InteractionResultHolder.success(returnStack);
                    } else {
                        level.setBlockAndUpdate(pos, originalState);
                    }
                }
            }
            return InteractionResultHolder.pass(stack);
        }
    }
}
