package net.mehvahdjukaar.amendments.forge;

import net.mehvahdjukaar.amendments.Amendments;
import net.mehvahdjukaar.amendments.configs.ClientConfigs;
import net.mehvahdjukaar.amendments.integration.forge.BlueprintIntegration;
import net.mehvahdjukaar.amendments.common.block.StructureCauldronHack;
import net.mehvahdjukaar.amendments.events.ModEvents;
import net.mehvahdjukaar.amendments.integration.CompatHandler;
import net.mehvahdjukaar.amendments.integration.forge.configured.ModConfigSelectScreen;
import net.mehvahdjukaar.amendments.reg.ModRegistry;
import net.mehvahdjukaar.moonlight.api.platform.PlatHelper;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.ai.village.poi.PoiType;
import net.minecraft.world.entity.ai.village.poi.PoiTypes;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TagsUpdatedEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.server.ServerAboutToStartEvent;
import net.minecraftforge.event.server.ServerStartedEvent;
import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.RegisterEvent;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static net.mehvahdjukaar.amendments.Amendments.MOD_ID;

/**
 * Author: MehVahdJukaar
 */
@Mod(MOD_ID)
public class AmendmentsForge {

    public AmendmentsForge() {
        Amendments.init();
        IEventBus bus = FMLJavaModLoadingContext.get().getModEventBus();
        bus.addListener(AmendmentsForge::onRegisterPOI);
        MinecraftForge.EVENT_BUS.register(this);
        if (PlatHelper.getPhysicalSide().isClient()) {
            MinecraftForge.EVENT_BUS.register(ClientEvents.class);
            if (CompatHandler.CONFIGURED && ClientConfigs.CUSTOM_CONFIGURED_SCREEN.get()) {
                ModConfigSelectScreen.registerConfigScreen(Amendments.MOD_ID, ModConfigSelectScreen::new);
            }
        }

        if(CompatHandler.BLUEPRINT){
            StructureCauldronHack.register();
            BlueprintIntegration.init();
        }
    }

    public static void onRegisterPOI(RegisterEvent event) {
        if (event.getRegistryKey() == Registries.POINT_OF_INTEREST_TYPE) {
            Set<BlockState> extraStates = Stream.of(ModRegistry.LIQUID_CAULDRON.get(), ModRegistry.DYE_CAULDRON.get()).flatMap(
                    (block) -> block.getStateDefinition().getPossibleStates().stream()).collect(Collectors.toSet());
            var holder = BuiltInRegistries.POINT_OF_INTEREST_TYPE.getHolderOrThrow(PoiTypes.LEATHERWORKER);
            PoiType value = holder.value();
            var set = new HashSet<>(value.matchingStates);
            set.addAll(extraStates);
            PoiType poiType = new PoiType(set, value.maxTickets(), value.validRange());

            event.getForgeRegistry().register(new ResourceLocation("leatherworker"), poiType);

        }
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

    @SubscribeEvent
    public void onTagUpdate(TagsUpdatedEvent event) {
        Amendments.onCommonTagUpdate(event.getRegistryAccess(),
                event.getUpdateCause() == TagsUpdatedEvent.UpdateCause.CLIENT_PACKET_RECEIVED);
    }
}
