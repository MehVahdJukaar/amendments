package net.mehvahdjukaar.amendments.integration.platform;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import net.mehvahdjukaar.amendments.Amendments;
import net.mehvahdjukaar.moonlight.core.client.config.MoonlightConfigSelectScreen;
import net.minecraft.resources.ResourceLocation;

public class ModMenuCompat implements ModMenuApi {

    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        return parent -> MoonlightConfigSelectScreen.create(Amendments.MOD_ID, parent,
                ResourceLocation.withDefaultNamespace("textures/block/deepslate_tiles.png"));
    }
}
