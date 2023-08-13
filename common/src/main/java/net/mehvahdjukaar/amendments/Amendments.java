package net.mehvahdjukaar.amendments;

import net.mehvahdjukaar.amendments.common.FlowerPotHandler;
import net.mehvahdjukaar.amendments.configs.ClientConfigs;
import net.mehvahdjukaar.amendments.reg.ModEvents;
import net.mehvahdjukaar.amendments.reg.ModRegistry;
import net.mehvahdjukaar.moonlight.api.platform.PlatHelper;
import net.minecraft.resources.ResourceLocation;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


public class Amendments {
    public static final String MOD_ID = "amendments";
    public static final Logger LOGGER = LogManager.getLogger("Amendments");

    public static ResourceLocation res(String name) {
        return new ResourceLocation(MOD_ID, name);
    }

    public static void init(){
        ModRegistry.init();
        ModEvents.init();

        if(PlatHelper.getPhysicalSide().isClient()) {
            ClientConfigs.init();
            AmendmentsClient.init();
        }
        PlatHelper.addCommonSetupAsync(Amendments::setupA);
        PlatHelper.addCommonSetup(Amendments::setup);
    }

    private static void setup() {
        ModEvents.setup();
    }

    private static void setupA() {
        FlowerPotHandler.setup();
    }

}
