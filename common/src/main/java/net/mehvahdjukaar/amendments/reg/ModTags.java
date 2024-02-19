package net.mehvahdjukaar.amendments.reg;

import net.mehvahdjukaar.amendments.Amendments;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;

public class ModTags {

    //block tags
    public static final TagKey<Block> POSTS = suppTag("posts");
    public static final TagKey<Block> PALISADES = suppTag("palisades");
    public static final TagKey<Block> BEAMS = suppTag("beams");
    public static final TagKey<Block> WALLS = suppTag("walls");

    public static final TagKey<Block> HEAT_SOURCES = blockTag("cauldron_heat_source");

    public static final TagKey<Block> STAIRS_CARPETS = blockTag("stairs_carpets");

    public static final TagKey<Block> VINE_SUPPORT = blockTag("vine_support");

    public static final TagKey<Block> WALL_LANTERNS_BLACKLIST = blockTag("wall_lanterns_blacklist");
    public static final TagKey<Block> WALL_LANTERNS_WHITELIST = blockTag("wall_lanterns_whitelist");

    //item tags

    public static final TagKey<Item> GOES_IN_LECTERN = itemTag("goes_in_lectern");

    private static TagKey<Item> itemTag(String name) {
        return TagKey.create(Registries.ITEM, Amendments.res(name));
    }

    private static TagKey<Block> blockTag(String name) {
        return TagKey.create(Registries.BLOCK, Amendments.res(name));
    }

    private static TagKey<Block> suppTag(String name) {
        return TagKey.create(Registries.BLOCK, new ResourceLocation("supplementaries", name));
    }

}
