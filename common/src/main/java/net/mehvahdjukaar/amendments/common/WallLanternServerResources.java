package net.mehvahdjukaar.amendments.common;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.mehvahdjukaar.amendments.Amendments;
import net.mehvahdjukaar.amendments.common.block.WallLanternBlock;
import net.mehvahdjukaar.amendments.common.block.WeatheringWallLanternBlock;
import net.mehvahdjukaar.amendments.reg.ModRegistry;
import net.mehvahdjukaar.amendments.reg.ModTags;
import net.mehvahdjukaar.moonlight.api.resources.ResType;
import net.mehvahdjukaar.moonlight.api.resources.SimpleTagBuilder;
import net.mehvahdjukaar.moonlight.api.resources.pack.DynamicServerResourceProvider;
import net.mehvahdjukaar.moonlight.api.resources.pack.PackGenerationStrategy;
import net.mehvahdjukaar.moonlight.api.resources.pack.ResourceGenTask;
import net.mehvahdjukaar.moonlight.api.util.Utils;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.List;
import java.util.function.Consumer;

/**
 * Wall lantern blocks are registered dynamically (one per detected lantern type), so their tags and copper
 * data maps can't be shipped statically. This generates:
 * <ul>
 *   <li>the {@code amendments:wall_lanterns} block tag with every wall lantern block;</li>
 *   <li>the {@code oxidizables}/{@code waxables} block data maps for copper wall lanterns, so they oxidize,
 *       scrape and wax through the loader's own copper system - exactly like their standalone lantern. The
 *       file is the standard NeoForge data map (read natively there) plus Moonlight's Fabric marker (read by
 *       {@code DataMapBridge} on Fabric), so one file drives both loaders.</li>
 * </ul>
 */
public class WallLanternServerResources extends DynamicServerResourceProvider {

    /**
     * Weather stage prefixes in oxidation order (the universal copper naming convention).
     */
    private static final String[] WEATHER_PREFIXES = {"", "exposed_", "weathered_", "oxidized_"};
    /**
     * Tells Moonlight's Fabric DataMapBridge to parse this NeoForge-format file; ignored by NeoForge.
     */
    private static final String FABRIC_MARKER = "moonlight_parse_on_fabric";

    public WallLanternServerResources() {
        super(Amendments.res("generated_server_pack"), PackGenerationStrategy.REGEN_ON_EVERY_RELOAD);
    }

    @Override
    protected Collection<String> gatherSupportedNamespaces() {
        return List.of("minecraft");
    }

    @Override
    public void regenerateDynamicAssets(Consumer<ResourceGenTask> executor) {
        executor.accept((manager, sink) -> {
            SimpleTagBuilder builder = SimpleTagBuilder.of(ModTags.WALL_LANTERNS.location());
            for (var wallBlock : ModRegistry.WALL_LANTERNS.values()) {
                builder.add(Utils.getID(wallBlock));
            }
            sink.addTag(builder, Registries.BLOCK);

            JsonObject oxidizables = new JsonObject();
            JsonObject waxables = new JsonObject();
            for (var entry : ModRegistry.WALL_LANTERNS.entrySet()) {
                if (!(entry.getValue() instanceof WeatheringWallLanternBlock wall)) continue;
                ResourceLocation lanternId = entry.getKey().getId();

                WallLanternBlock next = wallLanternFor(shiftOxidation(lanternId, 1));
                if (next != null) putPair(oxidizables, wall, next, "next_oxidation_stage");

                WallLanternBlock waxed = wallLanternFor(withWaxed(lanternId, true));
                if (waxed != null) putPair(waxables, wall, waxed, "waxed");
            }
            if (!oxidizables.keySet().isEmpty()) {
                sink.addJson(Amendments.res("data_maps/block/oxidizables"), wrap(oxidizables), ResType.JSON);
            }
            if (!waxables.keySet().isEmpty()) {
                sink.addJson(Amendments.res("data_maps/block/waxables"), wrap(waxables), ResType.JSON);
            }
        });
    }

    private static void putPair(JsonObject values, WallLanternBlock from, WallLanternBlock to, String field) {
        JsonObject value = new JsonObject();
        value.addProperty(field, Utils.getID(to).toString());
        values.add(Utils.getID(from).toString(), value);
    }

    private static JsonElement wrap(JsonObject values) {
        JsonObject root = new JsonObject();
        root.addProperty(FABRIC_MARKER, true);
        root.add("values", values);
        return root;
    }

    @Nullable
    private static WallLanternBlock wallLanternFor(@Nullable ResourceLocation lanternId) {
        return lanternId == null ? null : ModRegistry.WALL_LANTERNS_BY_LANTERN.get(lanternId);
    }

    /**
     * Returns the lantern id one oxidation step away ({@code +1} more weathered, {@code -1} scraped), or null past the ends.
     */
    @Nullable
    private static ResourceLocation shiftOxidation(ResourceLocation lanternId, int step) {
        String path = lanternId.getPath();
        boolean waxed = path.startsWith("waxed_");
        String rest = waxed ? path.substring("waxed_".length()) : path;
        int index = weatherIndex(rest);
        int next = index + step;
        if (next < 0 || next >= WEATHER_PREFIXES.length) return null;
        String base = rest.substring(WEATHER_PREFIXES[index].length());
        return ResourceLocation.fromNamespaceAndPath(lanternId.getNamespace(),
                (waxed ? "waxed_" : "") + WEATHER_PREFIXES[next] + base);
    }

    /**
     * Returns the waxed or unwaxed counterpart of the given copper lantern id, keeping its weather stage.
     */
    private static ResourceLocation withWaxed(ResourceLocation lanternId, boolean waxed) {
        String path = lanternId.getPath();
        boolean alreadyWaxed = path.startsWith("waxed_");
        String rest = alreadyWaxed ? path.substring("waxed_".length()) : path;
        return ResourceLocation.fromNamespaceAndPath(lanternId.getNamespace(), (waxed ? "waxed_" : "") + rest);
    }

    private static int weatherIndex(String unwaxedPath) {
        for (int i = WEATHER_PREFIXES.length - 1; i >= 1; i--) {
            if (unwaxedPath.startsWith(WEATHER_PREFIXES[i])) return i;
        }
        return 0;
    }
}
