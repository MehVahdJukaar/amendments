package net.mehvahdjukaar.betterjukebox.fabric;

import net.fabricmc.api.ModInitializer;
import net.mehvahdjukaar.betterjukebox.BetterJukeboxes;
import net.mehvahdjukaar.betterjukebox.BetterJukeboxesClient;
import net.mehvahdjukaar.moonlight.api.platform.PlatHelper;

public class BetterJukeboxesFabric implements ModInitializer {

    public void onInitialize() {
        if (PlatHelper.getPhysicalSide().isClient()) {
            BetterJukeboxesClient.init();

        }
    }

}
