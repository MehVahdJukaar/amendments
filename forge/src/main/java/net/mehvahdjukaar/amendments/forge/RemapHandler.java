package net.mehvahdjukaar.amendments.forge;

import net.mehvahdjukaar.amendments.Amendments;
import net.minecraft.core.DefaultedRegistry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.MissingMappingsEvent;


@Mod.EventBusSubscriber
public class RemapHandler {


    @SubscribeEvent
    public static void onRemapBlocks(MissingMappingsEvent event) {
        remapAll(event, BuiltInRegistries.BLOCK);
        remapAll(event, BuiltInRegistries.ITEM);
    }


    private static <T> void remapAll(MissingMappingsEvent event, DefaultedRegistry<T> block) {
        for (var mod : Amendments.OLD_MODS) {
            for (var mapping : event.getMappings(block.key(), mod)) {
                ResourceLocation newLoc = Amendments.res(mapping.getKey().getPath());
                var newBlock = block.getOptional(newLoc);
                newBlock.ifPresent(mapping::remap);
            }
        }
    }

}
