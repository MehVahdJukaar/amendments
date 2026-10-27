package net.mehvahdjukaar.amendments.reg;

import com.google.common.base.Preconditions;
import it.unimi.dsi.fastutil.objects.Object2ObjectLinkedOpenHashMap;
import net.mehvahdjukaar.amendments.Amendments;
import net.mehvahdjukaar.amendments.common.CakeRegistry;
import net.mehvahdjukaar.amendments.common.LanternRegistry;
import net.mehvahdjukaar.amendments.common.LecternEditMenu;
import net.mehvahdjukaar.amendments.common.block.*;
import net.mehvahdjukaar.amendments.common.entity.FallingLanternEntity;
import net.mehvahdjukaar.amendments.common.entity.MediumDragonFireball;
import net.mehvahdjukaar.amendments.common.entity.MediumFireball;
import net.mehvahdjukaar.amendments.common.item.DragonChargeItem;
import net.mehvahdjukaar.amendments.common.item.DyeBottleItem;
import net.mehvahdjukaar.amendments.common.item.placement.WallLanternPlacement;
import net.mehvahdjukaar.amendments.common.recipe.CauldronRecipe;
import net.mehvahdjukaar.amendments.common.recipe.DyeBottleRecipe;
import net.mehvahdjukaar.amendments.common.tile.*;
import net.mehvahdjukaar.amendments.configs.CommonConfigs;
import net.mehvahdjukaar.moonlight.api.fluids.SoftFluid;
import net.mehvahdjukaar.moonlight.api.fluids.SoftFluidRegistry;
import net.mehvahdjukaar.moonlight.api.item.additional_placements.AdditionalItemPlacementsAPI;
import net.mehvahdjukaar.moonlight.api.misc.HolderRef;
import net.mehvahdjukaar.moonlight.api.misc.RegSupplier;
import net.mehvahdjukaar.moonlight.api.misc.Registrator;
import net.mehvahdjukaar.moonlight.api.platform.PlatHelper;
import net.mehvahdjukaar.moonlight.api.platform.RegHelper;
import net.mehvahdjukaar.moonlight.api.set.BlockSetAPI;
import net.mehvahdjukaar.moonlight.api.set.BlocksColorAPI;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.Util;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.PushReaction;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import net.mehvahdjukaar.moonlight.api.util.Utils;

import java.util.function.Function;
import java.util.function.Supplier;

import static net.mehvahdjukaar.amendments.Amendments.res;
import static net.mehvahdjukaar.amendments.reg.ModConstants.*;

public class ModRegistry {


    public static void init() {
        BlockSetAPI.registerBlockSetDefinition(CakeRegistry.INSTANCE);
        BlockSetAPI.registerBlockSetDefinition(LanternRegistry.INSTANCE);
        BlockSetAPI.addDynamicRegistration(Amendments.MOD_ID, ModRegistry::registerDoubleCakes, BuiltInRegistries.BLOCK);
        BlockSetAPI.addDynamicRegistration(Amendments.MOD_ID, ModRegistry::registerWallLanterns, BuiltInRegistries.BLOCK);
        RegHelper.addExtraBEBlockStatesRegistration(event -> {
            event.addBlocks(WALL_LANTERN_TILE.get(), WALL_LANTERNS.values().toArray(new WallLanternBlock[0]));
        });
    }

    public static void registerAdditionalPlacements() {
        if (CommonConfigs.WALL_LANTERN.get()) {
            for (LanternRegistry.LanternType type : LanternRegistry.INSTANCE) {
                WallLanternBlock wallBlock = WALL_LANTERNS.get(type);
                if (wallBlock != null) {
                    AdditionalItemPlacementsAPI.registerPlacement(type.lantern.asItem(),
                            new WallLanternPlacement(wallBlock));
                }
            }
        }
        if (CommonConfigs.HANGING_POT.get()) {
            AdditionalItemPlacementsAPI.registerSimplePlacement(Items.FLOWER_POT, HANGING_FLOWER_POT.get());
        }
        if (CommonConfigs.CEILING_BANNERS.get()) {
            for (var e : CEILING_BANNERS.entrySet()) {
                Item item = BlocksColorAPI.getColoredItem("banner", e.getKey());
                if (item == Items.AIR) {
                    throw new IllegalStateException("Block " + e.getValue().get() + " has no corresponding item! How did this happen? Some OTHER mod must have screwed up the block to items map!");
                }
                AdditionalItemPlacementsAPI.registerSimplePlacement(Preconditions.checkNotNull(item),
                        e.getValue().get());
            }
        }
    }

    public static final HolderRef<DamageType> BOILING_DAMAGE = HolderRef.of(
            res("boiling"), Registries.DAMAGE_TYPE);


    public static final HolderRef<SoftFluid> DYE_SOFT_FLUID = HolderRef.of(res("dye"),
            SoftFluidRegistry.KEY);

    public static final RegSupplier<RecipeSerializer<DyeBottleRecipe>> DYE_BOTTLE_RECIPE = RegHelper.registerSpecialRecipe(
            res("dye_bottle"), DyeBottleRecipe::new);

    public static final Supplier<MenuType<LecternEditMenu>> LECTERN_EDIT_MENU = RegHelper.registerMenuType(
            res("lectern_edit"), LecternEditMenu::of
    );

    public static final RegSupplier<SimpleParticleType> BOILING_PARTICLE = RegHelper.registerParticle(res("boiling_bubble"));
    public static final RegSupplier<SimpleParticleType> SPLASH_PARTICLE = RegHelper.registerParticle(res("fluid_splash"));
    public static final Supplier<SimpleParticleType> DRAGON_FIREBALL_TRAIL_PARTICLE = RegHelper.registerParticle(res("dragon_fireball_trail"));
    public static final Supplier<SimpleParticleType> FIREBALL_TRAIL_PARTICLE = RegHelper.registerParticle(res("fireball_trail"));
    public static final Supplier<SimpleParticleType> FIREBALL_EMITTER_PARTICLE = RegHelper.registerParticle(res("fireball_explosion_emitter"));
    public static final Supplier<SimpleParticleType> FIREBALL_EXPLOSION_PARTICLE = RegHelper.registerParticle(res("fireball_explosion"));

    public static final RegSupplier<SoundEvent> FIREBALL_EXPLOSION_SOUND = RegHelper.registerSound(res("explosion.fireball"));

    public static final Supplier<RecipeType<CauldronRecipe>> CAULDRON_RECIPE_TYPE = RegHelper.registerRecipeType(
            res("cauldron_crafting"));

    public static final Supplier<RecipeSerializer<CauldronRecipe>> CAULDRON_RECIPE_SERIALIZER = RegHelper.registerRecipeSerializer(
            res("cauldron_crafting"), () -> new RecipeSerializer<>(CauldronRecipe.MAP_CODEC, CauldronRecipe.STREAM_CODEC));

    public static final Supplier<EntityType<MediumDragonFireball>> MEDIUM_DRAGON_FIREBALL =
            regEntity("medium_dragon_fireball",
                    EntityType.Builder.<MediumDragonFireball>of(MediumDragonFireball::new, MobCategory.MISC)
                            .sized(0.3125F, 0.3125F)
                            .clientTrackingRange(16)
                           // .fireImmune() //same as vanilla
                            .updateInterval(5));

    public static final Supplier<EntityType<MediumFireball>> MEDIUM_FIREBALL =
            regEntity("medium_fireball",
                    EntityType.Builder.<MediumFireball>of(MediumFireball::new, MobCategory.MISC)
                            .sized(0.3125F, 0.3125F)
                            .clientTrackingRange(16)
                            // .fireImmune() //same as vanilla
                            .updateInterval(5));

    public static final Supplier<Item> DRAGON_CHARGE = regItem(DRAGON_CHARGE_NAME,
            DragonChargeItem::new, new Item.Properties());

    public static final Supplier<Item> DYE_BOTTLE_ITEM = regItem(DYE_BOTTLE_NAME,
            DyeBottleItem::new, new Item.Properties()
                    .component(DataComponents.DYED_COLOR, DyeBottleItem.RED_COLOR)
                    .stacksTo(1)
                    .craftRemainder(Items.GLASS_BOTTLE));

    //lilypad
    public static final Supplier<Block> WATERLILY_BLOCK = regBlock(WATER_LILY_NAME,
            WaterloggedLilyBlock::new, () -> BlockBehaviour.Properties.ofFullCopy(Blocks.LILY_PAD).instabreak()
                    .sound(SoundType.LILY_PAD).noOcclusion()
    );

    public static final Supplier<BlockEntityType<WaterloggedLilyBlockTile>> WATERLILY_TILE = regTile(WATER_LILY_NAME,
            () -> PlatHelper.newBlockEntityType(WaterloggedLilyBlockTile::new, WATERLILY_BLOCK.get())
    );

    //cauldron
    public static final Supplier<LiquidCauldronBlock> LIQUID_CAULDRON = regBlock(LIQUID_CAULDRON_NAME,
            LiquidCauldronBlock::new, () -> BlockBehaviour.Properties.ofFullCopy(Blocks.CAULDRON)
    );
    public static final Supplier<Block> DYE_CAULDRON = regBlock(DYE_CAULDRON_NAME,
            DyeCauldronBlock::new, () -> BlockBehaviour.Properties.ofFullCopy(Blocks.CAULDRON)
    );

    public static final Supplier<BlockEntityType<LiquidCauldronBlockTile>> LIQUID_CAULDRON_TILE = regTile(LIQUID_CAULDRON_NAME,
            () -> PlatHelper.newBlockEntityType(LiquidCauldronBlockTile::new, LIQUID_CAULDRON.get(), DYE_CAULDRON.get())
    );

    //hanging flower pot
    public static final Supplier<Block> HANGING_FLOWER_POT = regBlock(HANGING_FLOWER_POT_NAME,
            HangingFlowerPotBlock::new, () -> BlockBehaviour.Properties.ofFullCopy(Blocks.FLOWER_POT));

    public static final Supplier<BlockEntityType<HangingFlowerPotBlockTile>> HANGING_FLOWER_POT_TILE = regTile(
            HANGING_FLOWER_POT_NAME, () -> PlatHelper.newBlockEntityType(
                    HangingFlowerPotBlockTile::new, HANGING_FLOWER_POT.get()));


    //ceiling banners
    public static final Map<DyeColor, Supplier<Block>> CEILING_BANNERS = Util.make(() -> {
        Map<DyeColor, Supplier<Block>> map = new Object2ObjectLinkedOpenHashMap<>();
        for (DyeColor color : BlocksColorAPI.SORTED_COLORS) {
            String name = "ceiling_banner" + "_" + color.getName();
            map.put(color, regBlock(name, p -> new CeilingBannerBlock(color, p),
                    () -> BlockBehaviour.Properties.of()
                            .ignitedByLava()
                            .forceSolidOn()
                            .mapColor(color.getMapColor())
                            .strength(1.0F)
                            .noCollision()
                            .sound(SoundType.WOOD)
            ));
        }
        return Collections.unmodifiableMap(map);
    });

    public static final Supplier<BlockEntityType<CeilingBannerBlockTile>> CEILING_BANNER_TILE = regTile(
            CEILING_BANNER_NAME,
            () -> PlatHelper.newBlockEntityType(
                    CeilingBannerBlockTile::new, CEILING_BANNERS.values().stream().map(Supplier::get).toArray(Block[]::new)));


    //carpeted blocks
    public static final Supplier<Block> CARPET_STAIRS = regBlock(CARPETED_STAIR_NAME,
            p -> new CarpetStairBlock(Blocks.OAK_STAIRS, p), () -> BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_STAIRS)
    );

    public static final Supplier<Block> CARPET_SLAB = regBlock(CARPETED_SLAB_NAME,
            CarpetSlabBlock::new, () -> BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_SLAB)
    );

    public static final Supplier<BlockEntityType<CarpetedBlockTile>> CARPET_STAIRS_TILE = regTile("carpeted_block",
            () -> PlatHelper.newBlockEntityType(CarpetedBlockTile::new, CARPET_STAIRS.get(), CARPET_SLAB.get())
    );


    public static final Map<LanternRegistry.LanternType, WallLanternBlock> WALL_LANTERNS = new LinkedHashMap<>();
    public static final Map<Identifier, WallLanternBlock> WALL_LANTERNS_BY_LANTERN = new HashMap<>();
    //dynamic blocks are registered before block entity types so the map is filled here. vanilla rejects an empty block list
    public static Supplier<BlockEntityType<WallLanternBlockTile>> WALL_LANTERN_TILE = RegHelper.registerBlockEntityType(res(WALL_LANTERN_NAME), () ->
            PlatHelper.newBlockEntityType(WallLanternBlockTile::new, WALL_LANTERNS.values().toArray(new Block[0])));

    private static void registerWallLanterns(Registrator<Block> event) {
        for (LanternRegistry.LanternType type : LanternRegistry.INSTANCE) {
            Identifier id = res(type.getVariantId("wall"));
            var p = BlockBehaviour.Properties.ofFullCopy(type.lantern)
                    .pushReaction(PushReaction.DESTROY)
                    .noLootTable()
                    .setId(ResourceKey.create(Registries.BLOCK, id));
            WallLanternBlock block = makeWallLantern(p, type);
            type.addChild("wall_lantern", block);
            event.register(id, block);
            WALL_LANTERNS.put(type, block);
            WALL_LANTERNS_BY_LANTERN.put(type.getId(), block);
        }
    }

    private static WallLanternBlock makeWallLantern(BlockBehaviour.Properties p, LanternRegistry.LanternType type) {
        if (type.lantern instanceof WeatheringCopper) {
            return new WeatheringWallLanternBlock(p, type);
        }
        if (type.getId().getPath().equals("redstone_lantern")) {
            return new RedstoneWallLanternBlock(p, type);
        }
        return new WallLanternBlock(p, type);
    }

    //backward compat
    public static Supplier<WallLanternBlock> WALL_LANTERN = () -> WALL_LANTERNS.get(LanternRegistry.VANILLA);

    public static final Supplier<EntityType<FallingLanternEntity>> FALLING_LANTERN = regEntity(FALLING_LANTERN_NAME,
            EntityType.Builder.<FallingLanternEntity>of(FallingLanternEntity::new, MobCategory.MISC)
                    .sized(0.98F, 0.98F)
                    .clientTrackingRange(10)
                    .updateInterval(20));

    //tool hook
    public static final Supplier<ToolHookBlock> TOOL_HOOK = regBlock(TOOL_HOOK_NAME, ToolHookBlock::new,
            () -> BlockBehaviour.Properties.ofFullCopy(Blocks.TRIPWIRE_HOOK)
                    .overrideLootTable(Blocks.TRIPWIRE_HOOK.getLootTable()));

    public static final Supplier<BlockEntityType<ToolHookBlockTile>> TOOL_HOOK_TILE = regTile(
            TOOL_HOOK_NAME, () -> PlatHelper.newBlockEntityType(
                    ToolHookBlockTile::new, TOOL_HOOK.get()));


    //stackable skulls
    public static final Supplier<Block> SKULL_PILE = regBlock(SKULL_PILE_NAME, DoubleSkullBlock::new,
            () -> BlockBehaviour.Properties.ofFullCopy(Blocks.SKELETON_SKULL).sound(SoundType.BONE_BLOCK));

    public static final Supplier<BlockEntityType<DoubleSkullBlockTile>> SKULL_PILE_TILE = regTile(
            SKULL_PILE_NAME, () -> PlatHelper.newBlockEntityType(
                    DoubleSkullBlockTile::new, SKULL_PILE.get()));

    //skulls candles
    public static final Supplier<Block> SKULL_CANDLE = regBlock(SKULL_CANDLE_NAME, FloorCandleSkullBlock::new,
            () -> BlockBehaviour.Properties.ofFullCopy(Blocks.SKELETON_SKULL).sound(SoundType.BONE_BLOCK));

    public static final Supplier<Block> SKULL_CANDLE_WALL = regBlock(SKULL_CANDLE_NAME + "_wall", WallCandleSkullBlock::new,
            () -> BlockBehaviour.Properties.ofFullCopy(SKULL_CANDLE.get()));


    //needed for tag so it can repel piglins
    public static final Supplier<Block> SKULL_CANDLE_SOUL = regBlock(SKULL_CANDLE_SOUL_NAME, FloorCandleSkullBlock::new,
            () -> BlockBehaviour.Properties.ofFullCopy(SKULL_CANDLE.get()));

    public static final Supplier<Block> SKULL_CANDLE_SOUL_WALL = regBlock(SKULL_CANDLE_SOUL_NAME + "_wall", WallCandleSkullBlock::new,
            () -> BlockBehaviour.Properties.ofFullCopy(SKULL_CANDLE.get()));


    public static final Supplier<BlockEntityType<CandleSkullBlockTile>> SKULL_CANDLE_TILE = regTile(
            SKULL_CANDLE_NAME, () -> PlatHelper.newBlockEntityType(
                    CandleSkullBlockTile::new, SKULL_CANDLE.get(), SKULL_CANDLE_WALL.get(),
                    SKULL_CANDLE_SOUL.get(), SKULL_CANDLE_SOUL_WALL.get()));

    //directional cake
    public static final Supplier<Block> DIRECTIONAL_CAKE = regBlock(DIRECTIONAL_CAKE_NAME,
            p -> new DirectionalCakeBlock(p, CakeRegistry.VANILLA),
            () -> Utils.copyPropertySafe(CakeRegistry.VANILLA.cake)
                    .overrideLootTable(CakeRegistry.VANILLA.cake.getLootTable()));

    public static final Map<CakeRegistry.CakeType, DoubleCakeBlock> DOUBLE_CAKES = new LinkedHashMap<>();

    private static void registerDoubleCakes(Registrator<Block> event) {
        for (CakeRegistry.CakeType type : CakeRegistry.INSTANCE) {

            Identifier id = res(type.getVariantId("double"));
            var p = Utils.copyPropertySafe(type.cake)
                    .overrideLootTable(type.cake.getLootTable())
                    .setId(ResourceKey.create(Registries.BLOCK, id));
            DoubleCakeBlock block = new DoubleCakeBlock(p, type);
            type.addChild("double_cake", block);
            event.register(id, block);
            ModRegistry.DOUBLE_CAKES.put(type, block);
        }
    }

    public static <T extends BlockEntityType<E>, E extends
            BlockEntity> Supplier<T> regTile(String name, Supplier<T> sup) {
        return RegHelper.registerBlockEntityType(res(name), sup);
    }

    public static <T extends Block> RegSupplier<T> regBlock(String name, Function<BlockBehaviour.Properties, T> factory,
                                                            Supplier<BlockBehaviour.Properties> properties) {
        return RegHelper.registerBlock(res(name), factory, properties);
    }

    public static <T extends Item> RegSupplier<T> regItem(String name, Function<Item.Properties, T> factory,
                                                          Item.Properties properties) {
        return RegHelper.registerItem(res(name), factory, properties);
    }

    public static <T extends Entity> Supplier<EntityType<T>> regEntity(String name, EntityType.Builder<T> builder) {
        return RegHelper.registerEntityType(res(name), builder);
    }

}
