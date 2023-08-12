package net.mehvahdjukaar.betterjukebox;

import net.mehvahdjukaar.moonlight.api.platform.ClientHelper;
import net.mehvahdjukaar.moonlight.api.util.Utils;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.resources.model.Material;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.RecordItem;
import net.minecraft.world.level.block.entity.BlockEntityType;

import java.util.HashMap;
import java.util.Map;


public class BetterJukeboxesClient {

    public static void init() {
        ClientHelper.addBlockEntityRenderersRegistration(BetterJukeboxesClient::registerTileRenderers);
        new ClientResourceGenerator().register();
    }

    private static void registerTileRenderers(ClientHelper.BlockEntityRendererEvent event) {
        event.register(BlockEntityType.JUKEBOX, JukeboxTileRenderer::new);
    }

    private static final Map<Item, Material> RECORDS = new HashMap<>();
    private static final Material DEFAULT = new Material(TextureAtlas.LOCATION_BLOCKS,
          BetterJukeboxes.  res("block/music_disc_template"));

    public static Map<Item, Material> getAllRecords() {
        if (RECORDS.isEmpty()) {
            for (var i : BuiltInRegistries.ITEM) {
                if (i instanceof RecordItem) {
                    RECORDS.put(i, new Material(TextureAtlas.LOCATION_BLOCKS,
                            BetterJukeboxes.res("block/" + Utils.getID(i).toString()
                                    .replace("minecraft:", "")
                                    .replace(":", "/"))));
                }
            }
        }
        return RECORDS;
    }

    public static Material getRecordMaterial(Item item) {
        return getAllRecords().getOrDefault(item, DEFAULT);
    }

}
