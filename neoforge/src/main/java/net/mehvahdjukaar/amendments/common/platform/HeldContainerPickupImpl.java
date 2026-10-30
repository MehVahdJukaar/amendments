package net.mehvahdjukaar.amendments.common.platform;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.items.ItemHandlerHelper;

public class HeldContainerPickupImpl {

    public static int insertIntoItemStorage(Player player, InteractionHand hand, ItemStack toInsert) {
        var handler = player.getItemInHand(hand).getCapability(Capabilities.ItemHandler.ITEM);
        if (handler == null) return 0;
        ItemStack remainder = ItemHandlerHelper.insertItem(handler, toInsert.copy(), false);
        return toInsert.getCount() - remainder.getCount();
    }
}
