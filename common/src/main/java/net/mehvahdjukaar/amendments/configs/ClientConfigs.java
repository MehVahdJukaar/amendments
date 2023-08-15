package net.mehvahdjukaar.amendments.configs;

import net.mehvahdjukaar.amendments.Amendments;
import net.mehvahdjukaar.amendments.client.ModMaterials;
import net.mehvahdjukaar.amendments.common.PendulumAnimation;
import net.mehvahdjukaar.moonlight.api.platform.configs.ConfigBuilder;
import net.mehvahdjukaar.moonlight.api.platform.configs.ConfigType;

import java.util.function.Supplier;

public class ClientConfigs {

    public static void init() {
    }

    public static final Supplier<Boolean> PLACEABLE_TOOLTIP;

    public static final Supplier<Double> LILY_OFFSET;
    public static final Supplier<Boolean> BELL_CONNECTION;
    public static final Supplier<Boolean> COLORED_BREWING_STAND;
    public static final Supplier<Boolean> SWINGING_SIGNS;
    public static final Supplier<Boolean> SIGN_ATTACHMENT;
    public static final Supplier<PendulumAnimation.Config> HANGING_SIGN_CONFIG;

    public static final Supplier<Boolean> FAST_LANTERNS;
    public static final Supplier<PendulumAnimation.Config> WALL_LANTERN_CONFIG;


    private static final Supplier<Boolean> COLORED_ARROWS;

    static {
        ConfigBuilder builder = ConfigBuilder.create(Amendments.MOD_ID, ConfigType.CLIENT);

        builder.push("general");

        PLACEABLE_TOOLTIP = builder.define("tooltips", true);
        builder.pop();

        builder.push("lily_pad");
        LILY_OFFSET = builder.comment("set to 0 tho have lilypads at the same exact position as vanilla." +
                        "negative numbers will place them in their own blockspace right below avoiding any clipping." +
                        "best of both worlds at default as its barely within its space")
                .define("y_offset", -0.25 / 16f - 0.001, -1, 1);
        builder.pop();

        builder.push("bell");
        BELL_CONNECTION = builder.comment("Visually attach chains and ropes to bells")
                .define("chain_attachment", true);
        builder.pop();

        builder.push("brewing_stand");
        COLORED_BREWING_STAND = builder.comment("Colors the brewing stand potion texture depending on the potions it's brewing.\n" +
                        "If using a resource pack add tint index from 0 to 3 to the 3 potion layers")
                .define("brewing_stand_colors", true);
        builder.pop();

        builder.push("arrows");
        //Keep?
        COLORED_ARROWS = builder.comment("Makes tipped arrows show their colors when loaded with a crossbow")
                .define("crossbows_colors", true);
        builder.pop();

        builder.push("hanging_sign");
        SWINGING_SIGNS = builder.comment("Makes siwng swing!")
                .define("swinging_signs", true);
        SIGN_ATTACHMENT = builder.comment("Signs have visual attachment to walls and fences")
                .define("sign_attachment", true);
        HANGING_SIGN_CONFIG = builder.defineObject("swing_physics",
                PendulumAnimation.Config::new,
                PendulumAnimation.Config.CODEC);
        builder.pop();

        builder.push("lantern");
        FAST_LANTERNS = builder.comment("Makes wall lantern use a simple block model instead of the animated tile entity renderer. This will make them render much faster but will also remove the animation" +
                        "Note that this option only affect lanterns close by as the one far away render as fast by default")
                .define("fast_lanterns", false);

        WALL_LANTERN_CONFIG = builder.defineObject("swing_physics",
                PendulumAnimation.Config::new,
                PendulumAnimation.Config.CODEC);
        builder.pop();

        builder.buildAndRegister();
    }


}
