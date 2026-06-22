package net.mehvahdjukaar.amendments.client;

import net.mehvahdjukaar.amendments.Amendments;
import net.mehvahdjukaar.amendments.common.LanternRegistry;
import net.mehvahdjukaar.amendments.common.block.WallLanternBlock;
import net.mehvahdjukaar.amendments.reg.ModRegistry;
import net.mehvahdjukaar.moonlight.api.client.util.RenderUtil;
import net.mehvahdjukaar.moonlight.api.platform.ClientHelper;
import net.mehvahdjukaar.moonlight.api.util.Utils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.BlockModelShaper;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.world.level.block.state.BlockState;

import java.util.IdentityHashMap;
import java.util.Map;

// Optional per-type overrides for the dangling lantern model (BER + fast mode).
public class WallLanternModelsManager {

    private static final Map<LanternRegistry.LanternType, ModelResourceLocation> CUSTOM_LANTERN_MODELS = new IdentityHashMap<>();

    public static void refreshModels(ResourceManager manager) {
        reloadCustomLanternModels(manager);
    }

    private static void reloadCustomLanternModels(ResourceManager manager) {
        CUSTOM_LANTERN_MODELS.clear();
        for (LanternRegistry.LanternType type : LanternRegistry.INSTANCE.getValues()) {
            ResourceLocation reg = type.getId();
            String namespace = (reg.getNamespace().equals("minecraft") || reg.getNamespace().equals(Amendments.MOD_ID)) ? "" : reg.getNamespace() + "/";
            // legacy override path
            String legacy = "block/custom_wall_lanterns/" + namespace + reg.getPath();
            ResourceLocation legacyPath = Amendments.res("models/" + legacy + ".json");
            if (manager.getResource(legacyPath).isPresent()) {
                CUSTOM_LANTERN_MODELS.put(type, RenderUtil.getStandaloneModelLocation(Amendments.res(legacy)));
                continue;
            }
            // per-type override for the dangling lantern only
            WallLanternBlock wallBlock = ModRegistry.WALL_LANTERNS.get(type);
            if (wallBlock != null) {
                ResourceLocation id = Utils.getID(wallBlock);
                ResourceLocation modelPath = Amendments.res("models/block/" + id.getPath() + "_lantern.json");
                if (manager.getResource(modelPath).isPresent()) {
                    CUSTOM_LANTERN_MODELS.put(type, RenderUtil.getStandaloneModelLocation(
                            Amendments.res("block/" + id.getPath() + "_lantern")));
                }
            }
        }
    }

    public static void registerSpecialModels(ClientHelper.SpecialModelEvent event) {
        CUSTOM_LANTERN_MODELS.values().forEach(e -> event.register(e.id()));
    }

    public static BakedModel getLanternModel(BlockModelShaper blockModelShaper, LanternRegistry.LanternType type, BlockState lanternState) {
        var special = CUSTOM_LANTERN_MODELS.get(type);
        if (special != null) {
            return ClientHelper.getModel(Minecraft.getInstance().getModelManager(), special);
        }
        return blockModelShaper.getBlockModel(lanternState);
    }
}
