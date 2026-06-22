package net.mehvahdjukaar.amendments.common;

import net.mehvahdjukaar.amendments.Amendments;
import net.minecraft.resources.ResourceLocation;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Lanterns with hand-made wall bracket textures shipped in the mod jar.
 */
public final class WallLanternCompat {

    private static final Map<ResourceLocation, ResourceLocation> SUPPORT_TEXTURES = new LinkedHashMap<>();

    static {
        twig("paper_lantern");
        twig("allium_paper_lantern");
        twig("blue_orchid_paper_lantern");
        twig("crimson_roots_paper_lantern");
        twig("dandelion_paper_lantern");

        add("forbidden_arcanus", "deorum_lantern", "wall_lantern_deorum");
        add("forbidden_arcanus", "deorum_soul_lantern", "wall_lantern_deorum");

        add("byg", "glowstone_lantern", "wall_lantern_glowstone");
        add("byg", "cryptic_lantern", "wall_lantern_cryptic");

        add("architects_palette", "nether_brass", "wall_lantern_nether_brass");
        add("oxidized", "copper_lantern", "wall_lantern_oxidized");
        add("pokecube_legends", "infected_lantern", "wall_lantern_infected");
    }

    private WallLanternCompat() {
    }

    private static void twig(String path) {
        add("twigs", path, "wall_lantern_twig");
    }

    private static void add(String namespace, String path, String textureName) {
        SUPPORT_TEXTURES.put(
                ResourceLocation.fromNamespaceAndPath(namespace, path),
                Amendments.res("block/wall_lanterns/" + textureName));
    }

    public static boolean hasBuiltinSupport(LanternRegistry.LanternType type) {
        return SUPPORT_TEXTURES.containsKey(type.getId());
    }

    public static Optional<ResourceLocation> getSupportTexture(LanternRegistry.LanternType type) {
        return Optional.ofNullable(SUPPORT_TEXTURES.get(type.getId()));
    }

    public static String getWallBlockPath(LanternRegistry.LanternType type) {
        return type.getVariantId("wall");
    }
}
