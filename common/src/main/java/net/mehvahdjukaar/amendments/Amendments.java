package net.mehvahdjukaar.amendments;

import net.mehvahdjukaar.amendments.common.FlowerPotHandler;
import net.mehvahdjukaar.amendments.common.entity.MediumDragonFireball;
import net.mehvahdjukaar.amendments.common.entity.MediumFireball;
import net.mehvahdjukaar.amendments.common.network.ModNetwork;
import net.mehvahdjukaar.amendments.configs.ClientConfigs;
import net.mehvahdjukaar.amendments.configs.CommonConfigs;
import net.mehvahdjukaar.amendments.events.behaviors.CauldronConversion;
import net.mehvahdjukaar.amendments.events.behaviors.InteractEvents;
import net.mehvahdjukaar.amendments.integration.CompatHandler;
import net.mehvahdjukaar.amendments.integration.SuppCompat;
import net.mehvahdjukaar.amendments.reg.ModRegistry;
import net.mehvahdjukaar.moonlight.api.fluids.FluidContainerList;
import net.mehvahdjukaar.moonlight.api.fluids.SoftFluid;
import net.mehvahdjukaar.moonlight.api.fluids.SoftFluidRegistry;
import net.mehvahdjukaar.moonlight.api.misc.EventCalled;
import net.mehvahdjukaar.moonlight.api.platform.PlatHelper;
import net.mehvahdjukaar.moonlight.api.platform.RegHelper;
import net.mehvahdjukaar.moonlight.api.util.DispenserHelper;
import net.minecraft.Util;
import net.minecraft.core.*;
import net.minecraft.core.dispenser.AbstractProjectileDispenseBehavior;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.ai.village.poi.PoiTypes;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SupportType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.RailShape;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;


public class Amendments {
    public static final String MOD_ID = "amendments";
    public static final Logger LOGGER = LogManager.getLogger("Amendments");

    public static final List<String> OLD_MODS = List.of("supplementaries", "carpeted", "betterlily", "betterjukebox");

    public static ResourceLocation res(String name) {
        return ResourceLocation.fromNamespaceAndPath(MOD_ID, name);
    }

    public static void init() {
        CommonConfigs.init();
        ModRegistry.init();
        ModNetwork.init();
        if (PlatHelper.getPhysicalSide().isClient()) {
            ClientConfigs.init();
            AmendmentsClient.init();
        }
        PlatHelper.addCommonSetupAsync(Amendments::setupAsync);
        PlatHelper.addCommonSetup(Amendments::setup);
        PlatHelper.addReloadableCommonSetup(Amendments::onReload);
        RegHelper.addDynamicDispenserBehaviorRegistration(Amendments::registerDispenserBehaviors);

        RegHelper.registerSimpleRecipeCondition(res("flag"), CommonConfigs::isFlagOn);
        RegHelper.addItemsToTabsRegistration(Amendments::addItemsToTabs);

        //TODO: check all fireballs & dispenser
        // configurable models for wall lanterns and skulls
        // todo: finish porting

        // register 1 wall lantern per type
        // make bell connections

        // mud slows down mobs
        //here we go. ideas part 2
        //carpeted trapdoor
        //flower pot broken color and grass
        //campfire interact hoppers
        //make directional cake reg override
        //tripwire hook and lead
        // spyglass zoom hotkey
        //sniffer eggdirectonal
        //banners as capes when in trinket
        //fix normal flower pot model
        //cobwebs animation string thngies
        //shiny particles on emeralds
        //pink petals on water (wuark does it)
        //particle mod snow rain enderman teleport, smoke wind lightning
        //low tech mod?
    }


    private static void setup() {
        if (CommonConfigs.INVERSE_POTIONS.get() == null) {
            throw new IllegalStateException("Inverse potions config is null. How??");
        }
        if (CompatHandler.SUPPLEMENTARIES) SuppCompat.setup();

        var holder = BuiltInRegistries.POINT_OF_INTEREST_TYPE.getHolderOrThrow(PoiTypes.LEATHERWORKER);
        var set = new HashSet<>(holder.value().matchingStates);
        Set<BlockState> extraStates = Stream.of(ModRegistry.LIQUID_CAULDRON.get(), ModRegistry.DYE_CAULDRON.get()).flatMap(
                (block) -> block.getStateDefinition().getPossibleStates().stream()).collect(Collectors.toSet());
        set.addAll(extraStates);
        holder.value().matchingStates = set;
        PoiTypes.registerBlockStates(holder, extraStates);
    }

    private static void setupAsync() {
        FlowerPotHandler.setup();
    }

    public static void onReload(RegistryAccess registryAccess, boolean client) {
        InteractEvents.setupOverrides();
        if (client) AmendmentsClient.afterTagSetup();
    }


    @EventCalled
    private static void registerDispenserBehaviors(DispenserHelper.Event event) {
        for (SoftFluid f : SoftFluidRegistry.getRegistry(event.getRegistryAccess())) {
            Set<Item> itemSet = new HashSet<>();
            Collection<FluidContainerList.Category> categories = f.getContainerList().getCategories();
            for (FluidContainerList.Category c : categories) {
                for (Item full : c.getFilledItems()) {
                    if (full != Items.AIR && !itemSet.contains(full)) {
                        event.register(new CauldronConversion.DispenserBehavior(full));
                        itemSet.add(full);
                    }
                }
            }
        }
    }

    public static void registerFluidBehavior(SoftFluid f, DispenserHelper.Event event) {
        Set<Item> itemSet = new HashSet<>();
        Collection<FluidContainerList.Category> categories = f.getContainerList().getCategories();
        for (FluidContainerList.Category c : categories) {
            for (Item full : c.getFilledItems()) {
                if (full != Items.AIR && !itemSet.contains(full)) {
                    event.register(new CauldronDispenserBehavior(full));
                    itemSet.add(full);
                }
            }
        }
    }

    private static void registerDispenserBehavior(DispenserHelper.Event event) {
        for (SoftFluid f : SoftFluidRegistry.getRegistry(event.getRegistryAccess())) {
            registerFluidBehavior(f, event);
        }
        if (CommonConfigs.FIRE_CHARGE_DISPENSER.get() && CommonConfigs.THROWABLE_FIRE_CHARGES.get()) {
            event.register(Items.FIRE_CHARGE, new AbstractProjectileDispenseBehavior() {
                @Override
                protected MediumFireball getProjectile(Level level, Position position, ItemStack stack) {
                    return Util.make(new MediumFireball(level, position.x(), position.y(), position.z()), (snowball) -> {
                        snowball.setItem(stack);
                    });

                }

                @Override
                protected void playSound(BlockSource source) {
                    source.getLevel().levelEvent(1018, source.getPos(), 0);
                }
            });
        }
        if (CommonConfigs.DRAGON_CHARGE.get()) {
            event.register(ModRegistry.DRAGON_CHARGE.get(), new AbstractProjectileDispenseBehavior() {
                @Override
                protected MediumDragonFireball getProjectile(Level level, Position position, ItemStack stack) {
                    return Util.make(new MediumDragonFireball(level, position.x(), position.y(), position.z()), (snowball) -> {
                        snowball.setItem(stack);
                    });
                }

                @Override
                protected void playSound(BlockSource source) {
                    //TODO:customsound
                    source.getLevel().levelEvent(1018, source.getPos(), 0);
                }
            });
        }
    }
    public static boolean isSupportingCeiling(BlockPos pos, LevelReader world) {
        return isSupportingCeiling(world.getBlockState(pos), pos, world);
    }

    public static boolean isSupportingCeiling(BlockState upState, BlockPos pos, LevelReader world) {
        if (CompatHandler.SUPPLEMENTARIES) return SuppCompat.isSupportingCeiling(upState, pos, world);
        return Block.canSupportCenter(world, pos, Direction.DOWN);
    }

    public static boolean canConnectDown(BlockState neighborState, LevelAccessor level, BlockPos pos) {
        if (CompatHandler.SUPPLEMENTARIES) return SuppCompat.canConnectDown(neighborState);
        return neighborState.isFaceSturdy(level, pos, Direction.UP, SupportType.CENTER);
    }

}
