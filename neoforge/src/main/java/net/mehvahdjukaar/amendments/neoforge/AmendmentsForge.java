package net.mehvahdjukaar.amendments.neoforge;

import net.mehvahdjukaar.amendments.Amendments;
import net.mehvahdjukaar.amendments.common.block.StructureCauldronHack;
import net.mehvahdjukaar.amendments.configs.ClientConfigs;
import net.mehvahdjukaar.amendments.events.ModEvents;
import net.mehvahdjukaar.amendments.integration.CompatHandler;
import net.mehvahdjukaar.amendments.integration.neoforge.BlueprintIntegration;
import net.mehvahdjukaar.amendments.integration.neoforge.configured.ModConfigSelectScreen;
import net.mehvahdjukaar.amendments.reg.ModRegistry;
import net.mehvahdjukaar.moonlight.api.fluids.neoforge.SoftFluidTankFluidHandlerWrapper;
import net.mehvahdjukaar.moonlight.api.platform.PlatHelper;
import net.mehvahdjukaar.moonlight.api.platform.RegHelper;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.player.AttackEntityEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;

import static net.mehvahdjukaar.amendments.Amendments.MOD_ID;

/**
 * Author: MehVahdJukaar
 */
@Mod(MOD_ID)
public class AmendmentsForge {

    public AmendmentsForge(IEventBus bus) {
        RegHelper.startRegisteringFor(bus);

        Amendments.init();
        bus.addListener(AmendmentsForge::registerCapabilities);
        NeoForge.EVENT_BUS.register(this);
        if (PlatHelper.getPhysicalSide().isClient()) {
            NeoForge.EVENT_BUS.register(ClientEvents.class);
            if (CompatHandler.CONFIGURED && ClientConfigs.CUSTOM_CONFIGURED_SCREEN.get()) {
                ModConfigSelectScreen.registerConfigScreen(Amendments.MOD_ID, ModConfigSelectScreen::new);
            }
        }

        if (CompatHandler.BLUEPRINT) {
            StructureCauldronHack.register();
            BlueprintIntegration.init();
        }
    }

    private static void registerCapabilities(RegisterCapabilitiesEvent event) {
        event.registerBlockEntity(
                Capabilities.FluidHandler.BLOCK,
                ModRegistry.LIQUID_CAULDRON_TILE.get(),
                (myBlockEntity, side) -> SoftFluidTankFluidHandlerWrapper.wrap(myBlockEntity)
        );
    }

    @SubscribeEvent(priority = EventPriority.LOW)
    public void onUseBlock(PlayerInteractEvent.RightClickBlock event) {
        if (!event.isCanceled()) {
            var ret = ModEvents.onRightClickBlock(event.getEntity(), event.getLevel(), event.getHand(), event.getHitVec());
            if (ret != InteractionResult.PASS) {
                event.setCanceled(true);
                event.setCancellationResult(ret);
            }
        }
    }

    //TODO: use ivingDamageEvent.Post (in 1.21)
    //runs after other damage events in case this were to be cancelled
    @SubscribeEvent(priority = EventPriority.LOW)
    public void onEntityHurt(AttackEntityEvent event) {
        if (!event.isCanceled()) {
            ModEvents.onAttackEntity(event.getEntity(), event.getEntity().level(),
                    InteractionHand.MAIN_HAND, event.getTarget(), null);
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGH)
    public void onUseBlockHP(PlayerInteractEvent.RightClickBlock event) {
        if (!event.isCanceled()) {
            var ret = ModEvents.onRightClickBlockHP(event.getEntity(), event.getLevel(), event.getHand(), event.getHitVec());
            if (ret != InteractionResult.PASS) {
                event.setCanceled(true);
                event.setCancellationResult(ret);
            }
        }
    }

    @SubscribeEvent(priority = EventPriority.LOW)
    public void onUseItem(PlayerInteractEvent.RightClickItem event) {
        if (!event.isCanceled()) {
            var ret = ModEvents.onUseItem(event.getEntity(), event.getLevel(), event.getHand());
            if (ret.getResult() != InteractionResult.PASS) {
                event.setCanceled(true);
                event.setCancellationResult(ret.getResult());
            }
        }
    }

}
