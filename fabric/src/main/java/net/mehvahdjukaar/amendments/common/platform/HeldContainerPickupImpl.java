package net.mehvahdjukaar.amendments.common.platform;

import net.fabricmc.fabric.api.transfer.v1.context.ContainerItemContext;
import net.fabricmc.fabric.api.transfer.v1.item.ItemStorage;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public class HeldContainerPickupImpl {

    public static int insertIntoItemStorage(Player player, InteractionHand hand, ItemStack toInsert) {
        var storage = ItemStorage.ITEM.find(player.getItemInHand(hand), ContainerItemContext.ofPlayerHand(player, hand));
        if (storage == null) return 0;
        try (Transaction transaction = Transaction.openOuter()) {
            long inserted = storage.insert(ItemVariant.of(toInsert), toInsert.getCount(), transaction);
            transaction.commit();
            return (int) inserted;
        }
    }

}
