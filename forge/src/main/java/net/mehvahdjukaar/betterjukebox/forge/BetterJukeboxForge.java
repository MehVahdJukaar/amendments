package net.mehvahdjukaar.betterjukebox.forge;

import net.mehvahdjukaar.betterjukebox.BetterJukeboxes;
import net.mehvahdjukaar.betterjukebox.BetterJukeboxesClient;
import net.mehvahdjukaar.moonlight.api.platform.PlatHelper;
import net.minecraftforge.fml.common.Mod;

import static net.mehvahdjukaar.betterjukebox.BetterJukeboxes.MOD_ID;

/**
 * Author: MehVahdJukaar
 */
@Mod(MOD_ID)
public class BetterJukeboxForge {

    public BetterJukeboxForge() {
        if (PlatHelper.getPhysicalSide().isClient()) {
            BetterJukeboxesClient.init();

        }
    }
}
