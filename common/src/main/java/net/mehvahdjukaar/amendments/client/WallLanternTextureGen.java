package net.mehvahdjukaar.amendments.client;

import net.mehvahdjukaar.amendments.Amendments;
import net.mehvahdjukaar.amendments.common.LanternRegistry;
import net.mehvahdjukaar.moonlight.api.resources.RPUtils;
import net.mehvahdjukaar.moonlight.api.resources.textures.Palette;
import net.mehvahdjukaar.moonlight.api.resources.textures.Respriter;
import net.mehvahdjukaar.moonlight.api.resources.textures.TextureImage;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;

public class WallLanternTextureGen {

    private WallLanternTextureGen() {
    }

    public static TextureImage generate(ResourceManager manager, ResourceLocation lanternTexture) throws Exception {
        try (TextureImage fullLantern = TextureImage.open(manager, lanternTexture);
             TextureImage sourceLantern = firstFrame(fullLantern);
             TextureImage template = TextureImage.open(manager, Amendments.res("block/wall_lanterns/wall_lantern"));
             TextureImage paletteMask = createMetalMask(sourceLantern)) {

            Palette palette = Palette.fromImage(sourceLantern, paletteMask);
            return Respriter.of(template).recolor(palette);
        }
    }

    /**
     * Lantern textures may be animated (frames stacked vertically). Only the first frame
     * represents the lit lantern, so we recolor from that single frame rather than the whole sheet.
     */
    private static TextureImage firstFrame(TextureImage image) {
        int fw = image.frameWidth();
        int fh = image.frameHeight();
        TextureImage frame = TextureImage.createNew(fw, fh);
        for (int x = 0; x < fw; x++) {
            for (int y = 0; y < fh; y++) {
                frame.setPixel(x, y, image.getFramePixel(0, x, y));
            }
        }
        return frame;
    }

    /**
     * Builds a palette mask that samples only the metal base at the bottom of the lantern.
     * Moonlight reads colors from the mask's transparent pixels, so everything above the base is
     * made opaque (ignored). The base carries the full metal palette - the wall mount texture is a
     * recolor of exactly those shades, so for lanterns whose metal matches vanilla the generated
     * mount comes out identical to the builtin one. It also sits farthest from the glowing core in
     * the upper-middle of the cage, which modded lanterns may draw larger than vanilla.
     */
    private static TextureImage createMetalMask(TextureImage image) {
        TextureImage mask = TextureImage.createNew(image.imageWidth(), image.imageHeight(), image.getMcMeta());
        int baseStart = Math.round(image.frameHeight() * 0.75f);
        image.forEachPixel(pixel -> {
            if (pixel.frameY() < baseStart) {
                mask.setPixel(pixel.x(), pixel.y(), 0xFFFFFFFF);
            }
        });
        return mask;
    }

    public static ResourceLocation getSupportTextureLocation(LanternRegistry.LanternType type) {
        ResourceLocation reg = type.getId();
        if (type.isVanilla() && reg.getPath().equals("lantern")) {
            return Amendments.res("block/wall_lanterns/wall_lantern");
        }
        // Skinned lanterns reuse the vanilla metal frame, so they share the base wall mount texture
        // instead of generating a recolored one (which would just duplicate the vanilla shades).
        if (reg.getNamespace().equals("skinnedlanterns")) {
            return Amendments.res("block/wall_lanterns/wall_lantern");
        }
        String namespace = (reg.getNamespace().equals("minecraft") || reg.getNamespace().equals(Amendments.MOD_ID)) ? "" : reg.getNamespace() + "/";
        return Amendments.res("block/wall_lanterns/" + namespace + reg.getPath());
    }

    public static ResourceLocation getLanternTextureLocation(ResourceManager manager, LanternRegistry.LanternType type) {
        try {
            return RPUtils.findFirstBlockTextureLocation(manager, type.lantern);
        } catch (Exception e) {
            return ResourceLocation.withDefaultNamespace("block/lantern");
        }
    }
}
