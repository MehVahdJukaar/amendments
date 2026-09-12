package net.mehvahdjukaar.amendments.integration.platform;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import net.mehvahdjukaar.amendments.Amendments;
import net.mehvahdjukaar.moonlight.api.platform.ClientHelper;
import net.minecraft.resources.Identifier;

public class ModMenuCompat implements ModMenuApi {

    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        return parent -> ClientHelper.getMoonlightConfigScreen(Amendments.MOD_ID, parent,
                Identifier.withDefaultNamespace("textures/block/deepslate_tiles.png"));
    }
}
