package net.mehvahdjukaar.amendments.forge;

import net.minecraftforge.common.ForgeConfig;

public class AmendmentsClientImpl {

    public static boolean hasFixedNormals() {
        return false;// ForgeConfig.CLIENT.calculateAllNormals.get();
    }
}
