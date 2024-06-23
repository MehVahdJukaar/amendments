package net.mehvahdjukaar.amendments.forge;

import net.mehvahdjukaar.amendments.AmendmentsClient;
import net.mehvahdjukaar.moonlight.api.platform.PlatHelper;
import net.minecraft.world.inventory.LecternMenu;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.client.event.ScreenEvent;
import net.minecraftforge.client.gui.overlay.VanillaGuiOverlay;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import org.lwjgl.glfw.GLFW;
import vectorwing.farmersdelight.data.BlockStates;

public class ClientEvents {

    @SubscribeEvent
    public static void tooltipEvent(ItemTooltipEvent event) {
        AmendmentsClient.onItemTooltip(event.getItemStack(),event.getFlags(), event.getToolTip());
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
