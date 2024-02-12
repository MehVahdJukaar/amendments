package net.mehvahdjukaar.amendments.reg;

import net.mehvahdjukaar.amendments.Amendments;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;

public class ModTags {

    //block tags
    public static final TagKey<Block> POSTS = blockTag("posts");
    public static final TagKey<Block> PALISADES = blockTag("palisades");
    public static final TagKey<Block> BEAMS = blockTag("beams");
    public static final TagKey<Block> WALLS = blockTag("walls");

    public static final TagKey<Block> STAIRS_CARPETS = blockTag("stairs_carpets");

    public static final TagKey<Block> VINE_SUPPORT = blockTag("vine_support");

    public static final TagKey<Block> WALL_LANTERNS_BLACKLIST = blockTag("wall_lanterns_blacklist");
    public static final TagKey<Block> WALL_LANTERNS_WHITELIST = blockTag("wall_lanterns_whitelist");

    //item tags

    public static final TagKey<Item> COOKIES = itemTag("cookies");
    public static final TagKey<Item> BRICKS = itemTag("throwable_bricks");
    public static final TagKey<Item> ROPES = itemTag("ropes");
    public static final TagKey<Item> CHAINS = itemTag("chains");
    public static final TagKey<Item> GOES_IN_LECTERN = itemTag("goes_in_lecterns");

    private static TagKey<Item> itemTag(String name) {
        return TagKey.create(Registries.ITEM, Amendments.res(name));
    }

    private static TagKey<Block> blockTag(String name) {
        return TagKey.create(Registries.BLOCK, Amendments.res(name));
    }


}
