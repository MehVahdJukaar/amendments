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

    public static final TagKey<SoftFluid> CAN_BOIL = fluidTag("can_boil");
    public static final TagKey<SoftFluid> CAULDRON_BLACKLIST = fluidTag("cant_go_in_cauldron");
    //block tags
    public static final TagKey<Block> POSTS = suppTag("posts");
    public static final TagKey<Block> PALISADES = suppTag("palisades");
    public static final TagKey<Block> BEAMS = suppTag("beams");
    public static final TagKey<Block> WALLS = suppTag("walls");
    public static final TagKey<Block> FALLING_LANTERNS_BLACKLIST = suppTag("falling_lantern_blacklist");

    public static final TagKey<Block> HEAT_SOURCES = blockTag("cauldron_heat_source");

    public static final TagKey<Block> STAIRS_CARPETS = blockTag("stairs_carpets");

    public static final TagKey<Block> VINE_SUPPORT = blockTag("vine_support");


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
