package net.mehvahdjukaar.betterjukebox;

import net.mehvahdjukaar.moonlight.api.platform.ClientHelper;
import net.mehvahdjukaar.moonlight.api.util.Utils;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.resources.model.Material;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.RecordItem;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.JukeboxBlockEntity;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.HashMap;
import java.util.Map;


public class BetterJukeboxes {
    public static final String MOD_ID = "betterjukebox";
    public static final Logger LOGGER = LogManager.getLogger("Fast Paintings");
    private static boolean installedOnServer = false;

    public static ResourceLocation res(String name) {

        return new ResourceLocation(MOD_ID, name);
    }
    public static void init() {
        ClientHelper.addBlockEntityRenderersRegistration(BetterJukeboxes::registerTileRenderers);
        new ClientResourceGenerator().register();
    }

    private static void registerTileRenderers(ClientHelper.BlockEntityRendererEvent event) {
        event.register(BlockEntityType.JUKEBOX, JukeboxTileRenderer::new);
    }

    private static final Map<Item, Material> RECORDS = new HashMap<>();
    public static final Material DEFAULT = new Material(TextureAtlas.LOCATION_BLOCKS,
            res("block/music_disc_template"));

    public static Map<Item, Material> getRecords() {
        if (RECORDS.isEmpty()) {
            for (var i : BuiltInRegistries.ITEM) {
                if (i instanceof RecordItem) {
                    RECORDS.put(i, new Material(TextureAtlas.LOCATION_BLOCKS,
                            res("block/" + Utils.getID(i).toString()
                                    .replace("minecraft:", "")
                                    .replace(":", "/"))));
                }
            }
        }
        return RECORDS;
    }

    public static void setIsInstalledOnServer() {
        installedOnServer = true;
    }

    //clear on world load

    //hack for client only mod
    //shitty networking hijack to avoid being server sided
    private static final ThreadLocal<Boolean> LOCK = ThreadLocal.withInitial(()->false);
    public static void setPlayingJukebox(Level level, BlockPos pos, boolean on, int id) {
        if (!installedOnServer && !LOCK.get() && level.getBlockEntity(pos) instanceof JukeboxBlockEntity be) {
            if (on){
                LOCK.set(true);
                be.setFirstItem(Item.byId(id).getDefaultInstance());
                LOCK.set(false);
            }
            ((IBetterJukebox)be).setPlaying(on);
        }
    }


}
