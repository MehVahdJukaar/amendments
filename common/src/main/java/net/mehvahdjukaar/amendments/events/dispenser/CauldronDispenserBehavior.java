package net.mehvahdjukaar.amendments.events.dispenser;

import net.mehvahdjukaar.moonlight.api.util.DispenserHelper;
import net.minecraft.world.item.Item;

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
    protected InteractionResult customBehavior(BlockSource source, ItemStack stack) {
        //this.setSuccessful(false);
        ServerLevel level = source.getLevel();
        BlockState disp = source.getBlockState();
        BlockPos pos = source.getPos().relative(disp.getValue(DispenserBlock.FACING));
        BlockState originalState = level.getBlockState(pos);
        if (!originalState.is(Blocks.CAULDRON)) return InteractionResult.PASS;

        BlockState newState = getNewState(pos, level, stack);
        if (newState != null) {
            level.setBlockAndUpdate(pos, newState);
            if (level.getBlockEntity(pos) instanceof LiquidCauldronBlockTile te) {
                SoftFluidTank tank = te.getSoftFluidTank();
                ItemStack returnStack = tank.interactWithItem(stack, level, pos, false);
                if (returnStack != null) {
                    level.gameEvent(null, GameEvent.BLOCK_CHANGE, pos);
                    return InteractionResult.SUCCESS;
                } else {
                    level.setBlockAndUpdate(pos, originalState);
                }
            }
        }
        return InteractionResult.PASS;
    }*/
}