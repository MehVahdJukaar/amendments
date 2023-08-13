package net.mehvahdjukaar.amendments.forge;

import net.mehvahdjukaar.amendments.Amendments;
import net.mehvahdjukaar.amendments.reg.ModEvents;
import net.minecraft.world.InteractionResult;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import static net.mehvahdjukaar.amendments.Amendments.MOD_ID;

/**
 * Author: MehVahdJukaar
 */
@Mod(MOD_ID)
public class AmendmentsForge {

    public AmendmentsForge() {
        Amendments.init();
        MinecraftForge.EVENT_BUS.register(this);
    }

    @SubscribeEvent(priority = EventPriority.HIGH)
    public void onUseBlock(PlayerInteractEvent.RightClickBlock event) {
        if (!event.isCanceled()) {
            var ret = ModEvents.onRightClickBlock(event.getEntity(), event.getLevel(), event.getHand(), event.getHitVec());
            if (ret != InteractionResult.PASS) {
                event.setCanceled(true);
                event.setCancellationResult(ret);

            }
        }
    }
}
