package net.mehvahdjukaar.amendments.reg;

import net.mehvahdjukaar.amendments.Amendments;
import net.mehvahdjukaar.moonlight.api.fluids.SoftFluid;
import net.mehvahdjukaar.moonlight.api.fluids.SoftFluidRegistry;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;

public class ModTags {

    public static final TagKey<SoftFluid> CANT_BOIL = fluidTag("cant_boil");
    public static final TagKey<SoftFluid> NO_TINT_IN_CAULDRON = fluidTag("no_tint_in_cauldron");
    public static final TagKey<SoftFluid> CANT_EXTINGUISH = fluidTag("cant_extinguish");
    public static final TagKey<SoftFluid> CAN_GLOW = fluidTag("can_glow");
    public static final TagKey<SoftFluid> CAULDRON_BLACKLIST = fluidTag("cant_go_in_liquid_cauldron");
    //block tags
    public static final TagKey<Block> COLUMN_SHAPE_4X4 = suppTag("column_shape_4x4");
    public static final TagKey<Block> COLUMN_SHAPE_6X6 = suppTag("column_shape_6x6");
    public static final TagKey<Block> COLUMN_SHAPE_8X8 = suppTag("column_shape_8x8");
    public static final TagKey<Block> COLUMN_SHAPE_10X10 = suppTag("column_shape_10x10");
    public static final TagKey<Block> FALLING_LANTERNS_BLACKLIST = suppTag("falling_lantern_blacklist");

    public static final TagKey<Block> HEAT_SOURCES = blockTag("cauldron_heat_source");

    public static final TagKey<Block> STAIRS_CARPETS = blockTag("stairs_carpets");

    public static final TagKey<Block> VINE_SUPPORT = blockTag("vine_support");

    public static final TagKey<Block> WALL_LANTERNS = blockTag("wall_lanterns");


    //item tags

    public static final TagKey<Item> GOES_IN_LECTERN = itemTag("goes_in_lectern");
    public static final TagKey<Item> GOES_IN_TRIPWIRE_HOOK = itemTag("goes_in_tripwire_hook");
    public static final TagKey<Item> SET_ENTITY_ON_FIRE = itemTag("sets_on_fire");
    public static final TagKey<Item> SKULL_PILE_BLACKLIST = itemTag("non_stackable_heads");

    private static TagKey<Item> itemTag(String name) {
        return TagKey.create(Registries.ITEM, Amendments.res(name));
    }

    private static TagKey<Block> blockTag(String name) {
        return TagKey.create(Registries.BLOCK, Amendments.res(name));
    }

    private static TagKey<SoftFluid> fluidTag(String name) {
        return TagKey.create(SoftFluidRegistry.KEY, Amendments.res(name));
    }

    private static TagKey<Block> suppTag(String name) {
        return TagKey.create(Registries.BLOCK, ResourceLocation.fromNamespaceAndPath("supplementaries", name));
    }

}
