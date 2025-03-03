package net.mehvahdjukaar.amendments.fabric;

import net.fabricmc.loader.api.FabricLoader;

import java.util.stream.Stream;

public class AmendmentsClientImpl {
    public static boolean hasFixedNormals() {
        return false;
    }

    public static Stream<String> getAllLoadedMods() {
     return    FabricLoader.getInstance().getAllMods().stream()
                .map(modContainer -> modContainer.getMetadata().getId());
    }
}
