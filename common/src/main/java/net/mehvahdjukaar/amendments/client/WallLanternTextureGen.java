package net.mehvahdjukaar.amendments.client;

import net.mehvahdjukaar.amendments.Amendments;
import net.mehvahdjukaar.amendments.common.LanternRegistry;
import net.mehvahdjukaar.moonlight.api.resources.RPUtils;
import net.mehvahdjukaar.moonlight.api.resources.textures.Palette;
import net.mehvahdjukaar.moonlight.api.resources.textures.Respriter;
import net.mehvahdjukaar.moonlight.api.resources.textures.TextureImage;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

public class WallLanternTextureGen {

    private static final ResourceLocation IRON_MOUNT = Amendments.res("block/wall_lanterns/wall_lantern");
    private static final ResourceLocation GOLD_MOUNT = Amendments.res("block/wall_lanterns/wall_lantern_gold");

    private static final Map<ResourceLocation, ResourceLocation> HANDMADE_MOUNTS = Map.of(
            ResourceLocation.fromNamespaceAndPath("suppsquared", "crimson_lantern"), GOLD_MOUNT,
            ResourceLocation.fromNamespaceAndPath("enderscape", "bulb_lantern"), IRON_MOUNT,
            ResourceLocation.fromNamespaceAndPath("bountifulfares", "feldspar_lantern"), IRON_MOUNT,
            ResourceLocation.fromNamespaceAndPath("goodending", "firefly_lantern"), IRON_MOUNT,
            ResourceLocation.fromNamespaceAndPath("dungeonsdelight", "living_lantern"), IRON_MOUNT,
            ResourceLocation.fromNamespaceAndPath("darkerdepths", "glowshroom_lantern"), IRON_MOUNT
    );

    private WallLanternTextureGen() {
    }

    public static TextureImage generate(ResourceManager manager, ResourceLocation lanternTexture) throws Exception {
        try (TextureImage template = TextureImage.open(manager, IRON_MOUNT)) {
            try (TextureImage fullLantern = TextureImage.open(manager, lanternTexture);
                 TextureImage sourceLantern = firstFrame(fullLantern)) {

                Palette palette = extractMetalPalette(sourceLantern);
                if (palette != null) {
                    return Respriter.of(template).recolor(palette);
                }
                Amendments.LOGGER.warn("Could not extract a usable palette from lantern texture {}. " +
                        "Using the default wall mount texture", lanternTexture);
            } catch (Exception e) {
                Amendments.LOGGER.warn("Failed to generate wall mount texture for lantern {}. " +
                        "Using the default wall mount texture", lanternTexture, e);
            }
            return template.makeCopy();
        }
    }

    @Nullable
    private static Palette extractMetalPalette(TextureImage source) {
        try (TextureImage mask = createMetalMask(source)) {
            Palette base = trySample(source, mask);
            if (base != null) return base;
            return trySample(source, null);
        }
    }

    @Nullable
    private static Palette trySample(TextureImage source, @Nullable TextureImage mask) {
        try {
            Palette p = Palette.fromImage(source, mask);
            return p.isEmpty() ? null : p;
        } catch (Exception e) {
            return null;
        }
    }

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
            return IRON_MOUNT;
        }
        //all of these reuse the vanilla frame, no point in copying the mount to a hundred paths
        if (reg.getNamespace().equals("skinnedlanterns")) {
            return IRON_MOUNT;
        }
        String namespace = (reg.getNamespace().equals("minecraft") || reg.getNamespace().equals(Amendments.MOD_ID)) ? "" : reg.getNamespace() + "/";
        return Amendments.res("block/wall_lanterns/" + namespace + reg.getPath());
    }

    @Nullable
    public static ResourceLocation getHandmadeMount(LanternRegistry.LanternType type) {
        ResourceLocation reg = type.getId();
        ResourceLocation manual = HANDMADE_MOUNTS.get(reg);
        if (manual != null) return manual;
        if (reg.getNamespace().equals("caverns_and_chasms")) {
            return copperMountTexture(reg.getPath());
        }
        return null;
    }

    @Nullable
    private static ResourceLocation copperMountTexture(String path) {
        String p = path.startsWith("waxed_") ? path.substring("waxed_".length()) : path;
        return switch (p) {
            case "copper_lantern" -> Amendments.res("block/wall_lanterns/wall_lantern_copper");
            case "exposed_copper_lantern" -> Amendments.res("block/wall_lanterns/wall_lantern_copper_exposed");
            case "weathered_copper_lantern" -> Amendments.res("block/wall_lanterns/wall_lantern_copper_weathered");
            case "oxidized_copper_lantern" -> Amendments.res("block/wall_lanterns/wall_lantern_copper_oxidized");
            case "cupric_lantern" -> IRON_MOUNT;
            default -> null;
        };
    }

    public static ResourceLocation getLanternTextureLocation(ResourceManager manager, LanternRegistry.LanternType type) {
        try {
            return RPUtils.findFirstBlockTextureLocation(manager, type.lantern);
        } catch (Exception e) {
            return ResourceLocation.withDefaultNamespace("block/lantern");
        }
    }
}
