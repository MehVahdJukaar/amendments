package net.mehvahdjukaar.amendments.common;

import net.mehvahdjukaar.amendments.Amendments;
import net.mehvahdjukaar.amendments.reg.ModRegistry;
import net.mehvahdjukaar.amendments.reg.ModTags;
import net.mehvahdjukaar.moonlight.api.platform.PlatHelper;
import net.mehvahdjukaar.moonlight.api.resources.SimpleTagBuilder;
import net.mehvahdjukaar.moonlight.api.resources.pack.DynamicServerResourceProvider;
import net.mehvahdjukaar.moonlight.api.resources.pack.PackGenerationStrategy;
import net.mehvahdjukaar.moonlight.api.resources.pack.ResourceGenTask;
import net.mehvahdjukaar.moonlight.api.util.Utils;
import net.minecraft.core.registries.Registries;

import java.util.Collection;
import java.util.List;
import java.util.function.Consumer;

/**
 * Wall lantern blocks are registered dynamically (one per detected lantern type), so their tags
 * can't be shipped statically. This generates the {@code amendments:wall_lanterns} block tag with
 * every wall lantern block; the static mineable/pickaxe and create:safe_nbt tags reference it.
 */
public class WallLanternServerResources extends DynamicServerResourceProvider {

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
        });
    }
}
