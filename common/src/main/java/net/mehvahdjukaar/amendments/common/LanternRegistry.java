package net.mehvahdjukaar.amendments.common;

import net.mehvahdjukaar.moonlight.api.set.BlockType;
import net.mehvahdjukaar.moonlight.api.set.BlockTypeRegistry;
import net.mehvahdjukaar.moonlight.api.util.Utils;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LanternBlock;

import java.util.Optional;
import java.util.Set;

public class LanternRegistry extends BlockTypeRegistry<LanternRegistry.LanternType> {

    public static final LanternRegistry INSTANCE = new LanternRegistry();

    public static final LanternType VANILLA = new LanternType(ResourceLocation.withDefaultNamespace("lantern"), Blocks.LANTERN);
    public static final LanternType SOUL = new LanternType(ResourceLocation.withDefaultNamespace("soul_lantern"), Blocks.SOUL_LANTERN);

    private static final Set<String> BLACKLIST_MODS = Set.of(
            "bbb", "extlights", "betterendforge", "spelunkery", "galosphere", "tconstruct", "enigmaticlegacy", "beautify"
    );

    private static final Set<ResourceLocation> WHITELIST = Set.of(
            ResourceLocation.fromNamespaceAndPath("enlightened_end", "xenon_lantern")
    );

    private static final Set<String> WHITELIST_NAMESPACES = Set.of("skinnedlanterns");

    private LanternRegistry() {
        super(LanternType.class, "lantern");
    }

    @Override
    public LanternType getDefaultType() {
        return VANILLA;
    }

    @Override
    public Optional<LanternType> detectTypeFromBlock(Block block, ResourceLocation blockId) {
        if (block.asItem() == Items.AIR) return Optional.empty();

        if (WHITELIST.contains(blockId)) return Optional.of(new LanternType(blockId, block));

        String namespace = blockId.getNamespace();
        if (BLACKLIST_MODS.contains(namespace)) return Optional.empty();

        if (WHITELIST_NAMESPACES.contains(namespace)) return Optional.of(new LanternType(blockId, block));

        String path = blockId.getPath();
        if (namespace.equals("twigs") && path.contains("paper_lantern")) {
            return Optional.of(new LanternType(blockId, block));
        }
        if (namespace.equals("windswept") && path.equals("ice_lantern")) {
            return Optional.of(new LanternType(blockId, block));
        }

        if (block instanceof LanternBlock && !block.defaultBlockState().hasBlockEntity()) {
            return Optional.of(new LanternType(blockId, block));
        }

        return Optional.empty();
    }

    public static class LanternType extends BlockType {
        public final Block lantern;

        public LanternType(ResourceLocation name, Block lantern) {
            super(name);
            this.lantern = lantern;
        }

        @Override
        public String getTranslationKey() {
            return Utils.getID(lantern).getPath();
        }

        @Override
        public ItemLike mainChild() {
            return lantern;
        }

        @Override
        protected void initializeChildrenBlocks() {
            this.addChild("lantern", this.lantern);
        }

        @Override
        protected void initializeChildrenItems() {
        }
    }
}
