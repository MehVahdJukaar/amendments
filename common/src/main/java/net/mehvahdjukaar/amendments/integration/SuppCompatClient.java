package net.mehvahdjukaar.amendments.integration;

import net.mehvahdjukaar.candlelight.api.ClientOnly;
import net.mehvahdjukaar.supplementaries.client.ModMaterials;
import net.minecraft.client.resources.model.Material;
import net.minecraft.world.item.BannerPatternItem;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

public class SuppCompatClient {

    @Nullable
    public static Material getFlagMaterial(Level l, BannerPatternItem bannerPatternItem) {
        return ModMaterials.getFlagMaterialForPatternItem(l, bannerPatternItem);
    }
}
