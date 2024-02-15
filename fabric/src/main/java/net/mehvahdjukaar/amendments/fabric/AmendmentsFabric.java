package net.mehvahdjukaar.amendments.fabric;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.CommonLifecycleEvents;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.fabricmc.fabric.api.event.player.UseItemCallback;
import net.mehvahdjukaar.amendments.Amendments;
import net.mehvahdjukaar.amendments.events.ModEvents;

public class AmendmentsFabric implements ModInitializer {

    public void onInitialize() {
        Amendments.init();
        UseBlockCallback.EVENT.register(ModEvents::onRightClickBlockHP);
        UseBlockCallback.EVENT.register(ModEvents::onRightClickBlock);
        UseItemCallback.EVENT.register(ModEvents::onUseItem);
        CommonLifecycleEvents.TAGS_LOADED.register(Amendments::onCommonTagUpdate);
    }

}
