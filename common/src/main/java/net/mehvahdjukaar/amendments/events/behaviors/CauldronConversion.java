package net.mehvahdjukaar.amendments.events.behaviors;

import net.mehvahdjukaar.amendments.common.block.LiquidCauldronBlock;
import net.mehvahdjukaar.amendments.common.tile.LiquidCauldronBlockTile;
import net.mehvahdjukaar.amendments.configs.CommonConfigs;
import net.mehvahdjukaar.amendments.reg.ModRegistry;
import net.mehvahdjukaar.moonlight.api.fluids.BuiltInSoftFluids;
import net.mehvahdjukaar.moonlight.api.fluids.SoftFluid;
import net.mehvahdjukaar.moonlight.api.fluids.SoftFluidRegistry;
import net.mehvahdjukaar.moonlight.api.fluids.SoftFluidStack;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

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

    @Override
    public InteractionResult tryPerformingAction(BlockState state, BlockPos pos, Level level, Player player, InteractionHand hand, ItemStack stack, BlockHitResult hit) {
        Item item = stack.getItem();
        if (state.is(Blocks.CAULDRON)) {
            //just checks if equivalent fluid exist. rest should not fail. I hope
            var fluid = SoftFluidRegistry.fromItem(item);
            if (fluid != null  && !fluid.is(BuiltInSoftFluids.LAVA.getID()) && !fluid.is(BuiltInSoftFluids.WATER.getID())
                    && !fluid.is(BuiltInSoftFluids.POWDERED_SNOW.getID())) {
                BlockState newState;
                if (item == ModRegistry.DYE_BOTTLE_ITEM.get()) {
                    newState = ModRegistry.DYE_CAULDRON.get().defaultBlockState();
                } else {
                    newState = ModRegistry.LIQUID_CAULDRON.get().defaultBlockState()
                            .setValue(LiquidCauldronBlock.BOILING,
                                    LiquidCauldronBlock.shouldBoil(level.getBlockState(pos.below()), new SoftFluidStack(fluid)));
                }
                level.setBlockAndUpdate(pos, newState);
                if (level.getBlockEntity(pos) instanceof LiquidCauldronBlockTile te) {
                    te.handleInteraction(player, hand);
                    return InteractionResult.sidedSuccess(level.isClientSide);
                }
            }
        }
        return InteractionResult.PASS;
    }
}
