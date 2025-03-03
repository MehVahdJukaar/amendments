package net.mehvahdjukaar.amendments.neoforge;

import net.neoforged.fml.ModList;
import net.neoforged.neoforgespi.language.IModInfo;

import java.util.stream.Stream;

public class AmendmentsClientImpl {

    public static boolean hasFixedNormals() {
        return false;// ForgeConfig.CLIENT.calculateAllNormals.get();
    }

    public static Stream<Object> getAllLoadedMods() {
        return ModList.get().getMods().stream()
                .map(IModInfo::getModId);
    }
}
