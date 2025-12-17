package net.mehvahdjukaar.amendments;

import net.mehvahdjukaar.amendments.common.FlowerPotHandler;
import net.mehvahdjukaar.amendments.common.entity.MediumDragonFireball;
import net.mehvahdjukaar.amendments.common.entity.MediumFireball;
import net.mehvahdjukaar.amendments.common.network.ModNetwork;
import net.mehvahdjukaar.amendments.configs.ClientConfigs;
import net.mehvahdjukaar.amendments.configs.CommonConfigs;
import net.mehvahdjukaar.amendments.events.behaviors.InteractEvents;
import net.mehvahdjukaar.amendments.events.dispenser.CauldronDispenserBehavior;
import net.mehvahdjukaar.amendments.integration.CompatHandler;
import net.mehvahdjukaar.amendments.integration.SuppCompat;
import net.mehvahdjukaar.amendments.reg.ModRegistry;
import net.mehvahdjukaar.moonlight.api.fluids.FluidContainerList;
import net.mehvahdjukaar.moonlight.api.fluids.SoftFluid;
import net.mehvahdjukaar.moonlight.api.fluids.SoftFluidRegistry;
import net.mehvahdjukaar.moonlight.api.platform.PlatHelper;
import net.mehvahdjukaar.moonlight.api.platform.RegHelper;
import net.mehvahdjukaar.moonlight.api.util.DispenserHelper;
import net.minecraft.Util;
import net.minecraft.core.*;
import net.minecraft.core.dispenser.AbstractProjectileDispenseBehavior;
import net.minecraft.resources.ResourceLocation;
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
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


public class Amendments {
    public static final String MOD_ID = "amendments";
    public static final Logger LOGGER = LogManager.getLogger("Amendments");

    public static final List<String> OLD_MODS = List.of("supplementaries", "carpeted", "betterlily", "betterjukebox");

    public static ResourceLocation res(String name) {
        return new ResourceLocation(MOD_ID, name);
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
        RegHelper.addDynamicDispenserBehaviorRegistration(Amendments::registerDispenserBehavior);
        RegHelper.registerSimpleRecipeCondition(res("flag"), CommonConfigs::isFlagOn);
        RegHelper.addItemsToTabsRegistration(Amendments::addItemsToTabs);
        RegHelper.addExtraPOIStatesRegistration(Amendments::addExtraPoiStates);


        //TODO: fix sign y offset on FD one and wall signs have weird scale
        //TODO: check all fireballs & dispenser
        // configurable models for wall lanterns and skulls
        // add wall lantern stand model override instead of texture one
        // todo: finish porting

        // register 1 wall lantern per type
        // make bell connections
//healing particles
        //snow golems healing in snow
        //TODO: fix candle holder particle
        // TODO: add sound for wind change and improve fire charge sounds
        //improved entity sync time
        //improved range at which sound plays and such
        // mud slows down mobs
        //here we go. ideas part 2
        //carpeted trapdoor
        //flower pot broken color and grass
        //campfire interact hoppers
        //make directional cake reg override
        //tripwire hook and lead
        // spyglass zoom hotkey
        //banners as capes when in trinket
        //fix normal flower pot model
        //hanging signs banner pattersn and items
        //cobwebs animation string thngies
        //shiny particles on emeralds
        //pink petals
        //waterlogged hollow logs have bubbles wit magma quark
        //particle mod snow rain enderman teleport, smoke wind lightning
        //low tech mod?
    }



    private static void addItemsToTabs(RegHelper.ItemToTabEvent itemToTabEvent) {
        if (CommonConfigs.THROWABLE_FIRE_CHARGES.get()) {
            itemToTabEvent.addBefore(CreativeModeTabs.COMBAT,
                    i -> i.is(Items.SNOWBALL), Items.FIRE_CHARGE);
            if (CommonConfigs.DRAGON_CHARGE.get()) {
                itemToTabEvent.addBefore(CreativeModeTabs.COMBAT,
                        i -> i.is(Items.SNOWBALL), ModRegistry.DRAGON_CHARGE.get());
            }
        }
    }

    private static void addExtraPoiStates(RegHelper.ExtraPOIStatesEvent event) {
        event.addBlocks(PoiTypes.LEATHERWORKER, List.of(ModRegistry.LIQUID_CAULDRON.get(), ModRegistry.DYE_CAULDRON.get()));
    }


    private static void setup() {
        if (CommonConfigs.INVERSE_POTIONS.get() == null) {
            throw new IllegalStateException("Inverse potions config is null. How??");
        }
        if (CompatHandler.SUPPLEMENTARIES) SuppCompat.setup();

        // gg vanilla. They arent even marked as fire immune
        EntityType.SMALL_FIREBALL.fireImmune = true;
        EntityType.FIREBALL.fireImmune = true;
        EntityType.DRAGON_FIREBALL.fireImmune = true;
    }

    private static void setupAsync() {
        FlowerPotHandler.setup();
        ClientConfigs.setup();
    }

    public static void onReload(RegistryAccess registryAccess, boolean client) {
        InteractEvents.setupOverrides();
        if (client) AmendmentsClient.afterTagSetup();
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
