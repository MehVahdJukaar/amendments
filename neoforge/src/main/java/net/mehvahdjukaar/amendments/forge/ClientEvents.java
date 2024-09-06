package net.mehvahdjukaar.amendments.forge;

import net.mehvahdjukaar.amendments.AmendmentsClient;
import net.mehvahdjukaar.moonlight.api.platform.PlatHelper;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.InputEvent;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;
import org.lwjgl.glfw.GLFW;

public class ClientEvents {

    @SubscribeEvent
    public static void tooltipEvent(ItemTooltipEvent event) {
        AmendmentsClient.onItemTooltip(event.getItemStack(), event.getFlags(), event.getToolTip());
    }

    @SubscribeEvent
    public static void e(InputEvent.Key event) {
        if (!PlatHelper.isDev()) return;
        float f = 0.01f;

        if (event.getKey() == GLFW.GLFW_KEY_J) {
            AmendmentsClient.x += (event.getModifiers() == GLFW.GLFW_MOD_SHIFT) ? f : -f;
        }
        if (event.getKey() == GLFW.GLFW_KEY_K) {
            AmendmentsClient.y += (event.getModifiers() == GLFW.GLFW_MOD_SHIFT) ? f : -f;
        }
        if (event.getKey() == GLFW.GLFW_KEY_L) {
            AmendmentsClient.z += (event.getModifiers() == GLFW.GLFW_MOD_SHIFT) ? f : -f;
        }
    }


}
