package net.mehvahdjukaar.amendments.reg;

import net.mehvahdjukaar.amendments.integration.CompatHandler;
import net.mehvahdjukaar.amendments.integration.SuppCompat;
import net.mehvahdjukaar.moonlight.api.block.MimicBlockTile;
import net.mehvahdjukaar.moonlight.api.client.model.ModelDataKey;
import net.mehvahdjukaar.moonlight.api.client.renderer.DynamicRenderedBlockTile;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.EndRodBlock;
import net.minecraft.world.level.block.WallBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.*;
import org.jetbrains.annotations.Nullable;

import java.util.Locale;

public class ModBlockProperties {

    public static final ModelDataKey<BlockState> MIMIC = MimicBlockTile.MIMIC_KEY;
    public static final ModelDataKey<ItemStack> ITEM = new ModelDataKey<>(ItemStack.class);
    public static final ModelDataKey<Boolean> FANCY = DynamicRenderedBlockTile.IS_FANCY;
    public static final EnumProperty<SignAttachment> SIGN_ATTACHMENT = EnumProperty.create("sign_attachment", SignAttachment.class);
    public static final EnumProperty<BlockAttachment> BLOCK_ATTACHMENT = EnumProperty.create("attachment", BlockAttachment.class);
    public static final IntegerProperty LIGHT_LEVEL = IntegerProperty.create("light_level", 0, 15);
    public static final BooleanProperty SOLID = BooleanProperty.create("solid");
    public static final BooleanProperty BOILING = BooleanProperty.create("boiling");
    public static final IntegerProperty LEVEL_1_4 = IntegerProperty.create("level", 1, 4);

    //for wall lanterns

    public enum PostType implements StringRepresentable {
        POST("post", 4),
        PALISADE("palisade", 6),
        WALL("wall", 8),
        BEAM("beam", 10);

        private final String name;
        private final int width;
        private final float offset;

        PostType(String name, int width) {
            this.name = name;
            this.width = width;
            this.offset = (8 - width / 2f) / 16f;
        }

        public int getWidth() {
            return width;
        }

        public float getOffset() {
            return offset;
        }

        @Override
        public String toString() {
            return this.name;
        }

        @Override
        public String getSerializedName() {
            return this.name;
        }

        public String getName() {
            return this.name;
        }

        @Nullable
        public static PostType get(BlockState state) {
            return get(state, false);
        }

        @Nullable
        public static PostType get(BlockState state, boolean needsFullHeight) {

            PostType type = null;
            //if (state.getBlock().hasTileEntity(state)) return type;
            if (state.is(ModTags.POSTS)) {
                type = PostType.POST;
            } else if (state.is(ModTags.PALISADES)) {
                type = PostType.PALISADE;
            } else if (state.is(ModTags.WALLS)) {
                if ((state.getBlock() instanceof WallBlock) && !state.getValue(WallBlock.UP)) {
                    //ignoring not full height ones. might use hitbox here instead
                    if (needsFullHeight && (state.getValue(WallBlock.NORTH_WALL) == WallSide.LOW ||
                            state.getValue(WallBlock.WEST_WALL) == WallSide.LOW)) return null;
                    type = PostType.PALISADE;
                } else {
                    type = PostType.WALL;
                }
            } else if (state.is(ModTags.BEAMS)) {
                if (state.hasProperty(BlockStateProperties.ATTACHED) && state.getValue(BlockStateProperties.ATTACHED)) {
                    //idk why this was here
                    type = null;
                } else {
                    type = PostType.BEAM;
                }
            }

            return type;
        }
    }


    public enum BlockAttachment implements StringRepresentable {
        BLOCK("block"),
        BEAM("beam"),
        WALL("wall"),
        PALISADE("palisade"),
        POST("post"),
        STICK("stick");

        private final String name;

        BlockAttachment(String name) {
            this.name = name;
        }

        @Override
        public String toString() {
            return this.name;
        }

        @Override
        public String getSerializedName() {
            return this.name;
        }

        public String getName() {
            return this.name;
        }

        @Nullable
        public static BlockAttachment get(BlockState state, BlockPos pos, LevelReader level, Direction facing) {
            if (state.isFaceSturdy(level, pos, facing)) return BLOCK;
            PostType postType = PostType.get(state, true);
            if (postType == null) {
                //case for sticks
                if ((CompatHandler.SUPPLEMENTARIES && SuppCompat.isVerticalStick(state, facing)) ||
                        (state.getBlock() instanceof EndRodBlock &&
                                state.getValue(EndRodBlock.FACING).getAxis() == Direction.Axis.Y)) return STICK;
                return null;
            }
            return switch (postType) {
                case BEAM -> BEAM;
                case WALL -> WALL;
                case PALISADE -> PALISADE;
                case POST -> POST;
            };
        }
    }

    public enum SignAttachment implements StringRepresentable {
        CEILING("ceiling"),
        BLOCK_BLOCK(BlockAttachment.BLOCK, BlockAttachment.BLOCK),
        BLOCK_BEAM(BlockAttachment.BLOCK, BlockAttachment.BEAM),
        BLOCK_WALL(BlockAttachment.BLOCK, BlockAttachment.WALL),
        BLOCK_PALISADE(BlockAttachment.BLOCK, BlockAttachment.PALISADE),
        BLOCK_POST(BlockAttachment.BLOCK, BlockAttachment.POST),


        BEAM_BLOCK(BlockAttachment.BEAM, BlockAttachment.BLOCK),
        BEAM_BEAM(BlockAttachment.BEAM, BlockAttachment.BEAM),
        BEAM_WALL(BlockAttachment.BEAM, BlockAttachment.WALL),
        BEAM_PALISADE(BlockAttachment.BEAM, BlockAttachment.PALISADE),
        BEAM_POST(BlockAttachment.BEAM, BlockAttachment.POST),


        WALL_BLOCK(BlockAttachment.WALL, BlockAttachment.BLOCK),
        WALL_BEAM(BlockAttachment.WALL, BlockAttachment.BEAM),
        WALL_WALL(BlockAttachment.WALL, BlockAttachment.WALL),
        WALL_PALISADE(BlockAttachment.WALL, BlockAttachment.PALISADE),
        WALL_POST(BlockAttachment.WALL, BlockAttachment.POST),


        PALISADE_BLOCK(BlockAttachment.PALISADE, BlockAttachment.BLOCK),
        PALISADE_BEAM(BlockAttachment.PALISADE, BlockAttachment.BEAM),
        PALISADE_WALL(BlockAttachment.PALISADE, BlockAttachment.WALL),
        PALISADE_PALISADE(BlockAttachment.PALISADE, BlockAttachment.PALISADE),
        PALISADE_POST(BlockAttachment.PALISADE, BlockAttachment.POST),


        POST_BLOCK(BlockAttachment.POST, BlockAttachment.BLOCK),
        POST_BEAM(BlockAttachment.POST, BlockAttachment.BEAM),
        POST_WALL(BlockAttachment.POST, BlockAttachment.WALL),
        POST_PALISADE(BlockAttachment.POST, BlockAttachment.PALISADE),
        POST_POST(BlockAttachment.POST, BlockAttachment.POST),

        STICK_BLOCK(BlockAttachment.STICK, BlockAttachment.BLOCK),
        STICK_BEAM(BlockAttachment.STICK, BlockAttachment.BEAM),
        STICK_WALL(BlockAttachment.STICK, BlockAttachment.WALL),
        STICK_PALISADE(BlockAttachment.STICK, BlockAttachment.PALISADE),
        STICK_POST(BlockAttachment.STICK, BlockAttachment.POST),
        STICK_STICK(BlockAttachment.STICK, BlockAttachment.STICK),

        BLOCK_STICK(BlockAttachment.BLOCK, BlockAttachment.STICK),
        BEAM_STICK(BlockAttachment.BEAM, BlockAttachment.STICK),
        WALL_STICK(BlockAttachment.WALL, BlockAttachment.STICK),
        PALISADE_STICK(BlockAttachment.PALISADE, BlockAttachment.STICK),
        POST_STICK(BlockAttachment.POST, BlockAttachment.STICK);

        public final BlockAttachment left;
        public final BlockAttachment right;
        private final String name;

        SignAttachment(BlockAttachment left, BlockAttachment right) {
            this.name = left.name + "_" + right.name;
            this.left = left;
            this.right = right;
        }

        SignAttachment(String name) {
            this.name = name;
            this.left = BlockAttachment.BLOCK;
            this.right = BlockAttachment.BLOCK;
        }

        @Override
        public String toString() {
            return this.name;
        }

        @Override
        public String getSerializedName() {
            return this.name;
        }

        public SignAttachment withAttachment(boolean left, @Nullable BlockAttachment attachment) {
            if (attachment == null) attachment = BlockAttachment.BLOCK;
            String s = left ? attachment.name + "_" + this.right : this.left + "_" + attachment.name;
            return SignAttachment.valueOf(s.toUpperCase(Locale.ROOT));
        }

    }


}
