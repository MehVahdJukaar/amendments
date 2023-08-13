package net.mehvahdjukaar.amendments.fabric;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.mehvahdjukaar.amendments.Amendments;
import net.mehvahdjukaar.amendments.reg.ModEvents;

public class AmendmentsFabric implements ModInitializer {

    public void onInitialize() {
        Amendments.init();
        UseBlockCallback.EVENT.register(ModEvents::onRightClickBlock);
    }

}
