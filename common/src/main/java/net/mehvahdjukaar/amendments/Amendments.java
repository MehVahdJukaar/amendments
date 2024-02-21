package net.mehvahdjukaar.amendments;

import net.mehvahdjukaar.amendments.common.FlowerPotHandler;
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
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.datafix.DataFixers;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
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

        RegHelper.registerSimpleRecipeCondition(res("flag"), CommonConfigs::isFlagOn);

        // make bell connections

        // mud slows down mobs
        //TODO: check bell ringing with rope
        //here we go. ideas part 2
        //carpeted trapdoor
        //flower pot broken color and grass
        //lantern holding animation
        //campfire interact hoppers
        //make directional cake reg override
        //tripwire hook and lead
        // spyglass zoom hotkey
        //banners as capes when in trinket
        //fix normal flower pot model
        //hanging signs banner pattersn and items
        //cobwebs animation string thngies
        //shiny particles on emeralds

        //waterlogged hollow logs have bubbles wit magma quark
        //particle mod snow rain enderman teleport, smoke wind lightning
    }

    private static void setup() {

    }

    private static void setupAsync() {
        FlowerPotHandler.setup();
    }

    private static boolean hasRun = false;

    @EventCalled
    public static void onCommonTagUpdate(RegistryAccess registryAccess, boolean client) {
        InteractEvents.setupOverrides();
        if (!hasRun) {
            hasRun = true;
            for (SoftFluid f : SoftFluidRegistry.getRegistry(registryAccess)) {
                registerFluidBehavior(f);
            }
        }
        if (client) AmendmentsClient.lateClientSetup();
    }

    public static void registerFluidBehavior(SoftFluid f) {
        Set<Item> itemSet = new HashSet<>();
        Collection<FluidContainerList.Category> categories = f.getContainerList().getCategories();
        for (FluidContainerList.Category c : categories) {
            for (Item full : c.getFilledItems()) {
                if (full != Items.AIR && !itemSet.contains(full)) {
                    DispenserHelper.registerCustomBehavior(new CauldronConversion.DispenserBehavior(full));
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

    public static boolean canConnectDown(BlockState neighborState) {
        if (CompatHandler.SUPPLEMENTARIES) return SuppCompat.canConnectDown(neighborState);
        return neighborState.isSolid();
    }

}
