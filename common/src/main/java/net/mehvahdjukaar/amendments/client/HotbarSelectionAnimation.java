package net.mehvahdjukaar.amendments.client;

import com.mojang.blaze3d.platform.NativeImage;
import net.mehvahdjukaar.amendments.configs.ClientConfigs;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;

public class HotbarSelectionAnimation {

    private static boolean selectionOnBackground = false;
    private static float hotbarSlotAnimation = -1;

    public static boolean isSelectionOnBackground() {
        return selectionOnBackground;
    }

    public static void checkSelectionLayer(ResourceManager manager) {
        var path = ResourceLocation.withDefaultNamespace("textures/gui/sprites/hud/hotbar_selection.png");
        try (var stream = manager.open(path); NativeImage image = NativeImage.read(stream)) {
            int middle = image.getPixelRGBA(image.getWidth() / 2, image.getHeight() / 2);
            selectionOnBackground = (middle >>> 24) > 127;
        } catch (Exception e) {
            selectionOnBackground = false;
        }
    }

    public static float getHotbarSlotAnimation(int selected, double speed, float elapsedTicks) {
        boolean wrap = ClientConfigs.HOTBAR_SLOT_ANIMATION_WRAP.get();
        if (hotbarSlotAnimation < 0) {
            hotbarSlotAnimation = selected;
            return selected;
        }
        float distance = selected - hotbarSlotAnimation;
        if (wrap) distance -= 9 * Math.round(distance / 9);
        //framerate idndpendent
        hotbarSlotAnimation += distance * (float) (1 - Math.exp(-speed * elapsedTicks));
        if (wrap) hotbarSlotAnimation = ((hotbarSlotAnimation % 9) + 9) % 9;
        else hotbarSlotAnimation = Math.clamp(hotbarSlotAnimation, 0, 8);
        float remaining = selected - hotbarSlotAnimation;
        if (wrap) remaining -= 9 * Math.round(remaining / 9);
        if (Math.abs(remaining) < 0.01f) {
            hotbarSlotAnimation = selected;
        }
        return hotbarSlotAnimation;
    }
}
