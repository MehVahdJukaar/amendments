package net.mehvahdjukaar.amendments.common;

import net.mehvahdjukaar.moonlight.api.set.BlockType;
import net.mehvahdjukaar.moonlight.api.set.BlockTypeRegistry;
import net.mehvahdjukaar.moonlight.api.util.Utils;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.EmptyBlockGetter;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LanternBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.VoxelShape;

import java.util.*;

public class LanternRegistry extends BlockTypeRegistry<LanternRegistry.LanternType> {

    public static final LanternRegistry INSTANCE = new LanternRegistry();

    /**
     * Manual vertical corrections (block units, {@code 1/16} = one pixel; positive pushes down) for
     * modded lanterns whose collision shape doesn't line up with their visual model. Added on top of
     * the shape-derived offset in {@link LanternType#computeAttachmentOffset}. Must be initialized
     * before the static {@link #VANILLA}/{@link #SOUL} types below, which read it during construction.
     */
    private static final Map<ResourceLocation, Double> SPECIAL_OFFSETS = new HashMap<>();

    static {
        // Caverns & Chasms copper lanterns (WeatheringCopperLanternBlock) use a taller collision box
        // than a vanilla lantern, so the shape-derived offset drops them 1px below the wall mount,
        // leaving a gap. Raise them back up. Only these 8 share that model - cupric_lantern is a plain
        // vanilla-shaped LanternBlock (no correction) and golden_lantern isn't a hanging lantern.
        for (String path : List.of(
                "copper_lantern", "exposed_copper_lantern", "weathered_copper_lantern",
                "oxidized_copper_lantern", "waxed_copper_lantern", "waxed_exposed_copper_lantern",
                "waxed_weathered_copper_lantern", "waxed_oxidized_copper_lantern")) {
            SPECIAL_OFFSETS.put(ResourceLocation.fromNamespaceAndPath("caverns_and_chasms", path), -1 / 16d);
        }
    }

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
        /**
         * How far the dangling wall lantern sits below its mount, in block units. Derived once from
         * the lantern's shape (plus any manual {@link #SPECIAL_OFFSETS} correction) since it never
         * changes for a given lantern - see {@link #computeAttachmentOffset}.
         */
        public final double attachmentOffset;

        public LanternType(ResourceLocation name, Block lantern) {
            super(name);
            this.lantern = lantern;
            this.attachmentOffset = computeAttachmentOffset(name, lantern);
        }

        private static double computeAttachmentOffset(ResourceLocation id, Block lantern) {
            // Paper (twigs) lanterns keep the default mount height.
            if (id.getNamespace().equals("twigs")) return 0;
            try {
                BlockState state = lantern.defaultBlockState();
                // Match the (non-hanging) state the wall lantern actually renders with.
                if (state.hasProperty(LanternBlock.HANGING)) {
                    state = state.setValue(LanternBlock.HANGING, false);
                }
                VoxelShape shape = state.getShape(EmptyBlockGetter.INSTANCE, BlockPos.ZERO);
                double manual = SPECIAL_OFFSETS.getOrDefault(id, 0d);
                if (shape.isEmpty()) return manual;
                return shape.bounds().maxY - (9 / 16d) + manual;
            } catch (Exception e) {
                // A modded lantern with a level-dependent shape may not tolerate an empty getter.
                return SPECIAL_OFFSETS.getOrDefault(id, 0d);
            }
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
