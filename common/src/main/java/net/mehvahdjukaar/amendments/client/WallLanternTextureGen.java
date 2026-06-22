package net.mehvahdjukaar.amendments.client;

import net.mehvahdjukaar.amendments.Amendments;
import net.mehvahdjukaar.amendments.common.LanternRegistry;
import net.mehvahdjukaar.moonlight.api.resources.RPUtils;
import net.mehvahdjukaar.moonlight.api.resources.textures.Palette;
import net.mehvahdjukaar.moonlight.api.resources.textures.Respriter;
import net.mehvahdjukaar.moonlight.api.resources.textures.TextureImage;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.FastColor;

public class WallLanternTextureGen {

    private WallLanternTextureGen() {
    }

    public static boolean canGenerateFrom(ResourceManager manager, ResourceLocation lanternTexture) {
        try (TextureImage source = TextureImage.open(manager, lanternTexture);
             TextureImage vanilla = TextureImage.open(manager, ResourceLocation.withDefaultNamespace("block/lantern"))) {
            return hasMatchingWhitespace(source, vanilla);
        } catch (Exception e) {
            return false;
        }
    }

    public static TextureImage generate(ResourceManager manager, ResourceLocation lanternTexture) throws Exception {
        try (TextureImage sourceLantern = TextureImage.open(manager, lanternTexture);
             TextureImage vanillaLantern = TextureImage.open(manager, ResourceLocation.withDefaultNamespace("block/lantern"));
             TextureImage template = TextureImage.open(manager, Amendments.res("block/wall_lanterns/wall_lantern"));
             TextureImage lanternMetalMask = openMetalMask(manager, Amendments.res("block/wall_lanterns/lantern_metal_mask"),
                     vanillaLantern, true);
             TextureImage supportMetalMask = openMetalMask(manager, Amendments.res("block/wall_lanterns/wall_lantern_metal_mask"),
                     template, false)) {

            restrictToUpperHalf(lanternMetalMask);
            Palette metalPalette = Palette.fromImage(sourceLantern, lanternMetalMask);
            Respriter respriter = Respriter.masked(template, supportMetalMask);
            return respriter.recolor(metalPalette);
        }
    }

    private static TextureImage openMetalMask(ResourceManager manager, ResourceLocation path,
                                              TextureImage fallbackFrom, boolean fromLantern) throws Exception {
        if (manager.getResource(net.mehvahdjukaar.moonlight.api.resources.ResType.TEXTURES.getPath(path)).isPresent()) {
            return TextureImage.open(manager, path);
        }
        return fromLantern ? createLanternMetalMask(fallbackFrom) : createSupportMetalMask(fallbackFrom);
    }

    /** Clears the lower half of a mask so palette sampling uses only the lantern cage. */
    private static void restrictToUpperHalf(TextureImage mask) {
        int halfHeight = (int) (mask.imageHeight() * 0.3);
        for (int x = 0; x < mask.imageWidth(); x++) {
            for (int y = halfHeight; y < mask.imageHeight(); y++) {
                mask.setPixel(x, y, 0);
            }
        }
    }

    /**
     * Marks metal pixels on a vanilla-style lantern texture (opaque, non-glass).
     */
    private static TextureImage createLanternMetalMask(TextureImage lantern) {
        TextureImage mask = TextureImage.createNew(lantern.imageWidth(), lantern.imageHeight());
        for (int x = 0; x < lantern.imageWidth(); x++) {
            for (int y = 0; y < lantern.imageHeight(); y++) {
                int pixel = lantern.getPixel(x, y);
                if (FastColor.ABGR32.alpha(pixel) == 0) continue;
                if (isGlassPixel(pixel)) continue;
                mask.setPixel(x, y, 0xFFFFFFFF);
            }
        }
        return mask;
    }

    /**
     * Marks recolorable metal pixels on the wall lantern support template.
     */
    private static TextureImage createSupportMetalMask(TextureImage template) {
        TextureImage mask = TextureImage.createNew(template.imageWidth(), template.imageHeight());
        for (int x = 0; x < template.imageWidth(); x++) {
            for (int y = 0; y < template.imageHeight(); y++) {
                int pixel = template.getPixel(x, y);
                if (FastColor.ABGR32.alpha(pixel) == 0) {
                    mask.setPixel(x, y, 0xFFFFFFFF);
                }
            }
        }
        return mask;
    }

    public static ResourceLocation getSupportTextureLocation(LanternRegistry.LanternType type) {
        ResourceLocation reg = type.getId();
        if (type.isVanilla() && reg.getPath().equals("lantern")) {
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
