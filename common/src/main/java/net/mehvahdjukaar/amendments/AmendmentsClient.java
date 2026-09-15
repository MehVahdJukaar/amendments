package net.mehvahdjukaar.amendments;

import com.google.common.base.Suppliers;
import net.mehvahdjukaar.amendments.client.ClientResourceGenerator;
import net.mehvahdjukaar.amendments.client.ItemHoldingAnimationsManager;
import net.mehvahdjukaar.amendments.client.ModRenderTypes;
import net.mehvahdjukaar.amendments.client.colors.BrewingStandColor;
import net.mehvahdjukaar.amendments.client.colors.CarpetBlockColor;
import net.mehvahdjukaar.amendments.client.colors.MimicBlockColor;
import net.mehvahdjukaar.amendments.client.colors.SoftFluidColor;
import net.mehvahdjukaar.amendments.client.gui.LecternBookEditScreen;
import net.mehvahdjukaar.amendments.client.model.*;
import net.mehvahdjukaar.amendments.client.particles.*;
import net.mehvahdjukaar.amendments.client.renderers.*;
import net.mehvahdjukaar.amendments.common.ProjectileStats;
import net.mehvahdjukaar.amendments.common.block.BoilingWaterCauldronBlock;
import net.mehvahdjukaar.amendments.configs.ClientConfigs;
import net.mehvahdjukaar.amendments.integration.CompatHandler;
import net.mehvahdjukaar.amendments.integration.CompatObjects;
import net.mehvahdjukaar.amendments.integration.SuppCompat;
import net.mehvahdjukaar.amendments.reg.ModRegistry;
import net.mehvahdjukaar.candlelight.api.PlatformImpl;
import net.mehvahdjukaar.moonlight.api.client.model.NestedUnbakedModel;
import net.mehvahdjukaar.moonlight.api.misc.EventCalled;
import net.mehvahdjukaar.moonlight.api.platform.ClientHelper;
import net.mehvahdjukaar.moonlight.api.platform.RegHelper;
import net.mehvahdjukaar.moonlight.api.set.BlocksColorAPI;
import net.mehvahdjukaar.moonlight.api.util.Utils;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.WidgetSprites;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.renderer.entity.FallingBlockRenderer;
import net.minecraft.client.renderer.entity.ThrownItemRenderer;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.resources.model.sprite.SpriteId;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.properties.WoodType;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

// TODO: rewrite as Vanillin compat for 1.21
// import net.mehvahdjukaar.amendments.integration.FlywheelCompat;


public class AmendmentsClient {


    public static final Set<Block> SIGN_THAT_WE_RENDER_AS_BLOCKS = new HashSet<>();

    public static final Identifier SIGN_SHEET = Identifier.withDefaultNamespace("textures/atlas/signs.png");

    public static final SpriteId CANVAS_SIGH_MATERIAL = new SpriteId(SIGN_SHEET,
            Amendments.res("entity/signs/hanging/farmersdelight/extension_canvas"));

    public static final Supplier<Map<WoodType, SpriteId>> HANGING_SIGN_EXTENSIONS =
            Suppliers.memoize(() -> WoodType.values().collect(Collectors.toMap(
                    Function.identity(),
                    w -> {
                        String str = w.name();
                        if (str.contains(":")) {
                            str = str.replace(":", "/extension_");
                        } else str = "extension_" + str;
                        return new SpriteId(SIGN_SHEET, Amendments.res("entity/signs/hanging/" + str));
                    },
                    (v1, v2) -> v1,
                    IdentityHashMap::new)));

    private static final Map<Item, SpriteId> RECORD_MATERIALS = new HashMap<>();
    public static final SpriteId DEFAULT_RECORD = new SpriteId(TextureAtlas.LOCATION_BLOCKS,
            Amendments.res("block/music_discs/music_disc_template"));
    public static final SpriteId TINTED_RECORD = new SpriteId(TextureAtlas.LOCATION_BLOCKS,
            Amendments.res("block/music_discs/music_disc_tinted"));
    public static final List<SpriteId> RECORD_PATTERNS = List.of(
            new SpriteId(TextureAtlas.LOCATION_BLOCKS, Amendments.res("block/music_discs/music_disc_pattern_0")),
            new SpriteId(TextureAtlas.LOCATION_BLOCKS, Amendments.res("block/music_discs/music_disc_pattern_1")),
            new SpriteId(TextureAtlas.LOCATION_BLOCKS, Amendments.res("block/music_discs/music_disc_pattern_2")),
            new SpriteId(TextureAtlas.LOCATION_BLOCKS, Amendments.res("block/music_discs/music_disc_pattern_3")),
            new SpriteId(TextureAtlas.LOCATION_BLOCKS, Amendments.res("block/music_discs/music_disc_pattern_4")),
            new SpriteId(TextureAtlas.LOCATION_BLOCKS, Amendments.res("block/music_discs/music_disc_pattern_5"))
    );
    public static final List<SpriteId> RECORD_PATTERNS_OVERLAY = List.of(
            new SpriteId(TextureAtlas.LOCATION_BLOCKS, Amendments.res("block/music_discs/music_disc_pattern_0s")),
            new SpriteId(TextureAtlas.LOCATION_BLOCKS, Amendments.res("block/music_discs/music_disc_pattern_1s")),
            new SpriteId(TextureAtlas.LOCATION_BLOCKS, Amendments.res("block/music_discs/music_disc_pattern_2s")),
            new SpriteId(TextureAtlas.LOCATION_BLOCKS, Amendments.res("block/music_discs/music_disc_pattern_3s")),
            new SpriteId(TextureAtlas.LOCATION_BLOCKS, Amendments.res("block/music_discs/music_disc_pattern_4s")),
            new SpriteId(TextureAtlas.LOCATION_BLOCKS, Amendments.res("block/music_discs/music_disc_pattern_5s"))
    );

    public static final ModelLayerLocation HANGING_SIGN_EXTENSION = loc("hanging_sign_extension");
    public static final ModelLayerLocation HANGING_SIGN_EXTENSION_CHAINS = loc("hanging_sign_chains");
    public static final ModelLayerLocation SKULL_CANDLE_OVERLAY = loc("skull_candle");
    public static final ModelLayerLocation BIG_THROWN_BALL = loc("bit_thrown_ball");
    public static final ModelLayerLocation MEDIUM_THROWN_BALL = loc("medium_thrown_ball");
    public static final ModelLayerLocation SMALL_THROWN_BALL = loc("small_thrown_ball");

    public static final Identifier BELL_ROPE = Amendments.res("block/bell_rope");
    public static final Identifier BELL_CHAIN = Amendments.res("block/bell_chain");
    public static final Identifier LECTERN_GUI = Amendments.res("textures/gui/lectern.png");
    public static final WidgetSprites BUTTON_SPRITES = new WidgetSprites(
            Amendments.res("widget/button"), Amendments.res("widget/button_highlighted"));
    public static final Identifier POTION_TEXTURE = Amendments.res("block/potion_cauldron");
    public static final Identifier MUSHROOM_STEW = Amendments.res("block/mushroom_stew_cauldron");
    public static final Identifier RABBIT_STEW = Amendments.res("block/rabbit_stew_cauldron");
    public static final Identifier BEETROOT_SOUP = Amendments.res("block/beetroot_soup_cauldron");
    public static final Identifier SUS_STEW = Amendments.res("block/suspicious_stew_cauldron");
    public static final Identifier BLAZE_TEXTURE = Amendments.res("textures/entity/projectile/blazeball_3d.png");
    public static final Identifier FIREBALL_TEXTURE = Amendments.res("textures/entity/projectile/fireball_3d.png");
    public static final Identifier FIREBALL_OFF_TEXTURE = Amendments.res("textures/entity/projectile/fireball_3d_off.png");
    public static final Identifier FIREBALL_OVERLAY_TEXTURE = Amendments.res("textures/entity/projectile/fireball_3d_overlay.png");
    public static final Identifier DRAGON_FIREBALL_TEXTURE = Amendments.res("textures/entity/projectile/dragon_fireball_3d.png");
    public static final Identifier DRAGON_FIREBALL_OVERLAY_TEXTURE = Amendments.res("textures/entity/projectile/dragon_fireball_3d_overlay.png");
    public static final Identifier SNOWBALL_TEXTURE = Amendments.res("textures/entity/projectile/snowball_3d.png");
    public static final Identifier SLIMEBALL_TEXTURE = Amendments.res("textures/entity/projectile/slimeball_3d.png");

    private static ModelLayerLocation loc(String name) {
        return new ModelLayerLocation(Amendments.res(name), name);
    }

    public static boolean WAS_INIT = false;

    public static void init() {
        WAS_INIT = true;
        RegHelper.registerDynamicResourceProvider(new ClientResourceGenerator());

        ClientHelper.addClientSetup(AmendmentsClient::setup);
        ClientHelper.addRenderPipelineRegistration(e -> e.register(ModRenderTypes.ENTITY_LIT_PIPELINE));
        ClientHelper.addBlockEntityRenderersRegistration(AmendmentsClient::registerTileRenderers);
        ClientHelper.addBlockModelRegistration(AmendmentsClient::registerBlockModels);
        ClientHelper.addBlockColorsRegistration(AmendmentsClient::registerBlockColors);
        ClientHelper.addModelLayerRegistration(AmendmentsClient::registerModelLayers);
        ClientHelper.addSpecialModelRegistration(AmendmentsClient::registerSpecialModels);
        ClientHelper.addEntityRenderersRegistration(AmendmentsClient::registerEntityRenderers);
        //item tints are json only now, these live in assets/amendments/items/<id>.json
        //ClientHelper.addItemColorsRegistration(AmendmentsClient::registerItemColors);
        ClientHelper.addParticleRegistration(AmendmentsClient::registerParticles);
        ClientHelper.addMenuScreensRegistration(AmendmentsClient::registerMenuScreens);
        if (CompatHandler.ENHANCED_BLOCK_ENTITIES) {
            ClientHelper.addClientLoginCallback(AmendmentsClient::warnAboutEnhancedBlockEntities);
        }
        // TODO: rewrite as Vanillin compat for 1.21
        // if (CompatHandler.FLYWHEEL) FlywheelCompat.init();
    }


    @EventCalled
    private static void warnAboutEnhancedBlockEntities() {
        boolean disabled = ClientConfigs.NO_EBE_WARN.get();
        if (disabled) return;
        Player player = ClientHelper.getLocalPlayer();
        if (player == null) return;
        player.sendSystemMessage(Component.translatable("message.amendments.ebe_warning")
                .withStyle(ChatFormatting.RED));
    }

    public static float x;
    public static float y;
    public static float z;

    @EventCalled
    public static void setup() {
        //render type goes in the block model jsons instead
        //ClientHelper.registerRenderType(ModRegistry.CARPET_STAIRS.get(), RenderTypes.cutout(), RenderTypes.solid(), RenderTypes.translucent());
        //ClientHelper.registerRenderType(ModRegistry.CARPET_SLAB.get(), RenderTypes.cutout(), RenderTypes.solid(), RenderTypes.translucent());
        //ClientHelper.registerRenderType(ModRegistry.WATERLILY_BLOCK.get(), RenderTypes.cutout());
        //ClientHelper.registerRenderType(Blocks.WATER_CAULDRON, RenderTypes.cutout(), RenderTypes.translucent());
        //ClientHelper.registerRenderType(ModRegistry.LIQUID_CAULDRON.get(), RenderTypes.cutout(), RenderTypes.translucent());
        //ClientHelper.registerRenderType(ModRegistry.DYE_CAULDRON.get(), RenderTypes.cutout(), RenderTypes.translucent());
        //ClientHelper.registerRenderType(ModRegistry.HANGING_FLOWER_POT.get(), RenderTypes.cutout());
        //for (var wallLantern : ModRegistry.WALL_LANTERNS.values()) {
        //    ClientHelper.registerRenderType(wallLantern, RenderTypes.cutout());
        //}
        //ClientHelper.registerRenderType(ModRegistry.TOOL_HOOK.get(), RenderTypes.cutout());
    }

    public static void afterTagSetup() {
        ItemHoldingAnimationsManager.addAnimations();
    }

    @EventCalled
    private static void registerMenuScreens(ClientHelper.MenuScreenEvent event) {
        event.register(ModRegistry.LECTERN_EDIT_MENU.get(), LecternBookEditScreen::new);
    }

    //@EventCalled
    //private static void registerItemColors(ClientHelper.ItemColorEvent event) {
    //    event.register((itemStack, i) -> i > 0 ? -1 :
    //            itemStack.get(DataComponents.DYED_COLOR).rgb(), ModRegistry.DYE_BOTTLE_ITEM.get());
    //
    //    event.register(new CrossbowColor(), Items.CROSSBOW);
    //}

    @EventCalled
    private static void registerTileRenderers(ClientHelper.BlockEntityRendererEvent event) {
        event.register(BlockEntityType.JUKEBOX, JukeboxTileRenderer::new);
        event.register(ModRegistry.CEILING_BANNER_TILE.get(), CeilingBannerBlockTileRenderer::new);
        event.register(ModRegistry.SKULL_PILE_TILE.get(), DoubleSkullBlockTileRenderer::new);
        event.register(ModRegistry.SKULL_CANDLE_TILE.get(), CandleSkullBlockTileRenderer::new);
        event.register(ModRegistry.WALL_LANTERN_TILE.get(), WallLanternBlockTileRenderer::new);
        event.register(ModRegistry.TOOL_HOOK_TILE.get(), ToolHookTileRenderer::new);

    }

    private static void registerParticles(ClientHelper.ParticleEvent event) {
        event.register(ModRegistry.BOILING_PARTICLE.get(), BoilingParticle.Provider::new);
        event.register(ModRegistry.SPLASH_PARTICLE.get(), ColoredSplashParticle.Provider::new);
        event.register(ModRegistry.FIREBALL_TRAIL_PARTICLE.get(), FireballTrailParticle.Provider::new);
        event.register(ModRegistry.FIREBALL_EMITTER_PARTICLE.get(), FireballExplosionEmitterParticle.Provider::new);
        event.register(ModRegistry.FIREBALL_EXPLOSION_PARTICLE.get(), FireballExplosionParticle.Provider::new);
    }

    @EventCalled
    private static void registerEntityRenderers(ClientHelper.EntityRendererEvent event) {
        event.register(ModRegistry.FALLING_LANTERN.get(), FallingBlockRenderer::new);

        float modelScale = 0.75f;
        //override vanilla renderers
        if (ClientConfigs.FIREBALL_3D.get()) {
            //same visual scale as the original
            event.register(EntityType.SMALL_FIREBALL, context -> new Fireball3DRenderer<>(context,
                    modelScale * ProjectileStats.BLAZE_FIREBALL.modelSize(),
                    BLAZE_TEXTURE, FIREBALL_OVERLAY_TEXTURE, FIREBALL_OFF_TEXTURE,
                    MEDIUM_THROWN_BALL, true));
            event.register(EntityType.FIREBALL, context -> new Fireball3DRenderer<>(context,
                    modelScale * ProjectileStats.GHAST_FIREBALL.modelSize(),
                    FIREBALL_TEXTURE, FIREBALL_OVERLAY_TEXTURE, FIREBALL_OFF_TEXTURE,
                    BIG_THROWN_BALL, false));
            event.register(EntityType.DRAGON_FIREBALL, context -> new Fireball3DRenderer<>(context,
                    modelScale * ProjectileStats.DRAGON_FIREBALL.modelSize(),
                    DRAGON_FIREBALL_TEXTURE, DRAGON_FIREBALL_OVERLAY_TEXTURE, null,
                    BIG_THROWN_BALL, false));

            //mod own entities
            event.register(ModRegistry.MEDIUM_DRAGON_FIREBALL.get(), context -> new Fireball3DRenderer<>(context,
                    modelScale * ProjectileStats.DRAGON_CHARGE.modelSize(),
                    DRAGON_FIREBALL_TEXTURE, DRAGON_FIREBALL_OVERLAY_TEXTURE, null,
                    BIG_THROWN_BALL, true));

            event.register(ModRegistry.MEDIUM_FIREBALL.get(), context -> new Fireball3DRenderer<>(context,
                    modelScale * ProjectileStats.PLAYER_FIREBALL.modelSize(),
                    FIREBALL_TEXTURE, FIREBALL_OVERLAY_TEXTURE, FIREBALL_OFF_TEXTURE,
                    BIG_THROWN_BALL, true));

        } else {
            event.register(ModRegistry.MEDIUM_FIREBALL.get(), ThrownItemRenderer::new);
            event.register(ModRegistry.MEDIUM_DRAGON_FIREBALL.get(), ThrownItemRenderer::new);
        }
        if (ClientConfigs.SNOWBALL_3D.get()) {
            event.register(EntityType.SNOWBALL, context -> new Small3DBallRenderer(context,
                    modelScale, SNOWBALL_TEXTURE, false));
        }
        if (CompatHandler.SUPPLEMENTARIES && ClientConfigs.SLIMEBALL_3D.get()) {
            event.register(SuppCompat.getSlimeBall(), context -> new Small3DBallRenderer(context,
                    modelScale, SLIMEBALL_TEXTURE, true));
        }
    }

    @EventCalled
    private static void registerSpecialModels(ClientHelper.SpecialModelEvent event) {
        //bell rope and chain were standalone models, no cross loader api for those yet
        //WallLanternModelsManager.refreshModels(Minecraft.getInstance().getResourceManager());
        //WallLanternModelsManager.registerSpecialModels(event);
        //event.register(BELL_CHAIN);
        //if (CompatHandler.SUPPLEMENTARIES) event.register(BELL_ROPE);
    }

    @EventCalled
    private static void registerBlockModels(ClientHelper.BlockModelEvent event) {
        event.register(Amendments.res("carpet_overlay"), NestedUnbakedModel.codec("carpet", CarpetedBlockModel::new));
        event.register(Amendments.res("waterlogged_lily"), WaterloggedLilyModel.Unbaked.CODEC);
        event.register(Amendments.res("wall_lantern"), NestedUnbakedModel.codec("support", WallLanternBakedModel::new));
        event.register(Amendments.res("cauldron"), CauldronBakedModel.Unbaked.CODEC);
        event.register(Amendments.res("hanging_pot"), NestedUnbakedModel.codec("rope", HangingPotBakedModel::new));
        event.register(Amendments.res("tool_hook"), NestedUnbakedModel.codec("hook", ToolHookBakedModel::new));
    }

    @EventCalled
    private static void registerModelLayers(ClientHelper.ModelLayerEvent event) {
        event.register(HANGING_SIGN_EXTENSION, HangingSignRendererExtension::createMesh);
        event.register(HANGING_SIGN_EXTENSION_CHAINS, HangingSignRendererExtension::createChainMesh);
        event.register(SKULL_CANDLE_OVERLAY, SkullCandleOverlayModel::createMesh);
        event.register(BIG_THROWN_BALL, () -> ThrownProjectile3DRenderer.createMesh(8));
        event.register(MEDIUM_THROWN_BALL, () -> ThrownProjectile3DRenderer.createMesh(6));
        event.register(SMALL_THROWN_BALL, () -> ThrownProjectile3DRenderer.createMesh(4));
    }

    @EventCalled
    private static void registerBlockColors(ClientHelper.BlockColorEvent event) {
        List<Block> mimics = new ArrayList<>();
        mimics.addAll(ModRegistry.WALL_LANTERNS.values());
        mimics.addAll(List.of(ModRegistry.HANGING_FLOWER_POT.get(),
                ModRegistry.WATERLILY_BLOCK.get()));
        mimics.addAll(ModRegistry.DOUBLE_CAKES.values());
        event.register(MimicBlockColor.layers(), mimics.toArray(new Block[0]));
        event.register(CarpetBlockColor.layers(), ModRegistry.CARPET_SLAB.get(), ModRegistry.CARPET_STAIRS.get());
        //event.register(new LilyBlockColor(), ModRegistry.WATERLILY_BLOCK.get());
        event.register(List.of(BoilingWaterCauldronBlock.waterColor()), Blocks.WATER_CAULDRON);
        event.register(BrewingStandColor.layers(), Blocks.BREWING_STAND);
        event.register(SoftFluidColor.layers(), ModRegistry.DYE_CAULDRON.get(), ModRegistry.LIQUID_CAULDRON.get());
    }


    public static Map<Item, SpriteId> getAllRecords() {
        if (RECORD_MATERIALS.isEmpty()) {
            for (var i : BuiltInRegistries.ITEM) {
                if (i.components().get(DataComponents.JUKEBOX_PLAYABLE) != null) {
                    RECORD_MATERIALS.put(i, discMaterial(i));
                }
            }
            if (CompatHandler.CAVERNS_AND_CHASMS) {
                Item i = BuiltInRegistries.ITEM.getValue(Identifier.fromNamespaceAndPath("caverns_and_chasms", "music_disc_copy"));
                if (i != null) RECORD_MATERIALS.put(i, discMaterial(i));
            }
        }
        return RECORD_MATERIALS;
    }

    private static @NotNull SpriteId discMaterial(Item i) {
        return new SpriteId(TextureAtlas.LOCATION_BLOCKS,
                Amendments.res("block/music_discs/" + Utils.getID(i).toString()
                        .replace("minecraft:", "")
                        .replace(":", "/")));
    }

    public static SpriteId getRecordMaterial(Item item) {
        return getAllRecords().getOrDefault(item, DEFAULT_RECORD);
    }


    public static final Supplier<Map<Block, Identifier>> SKULL_CANDLES_TEXTURES = Suppliers.memoize(() -> {
        Map<Block, Identifier> map = new LinkedHashMap<>();
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
        if (CompatObjects.CUPRIC_CANDLE.get() != null) {
            map.put(CompatObjects.CUPRIC_CANDLE.get(), Amendments.res("textures/block/skull_candles/cupric.png"));
        }
        if (CompatObjects.ENDER_CANDLE.get() != null) {
            map.put(CompatObjects.ENDER_CANDLE.get(), Amendments.res("textures/block/skull_candles/ender.png"));
        }
        if (CompatObjects.SPECTACLE_CANDLE.get() != null) {
            map.put(CompatObjects.SPECTACLE_CANDLE.get(), Amendments.res("textures/block/skull_candles/spectacle.png"));
        }
        return map;
    });


    //TODO: add
    @EventCalled
    public static void onItemTooltip(ItemStack stack, Item.TooltipContext tooltipContext, TooltipFlag tooltipFlag, List<Component> list) {
        if (ClientConfigs.TOOLTIP_HINTS.get()) {
            // InteractEvents.addOverrideTooltips(itemStack, tooltipFlag, components);
        }
    }

    @PlatformImpl
    public static boolean hasFixedNormals() {
        throw new AssertionError();
    }

    public static void withClientLevel(Consumer<Level> o) {
        o.accept(Minecraft.getInstance().level);
    }

}
