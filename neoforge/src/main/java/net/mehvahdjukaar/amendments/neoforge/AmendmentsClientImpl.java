package net.mehvahdjukaar.amendments.neoforge;

import net.neoforged.fml.ModList;
import net.neoforged.neoforgespi.language.IModInfo;
package net.mehvahdjukaar.amendments.forge;

import net.mehvahdjukaar.moonlight.api.platform.PlatHelper;
import net.minecraftforge.common.ForgeConfig;
import net.minecraftforge.fml.ModList;

import java.util.stream.Stream;

public class AmendmentsClientImpl {

    public static boolean hasFixedNormals() {
        return false;// ForgeConfig.CLIENT.calculateAllNormals.get();
    }

}
