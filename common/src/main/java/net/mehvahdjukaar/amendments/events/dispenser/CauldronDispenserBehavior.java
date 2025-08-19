package net.mehvahdjukaar.amendments.events.dispenser;

import net.mehvahdjukaar.amendments.common.tile.LiquidCauldronBlockTile;
import net.mehvahdjukaar.moonlight.api.fluids.SoftFluidTank;
import net.mehvahdjukaar.moonlight.api.util.DispenserHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.dispenser.BlockSource;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.DispenserBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;

public abstract class CauldronDispenserBehavior extends DispenserHelper.AdditionalDispenserBehavior {
    protected CauldronDispenserBehavior(Item item) {
        super(item);
    }

    //TODO:move here from CauldronInteraction

    /*
    public CauldronDispenserBehavior(Item item) {
        super(item);
    }

    @Override
    protected InteractionResultHolder<ItemStack> customBehavior(BlockSource source, ItemStack stack) {
        //this.setSuccessful(false);
        ServerLevel level = source.getLevel();
        BlockState disp = source.getBlockState();
        BlockPos pos = source.getPos().relative(disp.getValue(DispenserBlock.FACING));
        BlockState originalState = level.getBlockState(pos);
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
    }*/
}