package net.mehvahdjukaar.amendments.reg;

import com.google.common.base.Preconditions;
import it.unimi.dsi.fastutil.objects.Object2ObjectLinkedOpenHashMap;
import net.mehvahdjukaar.amendments.common.CakeRegistry;
import net.mehvahdjukaar.amendments.common.LecternEditMenu;
import net.mehvahdjukaar.amendments.common.block.*;
import net.mehvahdjukaar.amendments.common.entity.FallingLanternEntity;
import net.mehvahdjukaar.amendments.common.item.DyeBottleItem;
import net.mehvahdjukaar.amendments.common.item.placement.WallLanternPlacement;
import net.mehvahdjukaar.amendments.common.recipe.DyeBottleRecipe;
import net.mehvahdjukaar.amendments.common.tile.*;
import net.mehvahdjukaar.amendments.configs.CommonConfigs;
import net.mehvahdjukaar.amendments.integration.CompatHandler;
import net.mehvahdjukaar.amendments.integration.CompatObjects;
import net.mehvahdjukaar.moonlight.api.fluids.SoftFluid;
import net.mehvahdjukaar.moonlight.api.fluids.SoftFluidRegistry;
import net.mehvahdjukaar.moonlight.api.item.additional_placements.AdditionalItemPlacementsAPI;
import net.mehvahdjukaar.moonlight.api.misc.DataObjectReference;
import net.mehvahdjukaar.moonlight.api.misc.RegSupplier;
import net.mehvahdjukaar.moonlight.api.misc.Registrator;
import net.mehvahdjukaar.moonlight.api.platform.PlatHelper;
import net.mehvahdjukaar.moonlight.api.platform.RegHelper;
import net.mehvahdjukaar.moonlight.api.set.BlockSetAPI;
import net.mehvahdjukaar.moonlight.api.set.BlocksColorAPI;
import net.minecraft.Util;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.BlockItem;
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
import org.violetmoon.quark.content.building.block.ThatchBlock;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Supplier;

import static net.mehvahdjukaar.amendments.Amendments.res;
import static net.mehvahdjukaar.amendments.reg.ModConstants.*;
import static net.mehvahdjukaar.supplementaries.reg.ModConstants.GUNPOWDER_BLOCK_NAME;
import static net.mehvahdjukaar.supplementaries.reg.RegUtils.regBlock;

public class ModRegistry {


    public static void init() {
        BlockSetAPI.registerBlockSetDefinition(CakeRegistry.INSTANCE);
        BlockSetAPI.addDynamicBlockRegistration(ModRegistry::registerDoubleCakes, CakeRegistry.CakeType.class);
        AdditionalItemPlacementsAPI.addRegistration(ModRegistry::registerAdditionalPlacements);
    }

    public static final Supplier<Block> GUNPOWDER_BLOCK = RegHelper.registerBlockWithItem (
            ResourceLocation.tryParse("supplementaries:gunpowder"),
             () -> new GunpowderBlock(
            BlockBehaviour.Properties.ofFullCopy(Blocks.REDSTONE_WIRE).sound(SoundType.SAND)));

    public static final Supplier<Block> THATCH = RegHelper.registerBlockWithItem(
            ResourceLocation.tryParse("goated:thatch_slab"),
            () -> new SlabBlock(BlockBehaviour.Properties.of()
                    .strength(0.5F)
                    .sound(SoundType.GRASS)
                    .noOcclusion()));

    public static void registerAdditionalPlacements(AdditionalItemPlacementsAPI.Event event) {
        // this is specifically for things that place a new block in air. Stuff that modifiers blocks is in events.
        // reason is more complicated than this
        var wallLanternPlacement = new WallLanternPlacement();
        for (var i : BuiltInRegistries.ITEM) {
            if (i instanceof BlockItem bi) {
                Block block = bi.getBlock();
                if (CommonConfigs.WALL_LANTERN.get() && WallLanternBlock.isValidBlock(block)) {
                    event.register(i, wallLanternPlacement);
                }
            }
        }
        if (CommonConfigs.HANGING_POT.get()) {
            event.registerSimple(Items.FLOWER_POT, HANGING_FLOWER_POT.get());
        }
        if (CommonConfigs.CEILING_BANNERS.get()) {
            for (var e : CEILING_BANNERS.entrySet()) {
                event.registerSimple(Preconditions.checkNotNull(BannerBlock.byColor(e.getKey()).asItem()),
                        e.getValue().get());
            }
        }
    }

    public static final DataObjectReference<DamageType> BOILING_DAMAGE = new DataObjectReference<>(
            res("boiling"), Registries.DAMAGE_TYPE);


    public static final DataObjectReference<SoftFluid> DYE_SOFT_FLUID = new DataObjectReference<>(res("dye"),
            SoftFluidRegistry.KEY);

    public static final RegSupplier<RecipeSerializer<DyeBottleRecipe>> DYE_BOTTLE_RECIPE = RegHelper.registerSpecialRecipe(
            res("dye_bottle"), DyeBottleRecipe::new);

    public static final Supplier<MenuType<LecternEditMenu>> LECTERN_EDIT_MENU = RegHelper.registerMenuType(
            res("lectern_edit"), LecternEditMenu::new
    );

    public static final RegSupplier<SimpleParticleType> BOILING_PARTICLE = RegHelper.registerParticle(res("boiling_bubble"));
    public static final RegSupplier<SimpleParticleType> SPLASH_PARTICLE = RegHelper.registerParticle(res("fluid_splash"));

    /* todo
    private static final Supplier<RecipeType<CauldronRecipe>> CAULDRON_RECIPE = RegHelper.registerRecipeType(
            res("cauldron_recipe"));

    private static final Supplier<RecipeSerializer<CauldronRecipe>> CAULDRON_RECIPE_SERIALIZER = RegHelper.registerSpecialRecipe(
            res("cauldron_recipe"), CauldronRecipe::new);*/

    public static final Supplier<Item> DYE_BOTTLE_ITEM = regItem(DYE_BOTTLE_NAME,
            () -> new DyeBottleItem(new Item.Properties()
                    .component(DataComponents.DYED_COLOR, DyeBottleItem.RED_COLOR)
                    .stacksTo(1)
                    .craftRemainder(Items.GLASS_BOTTLE)));

    //lilypad
    public static final Supplier<Block> WATERLILY_BLOCK = regBlock(WATER_LILY_NAME,
            () -> new WaterloggedLilyBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.LILY_PAD).instabreak()
                    .sound(SoundType.LILY_PAD).noOcclusion())
    );

    public static final Supplier<BlockEntityType<WaterloggedLilyBlockTile>> WATERLILY_TILE = regTile(WATER_LILY_NAME,
            () -> PlatHelper.newBlockEntityType(WaterloggedLilyBlockTile::new, WATERLILY_BLOCK.get())
    );

    //cauldron
    public static final Supplier<LiquidCauldronBlock> LIQUID_CAULDRON = regBlock(LIQUID_CAULDRON_NAME,
            () -> new LiquidCauldronBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.CAULDRON))
    );
    public static final Supplier<Block> DYE_CAULDRON = regBlock(DYE_CAULDRON_NAME,
            () -> new DyeCauldronBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.CAULDRON))
    );

    public static final Supplier<BlockEntityType<LiquidCauldronBlockTile>> LIQUID_CAULDRON_TILE = regTile(LIQUID_CAULDRON_NAME,
            () -> PlatHelper.newBlockEntityType(LiquidCauldronBlockTile::new, LIQUID_CAULDRON.get(), DYE_CAULDRON.get())
    );

    //hanging flower pot
    public static final Supplier<Block> HANGING_FLOWER_POT = regBlock(HANGING_FLOWER_POT_NAME,
            () -> new HangingFlowerPotBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.FLOWER_POT)));

    public static final Supplier<BlockEntityType<HangingFlowerPotBlockTile>> HANGING_FLOWER_POT_TILE = regTile(
            HANGING_FLOWER_POT_NAME, () -> PlatHelper.newBlockEntityType(
                    HangingFlowerPotBlockTile::new, HANGING_FLOWER_POT.get()));


    //ceiling banners
    public static final Map<DyeColor, Supplier<Block>> CEILING_BANNERS = Util.make(() -> {
        Map<DyeColor, Supplier<Block>> map = new Object2ObjectLinkedOpenHashMap<>();
        for (DyeColor color : BlocksColorAPI.SORTED_COLORS) {
            String name = "ceiling_banner" + "_" + color.getName();
            map.put(color, regBlock(name, () -> new CeilingBannerBlock(color,
                            BlockBehaviour.Properties.of()
                                    .ignitedByLava()
                                    .forceSolidOn()
                                    .mapColor(color.getMapColor())
                                    .strength(1.0F)
                                    .noCollission()
                                    .sound(SoundType.WOOD)
                    )
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
            () -> new CarpetStairBlock(Blocks.OAK_STAIRS, BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_STAIRS))
    );

    public static final Supplier<Block> CARPET_SLAB = regBlock(CARPETED_SLAB_NAME,
            () -> new CarpetSlabBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_SLAB))
    );

    public static final Supplier<BlockEntityType<CarpetedBlockTile>> CARPET_STAIRS_TILE = regTile("carpeted_block",
            () -> PlatHelper.newBlockEntityType(CarpetedBlockTile::new, CARPET_STAIRS.get(), CARPET_SLAB.get())
    );


    //wall lantern
    public static final Supplier<WallLanternBlock> WALL_LANTERN = regBlock(WALL_LANTERN_NAME, () -> {
        var p = BlockBehaviour.Properties.ofFullCopy(Blocks.LANTERN)
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

    //tool hook
    public static final Supplier<ToolHookBlock> TOOL_HOOK = regBlock(TOOL_HOOK_NAME, () -> {
        var p = BlockBehaviour.Properties.ofFullCopy(Blocks.TRIPWIRE_HOOK).dropsLike(Blocks.TRIPWIRE_HOOK);
        return new ToolHookBlock(p);
    });

    public static final Supplier<BlockEntityType<ToolHookBlockTile>> TOOL_HOOK_TILE = regTile(
            TOOL_HOOK_NAME, () -> PlatHelper.newBlockEntityType(
                    ToolHookBlockTile::new, TOOL_HOOK.get()));


    //stackable skulls
    public static final Supplier<Block> SKULL_PILE = regBlock(SKULL_PILE_NAME, () -> {
        var p = BlockBehaviour.Properties.ofFullCopy(Blocks.SKELETON_SKULL).sound(SoundType.BONE_BLOCK);

        return new DoubleSkullBlock(p);
    });

    public static final Supplier<BlockEntityType<DoubleSkullBlockTile>> SKULL_PILE_TILE = regTile(
            SKULL_PILE_NAME, () -> PlatHelper.newBlockEntityType(
                    DoubleSkullBlockTile::new, SKULL_PILE.get()));

    //skulls candles
    public static final Supplier<Block> SKULL_CANDLE = regBlock(SKULL_CANDLE_NAME, () ->
            new FloorCandleSkullBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.SKELETON_SKULL).sound(SoundType.BONE_BLOCK)));

    public static final Supplier<Block> SKULL_CANDLE_WALL = regBlock(SKULL_CANDLE_NAME + "_wall", () ->
            new WallCandleSkullBlock(BlockBehaviour.Properties.ofFullCopy(SKULL_CANDLE.get())));


    //needed for tag so it can repel piglins
    public static final Supplier<Block> SKULL_CANDLE_SOUL = regBlock(SKULL_CANDLE_SOUL_NAME, () ->
            new FloorCandleSkullBlock(BlockBehaviour.Properties.ofFullCopy(SKULL_CANDLE.get()),
                    CompatHandler.BUZZIER_BEES ? CompatObjects.SMALL_SOUL_FLAME : () -> ParticleTypes.SOUL_FIRE_FLAME));

    public static final Supplier<Block> SKULL_CANDLE_SOUL_WALL = regBlock(SKULL_CANDLE_SOUL_NAME + "_wall", () ->
            new WallCandleSkullBlock(BlockBehaviour.Properties.ofFullCopy(SKULL_CANDLE.get()),
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

    private static void registerDoubleCakes
            (Registrator<Block> event, Collection<CakeRegistry.CakeType> cakeTypes) {
        for (CakeRegistry.CakeType type : cakeTypes) {

            ResourceLocation id = res(type.getVariantId("double"));
            DoubleCakeBlock block = new DoubleCakeBlock(type);
            type.addChild("double_cake", block);
            event.register(id, block);
            ModRegistry.DOUBLE_CAKES.put(type, block);
        }
    }

    public static <T extends BlockEntityType<E>, E extends
            BlockEntity> Supplier<T> regTile(String name, Supplier<T> sup) {
        return RegHelper.registerBlockEntityType(res(name), sup);
    }

    public static <T extends Block> RegSupplier<T> regBlock(String name, Supplier<T> sup) {
        return RegHelper.registerBlock(res(name), sup);
    }

    public static <T extends Item> RegSupplier<T> regItem(String name, Supplier<T> sup) {
        return RegHelper.registerItem(res(name), sup);
    }

    public static <T extends
            Entity> Supplier<EntityType<T>> regEntity(String name, Supplier<EntityType.Builder<T>> builder) {
        return RegHelper.registerEntityType(res(name), () -> builder.get().build(name));
    }

}
