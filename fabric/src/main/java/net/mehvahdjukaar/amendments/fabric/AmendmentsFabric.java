package net.mehvahdjukaar.amendments.fabric;

import com.google.common.collect.Lists;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.client.item.v1.ItemTooltipCallback;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.lifecycle.v1.CommonLifecycleEvents;
import net.fabricmc.fabric.api.event.player.AttackBlockCallback;
import net.fabricmc.fabric.api.event.player.AttackEntityCallback;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.fabricmc.fabric.api.event.player.UseItemCallback;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidStorage;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.loader.api.FabricLoader;
import net.mehvahdjukaar.amendments.Amendments;
import net.mehvahdjukaar.amendments.AmendmentsClient;
import net.mehvahdjukaar.amendments.events.ModEvents;
import net.mehvahdjukaar.amendments.reg.ModConstants;
import net.mehvahdjukaar.amendments.reg.ModRegistry;
import net.mehvahdjukaar.moonlight.api.platform.PlatHelper;
import net.mehvahdjukaar.supplementaries.mixins.BookViewScreenMixin;
import net.mehvahdjukaar.supplementaries.mixins.ExplosionMixin;
import net.minecraft.Util;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.ai.village.poi.PoiTypes;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.block.state.BlockState;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class AmendmentsFabric implements ModInitializer {

    public static final List<String> NAMES = Util.make(Lists.newArrayList(), list -> {
        list.add(ModConstants.CEILING_BANNER_NAME);
        list.add(ModConstants.HANGING_FLOWER_POT_NAME);
        list.add(ModConstants.LIQUID_CAULDRON_NAME);
        list.add(ModConstants.DYE_CAULDRON_NAME);
        list.add(ModConstants.WATER_LILY_NAME);
        list.add(ModConstants.DYE_BOTTLE_NAME);
        list.add(ModConstants.CARPETED_STAIR_NAME);
        list.add(ModConstants.CARPETED_SLAB_NAME);
        list.add(ModConstants.DIRECTIONAL_CAKE_NAME);
        list.add(ModConstants.SKULL_PILE_NAME);
        list.add(ModConstants.SKULL_CANDLE_NAME);
        list.add(ModConstants.SKULL_CANDLE_SOUL_NAME);
        list.add(ModConstants.WALL_LANTERN_NAME);
        list.add(ModConstants.FALLING_LANTERN_NAME);
    });
    private static final ResourceLocation LOW_PRIORITY = Amendments.res("low");

    public static ResourceLocation shouldRemap(String namespace, String path) {
        if (Amendments.OLD_MODS.contains(namespace)) {
            if (NAMES.contains(path)) {
                return Amendments.res(path);
            }
        }
        return null;
    }

    public void onInitialize() {
        Amendments.init();
        UseBlockCallback.EVENT.register(ModEvents::onRightClickBlockHP);
        UseBlockCallback.EVENT.register(ModEvents::onRightClickBlock);
        UseItemCallback.EVENT.register(ModEvents::onUseItem);
        if (PlatHelper.getPhysicalSide().isClient()) {
            ItemTooltipCallback.EVENT.register(AmendmentsClient::onItemTooltip);
        }

        AttackEntityCallback.EVENT.addPhaseOrdering(Event.DEFAULT_PHASE, LOW_PRIORITY);
        AttackEntityCallback.EVENT.register(LOW_PRIORITY,ModEvents::onAttackEntity);

        PlatHelper.addCommonSetup(() -> {
            FluidStorage.SIDED.registerForBlockEntity((myTank, direction) -> (Storage<FluidVariant>) myTank,
                    ModRegistry.LIQUID_CAULDRON_TILE.get());
        });
    }

}
