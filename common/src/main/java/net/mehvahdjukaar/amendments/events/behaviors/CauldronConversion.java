package net.mehvahdjukaar.amendments.events.behaviors;

import net.mehvahdjukaar.amendments.common.block.LiquidCauldronBlock;
import net.mehvahdjukaar.amendments.common.tile.LiquidCauldronBlockTile;
import net.mehvahdjukaar.amendments.configs.CommonConfigs;
import net.mehvahdjukaar.amendments.reg.ModRegistry;
import net.mehvahdjukaar.moonlight.api.fluids.BuiltInSoftFluids;
import net.mehvahdjukaar.moonlight.api.fluids.SoftFluidRegistry;
import net.mehvahdjukaar.moonlight.api.fluids.SoftFluidStack;
import net.mehvahdjukaar.moonlight.api.fluids.SoftFluidTank;
import net.mehvahdjukaar.moonlight.api.util.DispenserHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.BlockSource;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.DispenserBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;

public class CauldronConversion implements BlockUse {

    //block use as it has way too many items that could trigger

    @Override
    public boolean isEnabled() {
        return CommonConfigs.ENHANCED_CAULDRON.get();
    }

    @Override
    public boolean appliesToBlock(Block block) {
        return Blocks.CAULDRON == block;
    }

    //maybe move to mixin
    @Override
    public InteractionResult tryPerformingAction(BlockState state, BlockPos pos, Level level, Player player, InteractionHand hand, ItemStack stack, BlockHitResult hit) {
        if (!state.is(Blocks.CAULDRON)) return InteractionResult.PASS;
        BlockState newState = getNewState(pos, level, stack);
        if (newState != null) {
            level.setBlockAndUpdate(pos, newState);
            if (level.getBlockEntity(pos) instanceof LiquidCauldronBlockTile te) {
                if (te.handleInteraction(player, hand)) {
                    return InteractionResult.sidedSuccess(level.isClientSide);
                } else {
                    level.setBlockAndUpdate(pos, state);
                }
            }
        }
        return InteractionResult.PASS;
    }

    @Nullable
    public static BlockState getNewState(BlockPos pos, Level level, ItemStack stack) {
        Item item = stack.getItem();
        var fluid = SoftFluidRegistry.fromItem(item);
        if (fluid != null && !stack.is(Items.LAVA_BUCKET) && !stack.is(Items.POWDER_SNOW_BUCKET) &&
                !stack.is(Items.WATER_BUCKET) && !((stack.is(Items.POTION) && fluid.is(BuiltInSoftFluids.WATER.getID())))) {
            BlockState newState;
            if (item == ModRegistry.DYE_BOTTLE_ITEM.get()) {
                newState = ModRegistry.DYE_CAULDRON.get().defaultBlockState();
            } else {
                newState = ModRegistry.LIQUID_CAULDRON.get().defaultBlockState()
                        .setValue(LiquidCauldronBlock.BOILING,
                                LiquidCauldronBlock.shouldBoil(level.getBlockState(pos.below()), new SoftFluidStack(fluid)));
            }
            return newState;
        }
        return null;
    }

    public static class DispenserBehavior extends DispenserHelper.AdditionalDispenserBehavior {

        public DispenserBehavior(Item item) {
            super(item);
        }

        @Override
        protected InteractionResultHolder<ItemStack> customBehavior(BlockSource source, ItemStack stack) {
            //this.setSuccessful(false);
            ServerLevel level = source.getLevel();
            BlockState originalState = source.getBlockState();
            BlockPos pos = source.getPos().relative(originalState.getValue(DispenserBlock.FACING));
            if (!originalState.is(Blocks.CAULDRON)) return InteractionResultHolder.pass(stack);

            BlockState newState = getNewState(pos, level, stack);
            if (newState != null) {
                level.setBlockAndUpdate(pos, newState);
                if (level.getBlockEntity(pos) instanceof LiquidCauldronBlockTile te) {
                    SoftFluidTank tank = te.getSoftFluidTank();
                    ItemStack returnStack = tank.interactWithItem(stack, level, pos, false);
                    if (returnStack != null) {
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
