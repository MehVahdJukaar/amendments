package net.mehvahdjukaar.amendments.configs;

import net.mehvahdjukaar.amendments.Amendments;
import net.mehvahdjukaar.amendments.common.entity.FallingLanternEntity;
import net.mehvahdjukaar.moonlight.api.platform.configs.ConfigBuilder;
import net.mehvahdjukaar.moonlight.api.platform.configs.ConfigSpec;
import net.mehvahdjukaar.moonlight.api.platform.configs.ConfigType;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

public class CommonConfigs {

    public static final Map<String, Supplier<Boolean>> FEATURE_TOGGLES = new HashMap<>();

    public static final Supplier<Boolean> DOUBLE_CAKES;
    public static final Supplier<Boolean> DIRECTIONAL_CAKE;

    public static final Supplier<Boolean> SKULL_PILES;
    public static final Supplier<Boolean> SKULL_CANDLES;
    public static final Supplier<Boolean> SKULL_CANDLES_MULTIPLE;

    public static final Supplier<Boolean> HANGING_POT;

    public static final Supplier<Boolean> WALL_LANTERN;
    public static final Supplier<Boolean> WALL_LANTERN_HIGH_PRIORITY;
    public static final Supplier<List<String>> WALL_LANTERN_BLACKLIST;
    public static final Supplier<FallingLanternEntity.FallMode> FALLING_LANTERNS;

    public static final Supplier<Boolean> CEILING_BANNERS;

    public static final Supplier<Boolean> BELL_CHAIN_RINGING;
    public static final Supplier<Integer> BELL_CHAIN_LENGTH;

    public static final Supplier<Boolean> SCARE_VILLAGERS;

    public static final ConfigSpec CONFIG;

    static {
        ConfigBuilder builder = ConfigBuilder.create(Amendments.MOD_ID, ConfigType.COMMON);

        builder.push("features");

        //double cake
        builder.push("cake");
        DOUBLE_CAKES = builder.comment("Allows you to place a cake on top of another")
                .define("double_cake", true);
        DIRECTIONAL_CAKE = builder.comment("Allows eating a cake from every side")
                .define("directional_cake", true);
        builder.pop();

        //skulls stuff
        builder.push("mob_head");
        SKULL_PILES = builder.comment("Allows you to place two mob heads on top of each other")
                .define("skull_piles", true);
        SKULL_CANDLES = builder.comment("Allows candles to be placed on top of skulls")
                .define("skull_candles", true);
        SKULL_CANDLES_MULTIPLE = builder.comment("Allows placing more than one candle ontop of each skull")
                .define("multiple_candles", true);
        builder.pop();

        //hanging pot
        builder.push("flower_pot");
        HANGING_POT = builder.comment("allows you to place hanging flower pots. Works with any modded pot too")
                .define("hanging_pot", true);
        builder.pop();

        //wall lantern
        builder.push("lantern");
        WALL_LANTERN = builder.comment("Allow wall lanterns placement")
                .define("enabled", true);

        WALL_LANTERN_HIGH_PRIORITY = builder.comment("Gives high priority to wall lantern placement. Enable to override other wall lanterns placements, disable if it causes issues with other mods that use lower priority block click events")
                .define("high_priority", true);

        List<String> modBlacklist = Arrays.asList("extlights", "betterendforge", "tconstruct", "enigmaticlegacy");
        WALL_LANTERN_BLACKLIST = builder.comment("Mod ids of mods that have lantern block that extend the base lantern class but don't look like one")
                .define("mod_blacklist", modBlacklist);
        FALLING_LANTERNS = builder.comment("Allows ceiling lanterns to fall if their support is broken." +
                        "Additionally if they fall from high enough they will break creating a fire where they land")
                .define("falling_lanterns", FallingLanternEntity.FallMode.ON);
        builder.pop();

        //keep this?. yeah keep this
        //bells
        builder.push("bell");
        BELL_CHAIN_RINGING = builder.comment("Ring a bell by clicking on a chain that's connected to it")
                .define("chain_ringing", true);
        BELL_CHAIN_LENGTH = builder.comment("Max chain length that allows a bell to ring")
                .define("chain_length", 16, 0, 256);
        builder.pop();

        builder.push("banners");
        CEILING_BANNERS = builder.comment("Allow banners to be placed on ceilings")
                .define("ceiling_banners", true);
        builder.pop();

        //keep this??
        builder.push("noteblocks_scare");
        SCARE_VILLAGERS = builder.comment("Noteblocks with a zombie head will scare off villagers")
                .define("enabled", true);
        builder.pop();


        builder.pop();


        CONFIG = builder.buildAndRegister();
        CONFIG.loadFromFile();
    }



    private static Supplier<Boolean> feature(ConfigBuilder builder) {
        return feature(builder, "enabled", builder.currentCategory(), true);
    }

    private static Supplier<Boolean> feature(ConfigBuilder builder, String name) {
        return feature(builder, name, name, true);
    }

    private static Supplier<Boolean> feature(ConfigBuilder builder, String name, String key, boolean value) {
        var config = builder.gameRestart().define(name, value);
        FEATURE_TOGGLES.put(key, config);
        return config;
    }

    public static void init(){

    }
}
