package net.mehvahdjukaar.amendments.forge;

import net.mehvahdjukaar.amendments.AmendmentsClient;
import net.mehvahdjukaar.moonlight.api.platform.PlatHelper;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.client.event.ScreenEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import org.lwjgl.glfw.GLFW;

public class ClientEvents {

    @SubscribeEvent
    public static void screenEvent(ScreenEvent.Render event) {
    }

    @SubscribeEvent
    public static void e(InputEvent.Key event) {
        if (!PlatHelper.isDev()) return;
        float f = 0.002f;
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
