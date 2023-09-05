package net.mehvahdjukaar.amendments.forge;

import net.mehvahdjukaar.amendments.AmendmentsClient;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.LecternScreen;
import net.minecraftforge.client.event.ScreenEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class ClientEvents {

    @SubscribeEvent
    public static void screenEvent(ScreenEvent.Render event) {
        if (event.getScreen() instanceof LecternScreen) {
            GuiGraphics guiGraphics = event.getGuiGraphics();
            int mid = event.getScreen().width / 2;

            int w = 80;
            int bookW = 192;
            guiGraphics.blit(AmendmentsClient.LECTERN_GUI, mid - bookW / 2 - w, 2, 4, 0, w, 128);

        }
    }
}
