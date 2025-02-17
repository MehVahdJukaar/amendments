package net.mehvahdjukaar.amendments.forge;

import net.mehvahdjukaar.amendments.AmendmentsClient;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class ClientEvents {

    @SubscribeEvent
    public static void tooltipEvent(ItemTooltipEvent event) {
        AmendmentsClient.onItemTooltip(event.getItemStack(), event.getFlags(), event.getToolTip());

    }


}
