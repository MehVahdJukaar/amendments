package net.mehvahdjukaar.amendments.events.dispenser;

import net.mehvahdjukaar.moonlight.api.util.DispenserHelper;
import net.minecraft.core.dispenser.BlockSource;
import net.mehvahdjukaar.moonlight.api.util.ItemStackResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

//TODO
public class VanillaBucketDispenserBehavior  extends DispenserHelper.AdditionalDispenserBehavior {
    protected VanillaBucketDispenserBehavior(Item item) {
        super(item);
    }

    @Override
    protected ItemStackResult customBehavior(BlockSource blockSource, ItemStack itemStack) {
        return ItemStackResult.pass(itemStack);
    }
//TODO: add water behavior here and lava and powder snow

}
