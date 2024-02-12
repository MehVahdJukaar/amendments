package net.mehvahdjukaar.amendments.fabric;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.CommonLifecycleEvents;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.fabricmc.fabric.api.event.player.UseItemCallback;
import net.mehvahdjukaar.amendments.Amendments;
import net.mehvahdjukaar.amendments.events.ModEvents;
import net.mehvahdjukaar.amendments.events.behaviors.PlaceEventsHandler;
import net.mehvahdjukaar.supplementaries.common.events.ServerEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;

public class AmendmentsFabric implements ModInitializer {

    public void onInitialize() {
        Amendments.init();
        UseBlockCallback.EVENT.register(ModEvents::onRightClickBlockHP);
        UseBlockCallback.EVENT.register(ModEvents::onRightClickBlock);
        UseItemCallback.EVENT.register(ModEvents::onUseItem);
        CommonLifecycleEvents.TAGS_LOADED.register(Amendments::onCommonTagUpdate);
    }

}
