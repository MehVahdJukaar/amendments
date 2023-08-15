package net.mehvahdjukaar.amendments.reg;

import it.unimi.dsi.fastutil.objects.Object2ObjectLinkedOpenHashMap;
import net.mehvahdjukaar.amendments.common.CakeRegistry;
import net.mehvahdjukaar.amendments.common.block.*;
import net.mehvahdjukaar.amendments.common.entity.FallingLanternEntity;
import net.mehvahdjukaar.amendments.common.tile.*;
import net.mehvahdjukaar.amendments.configs.CommonConfigs;
import net.mehvahdjukaar.amendments.integration.CompatHandler;
import net.mehvahdjukaar.amendments.integration.CompatObjects;
import net.mehvahdjukaar.moonlight.api.item.additional_placements.AdditionalItemPlacementsAPI;
import net.mehvahdjukaar.moonlight.api.misc.RegSupplier;
import net.mehvahdjukaar.moonlight.api.misc.Registrator;
import net.mehvahdjukaar.moonlight.api.platform.PlatHelper;
import net.mehvahdjukaar.moonlight.api.platform.RegHelper;
import net.mehvahdjukaar.moonlight.api.set.BlockSetAPI;
import net.mehvahdjukaar.moonlight.api.set.BlocksColorAPI;
import net.minecraft.Util;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.PushReaction;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Supplier;

import static net.mehvahdjukaar.amendments.Amendments.res;
import static net.mehvahdjukaar.amendments.reg.ModConstants.*;

public class ModRegistry {

    public static void init() {
        BlockSetAPI.registerBlockSetDefinition(CakeRegistry.INSTANCE);
        BlockSetAPI.addDynamicBlockRegistration(ModRegistry::registerDoubleCakes, CakeRegistry.CakeType.class);

    }


    //lilypad
    public static final Supplier<Block> WATERLILY_BLOCK = regBlock(WATER_LILY_NAME,
            () -> new WaterloggedLilyBlock(BlockBehaviour.Properties.copy(Blocks.LILY_PAD).instabreak()
                    .sound(SoundType.LILY_PAD).noOcclusion())
    );

    public static final Supplier<BlockEntityType<WaterloggedLilyBlockTile>> WATERLILY_TILE = regTile(WATER_LILY_NAME,
            () -> PlatHelper.newBlockEntityType(WaterloggedLilyBlockTile::new, WATERLILY_BLOCK.get())
    );

    //cauldron
    public static final Supplier<Block> LIQUID_CAULDRON = regBlock(LIQUID_CAULDRON_NAME,
            () -> new LiquidCauldronBlock(BlockBehaviour.Properties.copy(Blocks.CAULDRON))
    );

    public static final Supplier<BlockEntityType<LiquidCauldronBlockTile>> LIQUID_CAULDRON_TILE = regTile(LIQUID_CAULDRON_NAME,
            () -> PlatHelper.newBlockEntityType(LiquidCauldronBlockTile::new, LIQUID_CAULDRON.get())
    );

    //hanging flower pot
    public static final Supplier<Block> HANGING_FLOWER_POT = regPlaceableItem(HANGING_FLOWER_POT_NAME,
            () -> new HangingFlowerPotBlock(BlockBehaviour.Properties.copy(Blocks.FLOWER_POT)),
            () -> Items.FLOWER_POT, CommonConfigs.HANGING_POT);

    public static final Supplier<BlockEntityType<HangingFlowerPotBlockTile>> HANGING_FLOWER_POT_TILE = regTile(
            HANGING_FLOWER_POT_NAME, () -> PlatHelper.newBlockEntityType(
                    HangingFlowerPotBlockTile::new, HANGING_FLOWER_POT.get()));


    //ceiling banners
    public static final Map<DyeColor, Supplier<Block>> CEILING_BANNERS = Util.make(() -> {
        Map<DyeColor, Supplier<Block>> map = new Object2ObjectLinkedOpenHashMap<>();
        for (DyeColor color : BlocksColorAPI.SORTED_COLORS) {
            String name = "ceiling_banner" + "_" + color.getName();
            map.put(color, regPlaceableItem(name, () -> new CeilingBannerBlock(color,
                            BlockBehaviour.Properties.of()
                                    .ignitedByLava()
                                    .forceSolidOn()
                                    .mapColor(color.getMapColor())
                                    .strength(1.0F)
                                    .noCollission()
                                    .sound(SoundType.WOOD)
                    ), color.getName() + "_banner", CommonConfigs.CEILING_BANNERS
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
            () -> new CarpetStairBlock(Blocks.OAK_STAIRS)
    );

    public static final Supplier<Block> CARPET_SLAB = regBlock(CARPETED_SLAB_NAME,
            () -> new CarpetSlabBlock(Blocks.OAK_SLAB)
    );

    public static final Supplier<BlockEntityType<CarpetedBlockTile>> CARPET_STAIRS_TILE = regTile("carpeted_block",
            () -> PlatHelper.newBlockEntityType(CarpetedBlockTile::new, CARPET_STAIRS.get(), CARPET_SLAB.get())
    );


    //wall lantern
    public static final Supplier<WallLanternBlock> WALL_LANTERN = regBlock(WALL_LANTERN_NAME, () -> {
        var p = BlockBehaviour.Properties.copy(Blocks.LANTERN)
                .pushReaction(PushReaction.DESTROY)
                .lightLevel((state) -> 15)
                .noLootTable();
        return new WallLanternBlock(p);
    });

    public static final Supplier<BlockEntityType<WallLanternBlockTile>> WALL_LANTERN_TILE = regTile(
            WALL_LANTERN_NAME, () -> PlatHelper.newBlockEntityType(
                    WallLanternBlockTile::new, WALL_LANTERN.get()));

    public static final Supplier<EntityType<FallingLanternEntity>> FALLING_LANTERN = regEntity(FALLING_LANTERN_NAME, () ->
            EntityType.Builder.<FallingLanternEntity>of(FallingLanternEntity::new, MobCategory.MISC)
                    .sized(0.98F, 0.98F)
                    .clientTrackingRange(10)
                    .updateInterval(20));

    //stackable skulls
    public static final Supplier<Block> SKULL_PILE = regBlock(SKULL_PILE_NAME, () -> {
        var p = BlockBehaviour.Properties.copy(Blocks.SKELETON_SKULL).sound(SoundType.BONE_BLOCK);

        return new DoubleSkullBlock(p);
    });

    public static final Supplier<BlockEntityType<DoubleSkullBlockTile>> SKULL_PILE_TILE = regTile(
            SKULL_PILE_NAME, () -> PlatHelper.newBlockEntityType(
                    DoubleSkullBlockTile::new, SKULL_PILE.get()));

    //skulls candles
    public static final Supplier<Block> SKULL_CANDLE = regBlock(SKULL_CANDLE_NAME, () ->
            new FloorCandleSkullBlock(BlockBehaviour.Properties.copy(Blocks.SKELETON_SKULL).sound(SoundType.BONE_BLOCK)));

    public static final Supplier<Block> SKULL_CANDLE_WALL = regBlock(SKULL_CANDLE_NAME + "_wall", () ->
            new WallCandleSkullBlock(BlockBehaviour.Properties.copy(SKULL_CANDLE.get())));


    //needed for tag so it can repel piglins
    public static final Supplier<Block> SKULL_CANDLE_SOUL = regBlock(SKULL_CANDLE_SOUL_NAME, () ->
            new FloorCandleSkullBlock(BlockBehaviour.Properties.copy(SKULL_CANDLE.get()),
                    CompatHandler.BUZZIER_BEES ? CompatObjects.SMALL_SOUL_FLAME : () -> ParticleTypes.SOUL_FIRE_FLAME));

    public static final Supplier<Block> SKULL_CANDLE_SOUL_WALL = regBlock(SKULL_CANDLE_SOUL_NAME + "_wall", () ->
            new WallCandleSkullBlock(BlockBehaviour.Properties.copy(SKULL_CANDLE.get()),
                    CompatHandler.BUZZIER_BEES ? CompatObjects.SMALL_SOUL_FLAME : () -> ParticleTypes.SOUL_FIRE_FLAME));


    public static final Supplier<BlockEntityType<CandleSkullBlockTile>> SKULL_CANDLE_TILE = regTile(
            SKULL_CANDLE_NAME, () -> PlatHelper.newBlockEntityType(
                    CandleSkullBlockTile::new, SKULL_CANDLE.get(), SKULL_CANDLE_WALL.get(),
                    SKULL_CANDLE_SOUL.get(), SKULL_CANDLE_SOUL_WALL.get()));

    //directional cake
    public static final Supplier<Block> DIRECTIONAL_CAKE = regBlock(DIRECTIONAL_CAKE_NAME, () -> new DirectionalCakeBlock(
            CakeRegistry.VANILLA
    ));

    public static final Map<CakeRegistry.CakeType, DoubleCakeBlock> DOUBLE_CAKES = new LinkedHashMap<>();

    private static void registerDoubleCakes(Registrator<Block> event, Collection<CakeRegistry.CakeType> cakeTypes) {
        for (CakeRegistry.CakeType type : cakeTypes) {

            ResourceLocation id = res(type.getVariantId("double"));
            DoubleCakeBlock block = new DoubleCakeBlock(type);
            type.addChild("double_cake", block);
            event.register(id, block);
            ModRegistry.DOUBLE_CAKES.put(type, block);
        }
    }


    public static <T extends Block> RegSupplier<T> regPlaceableItem(
            String name, Supplier<T> sup,
            String itemLocation, Supplier<Boolean> config) {
        Supplier<Item> itemSupp = () -> BuiltInRegistries.ITEM.get(new ResourceLocation(itemLocation));
        return regPlaceableItem(name, sup, itemSupp, config);
    }

    public static <T extends Block> RegSupplier<T> regPlaceableItem(
            String name, Supplier<T> blockFactory,
            Supplier<? extends Item> itemSupplier, Supplier<Boolean> config) {
        var block = regBlock(name, blockFactory);
        if(config.get()) {
            AdditionalItemPlacementsAPI.registerSimple(block, itemSupplier);
        }
        return block;
    }


    public static <T extends BlockEntityType<E>, E extends BlockEntity> Supplier<T> regTile(String name, Supplier<T> sup) {
        return RegHelper.registerBlockEntityType(res(name), sup);
    }

    public static <T extends Block> RegSupplier<T> regBlock(String name, Supplier<T> sup) {
        return RegHelper.registerBlock(res(name), sup);
    }

    public static <T extends Entity> Supplier<EntityType<T>> regEntity(String name, Supplier<EntityType.Builder<T>> builder) {
        return RegHelper.registerEntityType(res(name), () -> builder.get().build(name));
    }

}
