package net.mehvahdjukaar.amendments.forge;

import net.mehvahdjukaar.amendments.Amendments;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.RecordItem;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.MissingMappingsEvent;


@Mod.EventBusSubscriber(modid = Amendments.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class RemapHandler {


    @SubscribeEvent
    public static void onRemapBlocks(MissingMappingsEvent event) {
        remapAll(event, BuiltInRegistries.BLOCK);
        remapAll(event, BuiltInRegistries.ITEM);
        remapAll(event, BuiltInRegistries.BLOCK_ENTITY_TYPE);
        remapAll(event, BuiltInRegistries.ENTITY_TYPE);
    }


    private static <T> void remapAll(MissingMappingsEvent event, Registry<T> registry) {
        for (var mod : Amendments.OLD_MODS) {
            for (var mapping : event.getMappings(registry.key(), mod)) {
                ResourceLocation newLoc = Amendments.res(mapping.getKey().getPath());
                var newBlock = registry.getOptional(newLoc);
                newBlock.ifPresent(mapping::remap);
            }
        }
    }

}
