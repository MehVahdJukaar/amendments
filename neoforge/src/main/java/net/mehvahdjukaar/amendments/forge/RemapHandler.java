package net.mehvahdjukaar.amendments.forge;

import net.mehvahdjukaar.amendments.Amendments;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.registries.IdMappingEvent;

@EventBusSubscriber(modid = Amendments.MOD_ID, bus = EventBusSubscriber.Bus.GAME)
public class RemapHandler {


    @SubscribeEvent
    public static void onRemapBlocks(IdMappingEvent event) {
        remapAll(event, BuiltInRegistries.BLOCK);
        remapAll(event, BuiltInRegistries.ITEM);
        remapAll(event, BuiltInRegistries.BLOCK_ENTITY_TYPE);
        remapAll(event, BuiltInRegistries.ENTITY_TYPE);
    }


    private static <T> void remapAll(IdMappingEvent event, Registry<T> registry) {
        for (var mod : Amendments.OLD_MODS) {
            for (var mapping : event.getMappings(registry.key(), mod)) {
                ResourceLocation newLoc = Amendments.res(mapping.getKey().getPath());
                var newBlock = registry.getOptional(newLoc);
                newBlock.ifPresent(mapping::remap);
            }
        }
    }

}
