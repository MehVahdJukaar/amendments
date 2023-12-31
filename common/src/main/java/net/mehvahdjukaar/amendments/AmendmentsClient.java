package net.mehvahdjukaar.amendments;

import com.google.common.base.Suppliers;
import net.mehvahdjukaar.amendments.client.ClientResourceGenerator;
import net.mehvahdjukaar.amendments.client.WallLanternModelsManager;
import net.mehvahdjukaar.amendments.client.colors.BrewingStandColor;
import net.mehvahdjukaar.amendments.client.colors.LilyBlockColor;
import net.mehvahdjukaar.amendments.client.colors.MimicBlockColor;
import net.mehvahdjukaar.amendments.client.colors.SoftFluidColor;
import net.mehvahdjukaar.amendments.client.gui.LecternBookEditScreen;
import net.mehvahdjukaar.amendments.client.model.*;
import net.mehvahdjukaar.amendments.client.renderers.*;
import net.mehvahdjukaar.amendments.common.item.DyeBottleItem;
import net.mehvahdjukaar.amendments.integration.CompatObjects;
import net.mehvahdjukaar.amendments.reg.ModRegistry;
import net.mehvahdjukaar.moonlight.api.client.model.NestedModelLoader;
import net.mehvahdjukaar.moonlight.api.misc.EventCalled;
import net.mehvahdjukaar.moonlight.api.platform.ClientHelper;
import net.mehvahdjukaar.moonlight.api.set.BlocksColorAPI;
import net.mehvahdjukaar.moonlight.api.util.Utils;
import net.mehvahdjukaar.supplementaries.client.renderers.color.FluidColor;
import net.mehvahdjukaar.supplementaries.client.screens.PulleyBlockScreen;
import net.mehvahdjukaar.supplementaries.reg.ModMenuTypes;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.renderer.BiomeColors;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.FallingBlockRenderer;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.resources.model.Material;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.datafix.fixes.CauldronRenameFix;
import net.minecraft.world.item.*;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CauldronBlock;
import net.minecraft.world.level.block.entity.BlockEntityType;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Supplier;


public class AmendmentsClient {

    private static final Map<Item, Material> RECORDS = new HashMap<>();
    private static final Material DEFAULT_RECORD = new Material(TextureAtlas.LOCATION_BLOCKS,
            Amendments.res("block/music_disc_template"));
    public static final ModelLayerLocation HANGING_SIGN_EXTENSION = loc("hanging_sign_extension");
    public static final ModelLayerLocation HANGING_SIGN_EXTENSION_CHAINS = loc("hanging_sign_chains");
    public static final ModelLayerLocation SKULL_CANDLE_OVERLAY = loc("skull_candle");

    public static final ResourceLocation BELL_ROPE = Amendments.res("block/bell_rope");
    public static final ResourceLocation BELL_CHAIN = Amendments.res("block/bell_chain");
    public static final ResourceLocation LECTERN_GUI = Amendments.res("textures/gui/lectern.png");

    private static ModelLayerLocation loc(String name) {
        return new ModelLayerLocation(Amendments.res(name), name);
    }

    public static void init() {
        new ClientResourceGenerator().register();

        ClientHelper.addClientSetup(AmendmentsClient::setup);
        ClientHelper.addBlockEntityRenderersRegistration(AmendmentsClient::registerTileRenderers);
        ClientHelper.addModelLoaderRegistration(AmendmentsClient::registerModelLoaders);
        ClientHelper.addBlockColorsRegistration(AmendmentsClient::registerBlockColors);
        ClientHelper.addModelLayerRegistration(AmendmentsClient::registerModelLayers);
        ClientHelper.addSpecialModelRegistration(AmendmentsClient::registerSpecialModels);
        ClientHelper.addEntityRenderersRegistration(AmendmentsClient::registerEntityRenderers);
        ClientHelper.addItemColorsRegistration(AmendmentsClient::registerItemColors);
    }


    @EventCalled
    public static void setup() {
        ClientHelper.registerRenderType(ModRegistry.CARPET_STAIRS.get(), RenderType.translucent());
        ClientHelper.registerRenderType(ModRegistry.WATERLILY_BLOCK.get(), RenderType.cutout());
        ClientHelper.registerRenderType(Blocks.WATER_CAULDRON,RenderType.cutout(),RenderType.translucent());
        ClientHelper.registerRenderType(ModRegistry.LIQUID_CAULDRON.get(),RenderType.cutout(),RenderType.translucent());
        ClientHelper.registerRenderType(ModRegistry.HANGING_FLOWER_POT.get(), RenderType.cutout());
        ClientHelper.registerRenderType(ModRegistry.WALL_LANTERN.get(), RenderType.cutout());
        MenuScreens.register(ModRegistry.LECTERN_EDIT_MENU.get(), LecternBookEditScreen::new);

    }


    @EventCalled
    private static void registerItemColors(ClientHelper.ItemColorEvent event) {
        event.register((itemStack, i) -> i > 0 ? -1 :
                DyeBottleItem.getColor(itemStack), ModRegistry.DYE_BOTTLE_ITEM.get());

    }


    @EventCalled
    private static void registerTileRenderers(ClientHelper.BlockEntityRendererEvent event) {
        event.register(BlockEntityType.JUKEBOX, JukeboxTileRenderer::new);
        event.register(ModRegistry.CEILING_BANNER_TILE.get(), CeilingBannerBlockTileRenderer::new);
        event.register(ModRegistry.SKULL_PILE_TILE.get(), DoubleSkullBlockTileRenderer::new);
        event.register(ModRegistry.SKULL_CANDLE_TILE.get(), CandleSkullBlockTileRenderer::new);
        event.register(ModRegistry.WALL_LANTERN_TILE.get(), WallLanternBlockTileRenderer::new);

    }

    private static void registerEntityRenderers(ClientHelper.EntityRendererEvent event){
        event.register(ModRegistry.FALLING_LANTERN.get(), FallingBlockRenderer::new);
    }

    @EventCalled
    private static void registerSpecialModels(ClientHelper.SpecialModelEvent event) {
        WallLanternModelsManager.registerSpecialModels(event);
        event.register(BELL_ROPE);
        event.register(BELL_CHAIN);
    }

    @EventCalled
    private static void registerModelLoaders(ClientHelper.ModelLoaderEvent event) {
        event.register(Amendments.res("carpet_overlay"), new NestedModelLoader("carpet", CarpetedBlockModel::new));
        event.register(Amendments.res("waterlogged_lily"), WaterloggedLilyModel::new);
        event.register(Amendments.res("wall_lantern"), new NestedModelLoader("support", WallLanternBakedModel::new));
        event.register(Amendments.res("cauldron"), new CauldronModelLoader());

    }

    @EventCalled
    private static void registerModelLayers(ClientHelper.ModelLayerEvent event) {
        event.register(HANGING_SIGN_EXTENSION, HangingSignRendererExtension::createMesh);
        event.register(HANGING_SIGN_EXTENSION_CHAINS, HangingSignRendererExtension::createChainMesh);
        event.register(SKULL_CANDLE_OVERLAY, SkullCandleOverlayModel::createMesh);
    }

    @EventCalled
    private static void registerBlockColors(ClientHelper.BlockColorEvent event) {
        event.register(new MimicBlockColor(), ModRegistry.CARPET_STAIRS.get(), ModRegistry.WALL_LANTERN.get());
        event.register(new LilyBlockColor(), ModRegistry.WATERLILY_BLOCK.get());
        event.register((blockState, level, pos, i) -> i==1 && level != null && pos != null ? BiomeColors.getAverageWaterColor(level, pos) : -1,
                Blocks.WATER_CAULDRON);
        event.register(new BrewingStandColor(), Blocks.BREWING_STAND);
        event.register(new SoftFluidColor(), ModRegistry.LIQUID_CAULDRON.get());
    }


    public static Map<Item, Material> getAllRecords() {
        if (RECORDS.isEmpty()) {
            for (var i : BuiltInRegistries.ITEM) {
                if (i instanceof RecordItem) {
                    RECORDS.put(i, new Material(TextureAtlas.LOCATION_BLOCKS,
                            Amendments.res("block/" + Utils.getID(i).toString()
                                    .replace("minecraft:", "")
                                    .replace(":", "/"))));
                }
            }
        }
        return RECORDS;
    }

    public static Material getRecordMaterial(Item item) {
        return getAllRecords().getOrDefault(item, DEFAULT_RECORD);
    }


    public static final Supplier<Map<Block, ResourceLocation>> SKULL_CANDLES_TEXTURES = Suppliers.memoize(() -> {
        Map<Block, ResourceLocation> map = new LinkedHashMap<>();
        //first key and default one too
        map.put(Blocks.CANDLE, Amendments.res("textures/block/skull_candles/default.png"));
        for (DyeColor color : DyeColor.values()) {
            Block candle = BlocksColorAPI.getColoredBlock("candle", color);
            map.put(candle, Amendments.res("textures/block/skull_candles/" + color.getName() + ".png"));
        }
        //worst case this becomes null
        if (CompatObjects.SOUL_CANDLE.get() != null) {
            map.put(CompatObjects.SOUL_CANDLE.get(), Amendments.res("textures/block/skull_candles/soul.png"));
        }
        if (CompatObjects.SPECTACLE_CANDLE.get() != null) {
            map.put(CompatObjects.SPECTACLE_CANDLE.get(), Amendments.res("textures/block/skull_candles/spectacle.png"));
        }
        return map;
    });


}
