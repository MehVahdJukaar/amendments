package net.mehvahdjukaar.amendments.platform;

import net.mehvahdjukaar.amendments.AmendmentsClient;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;

public class ClientEvents {

    @SubscribeEvent
    public static void tooltipEvent(ItemTooltipEvent event) {
        AmendmentsClient.onItemTooltip(event.getItemStack(), event.getContext(), event.getFlags(), event.getToolTip());
    }

}
