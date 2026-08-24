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

    // Lanterns whose texture doesn't sample into a decent mount (colored glass, glowing bulbs, vines)
    // so the recolor comes out garish. They all hang off a plain metal frame anyway, so give them a
    // handmade mount.
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
            // Last resort: emit the vanilla (iron) wall mount unchanged. A plain mount is far better
            // than a missing/broken texture.
            return template.makeCopy();
        }
    }

    /**
     * Grabs the lantern's metal palette with progressively looser sampling:
     * <ol>
     *   <li>the metal base band at the bottom of the lantern (see {@link #createMetalMask}). This
     *       matches the vanilla texture layout, so vanilla-metal lanterns reproduce the builtin mount
     *       exactly.</li>
     *   <li>the whole opaque lantern, for modded textures (e.g. Caverns &amp; Chasms copper lanterns)
     *       that pack their art higher up and leave the base band transparent - the narrow sample
     *       would otherwise come back empty.</li>
     * </ol>
     * Returns {@code null} if neither yields any colors, so the caller can fall back to the default
     * mount texture.
     */
    @Nullable
    private static Palette extractMetalPalette(TextureImage source) {
        try (TextureImage mask = createMetalMask(source)) {
            Palette base = trySample(source, mask);
            if (base != null) return base;
            // The vanilla-shaped base band was empty; sample the entire lantern instead.
            return trySample(source, null);
        }
    }

    @Nullable
    private static Palette trySample(TextureImage source, @Nullable TextureImage mask) {
        try {
            // Palette.fromImage throws when the sampled region has no opaque pixels.
            Palette p = Palette.fromImage(source, mask);
            return p.isEmpty() ? null : p;
        } catch (Exception e) {
            return null;
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

    /**
     * Where the wall mount texture for this lantern lives. Always a per lantern path so a resource
     * pack can drop its own texture there and win over whatever we make, be it a recolor or a copy
     * of one of our handmade mounts.
     */
    public static ResourceLocation getSupportTextureLocation(LanternRegistry.LanternType type) {
        ResourceLocation reg = type.getId();
        if (type.isVanilla() && reg.getPath().equals("lantern")) {
            return IRON_MOUNT;
        }
        // Skinned lanterns all reuse the vanilla metal frame so they just share the base mount.
        // Copying it to a hundred odd paths would only waste atlas space.
        if (reg.getNamespace().equals("skinnedlanterns")) {
            return IRON_MOUNT;
        }
        String namespace = (reg.getNamespace().equals("minecraft") || reg.getNamespace().equals(Amendments.MOD_ID)) ? "" : reg.getNamespace() + "/";
        return Amendments.res("block/wall_lanterns/" + namespace + reg.getPath());
    }

    /**
     * The handmade mount this lantern should copy instead of getting a generated recolor,
     * or null if it has none and must be generated.
     */
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

    /**
     * Caverns &amp; Chasms copper lanterns use the standard copper weathering colors, so their wall
     * mounts are just the shared, handmade copper mount textures rather than runtime palette
     * generation (which their texture layout - a transparent metal base - can't feed). Waxed variants
     * share their unwaxed colour; cupric reuses the plain iron mount. Returns {@code null} for any
     * other C&amp;C lantern so it falls back to normal generation.
     */
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
