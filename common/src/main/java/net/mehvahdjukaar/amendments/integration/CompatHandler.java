package net.mehvahdjukaar.amendments.integration;

import net.mehvahdjukaar.amendments.Amendments;
import net.mehvahdjukaar.moonlight.api.platform.PlatHelper;
import net.mehvahdjukaar.supplementaries.Supplementaries;

public class CompatHandler {

    public static final boolean FARMERS_DELIGHT;
    public static final boolean SUPPLEMENTARIES = PlatHelper.isModLoaded("supplementaries");
    public static final boolean SUPPSQUARED = PlatHelper.isModLoaded("suppsquared");
    public static final boolean QUARK = PlatHelper.isModLoaded("quark");
    public static final boolean TORCHSLAB = PlatHelper.isModLoaded("torch_slab");
    public static final boolean BUZZIER_BEES = PlatHelper.isModLoaded("buzzier_bees");
    public static final boolean SHIMMER = PlatHelper.isModLoaded("shimmer");
    public static final boolean CAVE_ENHANCEMENTS = PlatHelper.isModLoaded("cave_enhancements");
    public static final boolean FLAN = PlatHelper.isModLoaded("flan");
    public static final boolean BLUEPRINT = PlatHelper.isModLoaded("blueprint");
    public static final boolean CONFIGURED = PlatHelper.isModLoaded("configured");
    public static final boolean ALEX_CAVES = PlatHelper.isModLoaded("alexscaves");
    public static final boolean RATS = PlatHelper.isModLoaded("rats");
    public static final boolean THIN_AIR = PlatHelper.isModLoaded("thin_air");

    static {
        boolean fd = false;
        if (PlatHelper.isModLoaded("farmersdelight")) {
            try {
                Class.forName("vectorwing.farmersdelight.FarmersDelight");
                fd = true;
            } catch (Exception e) {
                Amendments.LOGGER.error("Farmers Delight Refabricated is not installed. Disabling Farmers Delight Module");
            }
        }
        FARMERS_DELIGHT = fd;
    }
}

