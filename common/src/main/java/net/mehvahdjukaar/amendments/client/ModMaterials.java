package net.mehvahdjukaar.amendments.client;

import com.google.common.base.Suppliers;
import net.mehvahdjukaar.amendments.Amendments;
import net.minecraft.client.resources.model.Material;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.state.properties.WoodType;

import java.util.IdentityHashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class ModMaterials {
    public static final ResourceLocation SIGN_SHEET = new ResourceLocation("textures/atlas/signs.png");


    public static final Material CANVAS_SIGH_MATERIAL = new Material(SIGN_SHEET, Amendments.res("entity/signs/hanging/farmersdelight/extension_canvas"));
    public static final Supplier<Map<WoodType, Material>> HANGING_SIGN_EXTENSIONS =
            Suppliers.memoize(() -> WoodType.values().collect(Collectors.toMap(
                    Function.identity(),
                    w -> {
                        String str = w.name();
                        if (str.contains(":")) {
                            str = str.replace(":", "/extension_");
                        } else str = "extension_" + str;
                        return new Material(SIGN_SHEET, Amendments.res("entity/signs/hanging/" + str));
                    },
                    (v1, v2) -> v1,
                    IdentityHashMap::new)));


}
