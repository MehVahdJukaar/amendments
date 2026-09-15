package net.mehvahdjukaar.amendments.client;

import net.mehvahdjukaar.amendments.Amendments;
import net.mehvahdjukaar.amendments.common.LanternRegistry;
import net.mehvahdjukaar.amendments.common.block.WallLanternBlock;
import net.mehvahdjukaar.amendments.reg.ModRegistry;
import net.mehvahdjukaar.moonlight.api.client.util.RenderUtil;
import net.mehvahdjukaar.moonlight.api.platform.ClientHelper;
import net.mehvahdjukaar.moonlight.api.util.Utils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.world.level.block.state.BlockState;

import java.util.IdentityHashMap;
import java.util.Map;

public class WallLanternModelsManager {

    private static final Map<LanternRegistry.LanternType, Identifier> CUSTOM_LANTERN_MODELS = new IdentityHashMap<>();

    public static void registerStandaloneModels(ClientHelper.StandaloneModelEvent event) {
        refreshModels(Minecraft.getInstance().getResourceManager());
        CUSTOM_LANTERN_MODELS.values().forEach(event::register);
    }

    private static void refreshModels(ResourceManager manager) {
        CUSTOM_LANTERN_MODELS.clear();
        for (LanternRegistry.LanternType type : LanternRegistry.INSTANCE.getValues()) {
            Identifier reg = type.getId();
            String namespace = (reg.getNamespace().equals("minecraft") || reg.getNamespace().equals(Amendments.MOD_ID)) ?
                    "" : reg.getNamespace() + "/";
            // legacy override path
            String legacy = "block/custom_wall_lanterns/" + namespace + reg.getPath();
            if (manager.getResource(Amendments.res("models/" + legacy + ".json")).isPresent()) {
                CUSTOM_LANTERN_MODELS.put(type, Amendments.res(legacy));
                continue;
            }
            WallLanternBlock wallBlock = ModRegistry.WALL_LANTERNS.get(type);
            if (wallBlock == null) continue;
            String model = "block/" + Utils.getID(wallBlock).getPath() + "_lantern";
            if (manager.getResource(Amendments.res("models/" + model + ".json")).isPresent()) {
                CUSTOM_LANTERN_MODELS.put(type, Amendments.res(model));
            }
        }
    }

    public static BlockStateModel getLanternModel(LanternRegistry.LanternType type, BlockState lanternState) {
        Identifier special = CUSTOM_LANTERN_MODELS.get(type);
        if (special != null) {
            BlockStateModel model = ClientHelper.getStandaloneModel(special);
            if (model != null) return model;
        }
        return RenderUtil.getBlockModel(lanternState);
    }
}
