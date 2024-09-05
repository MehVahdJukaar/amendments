package net.mehvahdjukaar.amendments.common.tile;

import net.mehvahdjukaar.amendments.Amendments;
import net.mehvahdjukaar.amendments.common.ISwingingTile;
import net.mehvahdjukaar.amendments.common.PendulumAnimation;
import net.mehvahdjukaar.amendments.common.SwingAnimation;
import net.mehvahdjukaar.amendments.configs.ClientConfigs;
import net.mehvahdjukaar.amendments.reg.ModBlockProperties;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.CeilingHangingSignBlock;
import net.minecraft.world.level.block.WallHangingSignBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.RotationSegment;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;

public class HangingSignTileExtension {

    @Nullable
    private ModBlockProperties.PostType leftAttachment = null;

    @Nullable
    private ModBlockProperties.PostType rightAttachment = null;

    private final boolean isCeiling;

    private boolean canSwing = true;

    private SwingAnimation animation;

    //no item handler here. we don't want hoppers and such anyway
    private ItemStack frontItem = ItemStack.EMPTY;
    private ItemStack backItem = ItemStack.EMPTY;

    public HangingSignTileExtension(BlockState state) {
        super();
        isCeiling = state.getBlock() instanceof CeilingHangingSignBlock;
    }

    public void clientTick(Level level, BlockPos pos, BlockState state) {
        var animation = getClientAnimation();
        if (!canSwing) {
            animation.reset();
        } else {
            animation.tick(level, pos, state);
        }
    }

    private Vector3f getRotationAxis(BlockState state) {
        return state.hasProperty(WallHangingSignBlock.FACING) ?
                state.getValue(WallHangingSignBlock.FACING).getClockWise().step() :
                new Vector3f(0, 0, 1).rotateY(Mth.DEG_TO_RAD *
                        (90 + RotationSegment.convertToDegrees(state.getValue(CeilingHangingSignBlock.ROTATION))));
    }


    public ModBlockProperties.PostType getRightAttachment() {
        return rightAttachment;
    }

    public ModBlockProperties.PostType getLeftAttachment() {
        return leftAttachment;
    }

    public void saveAdditional(CompoundTag tag) {
        if (!isCeiling) {
            if (leftAttachment != null) {
                tag.putByte("left_attachment", (byte) leftAttachment.ordinal());
            }
            if (rightAttachment != null) {
                tag.putByte("right_attachment", (byte) rightAttachment.ordinal());
            }
        }
        if (!canSwing) {
            tag.putBoolean("can_swing", false);
        }
        if (!frontItem.isEmpty()) {
            tag.put("front_item", frontItem.save(new CompoundTag()));
        }
        if (!backItem.isEmpty()) {
            tag.put("back_item", backItem.save(new CompoundTag()));
        }
    }

    public void load(CompoundTag tag) {
        if (!isCeiling) {
            if (tag.contains("left_attachment")) {
                leftAttachment = ModBlockProperties.PostType.values()[tag.getByte("left_attachment")];
            }
            if (tag.contains("right_attachment")) {
                rightAttachment = ModBlockProperties.PostType.values()[tag.getByte("right_attachment")];
            }
        }
        if (tag.contains("can_swing")) {
            canSwing = tag.getBoolean("can_swing");
        } else canSwing = true;
        if (tag.contains("front_item")) {
            this.setFrontItem(ItemStack.of(tag.getCompound("front_item")));
        } else setFrontItem(ItemStack.EMPTY);
        if (tag.contains("back_item")) {
            this.setBackItem(ItemStack.of(tag.getCompound("back_item")));
        } else setBackItem(ItemStack.EMPTY);
    }


    //just called by wall hanging sign
    public void updateShape(BlockState state, Direction direction, BlockState neighborState, LevelAccessor level,
                            BlockPos pos, BlockPos neighborPos) {

        if (!isCeiling) {
            Direction selfFacing = state.getValue(WallHangingSignBlock.FACING);
            if (direction == selfFacing.getClockWise()) {
                rightAttachment = ModBlockProperties.PostType.get(neighborState, true);
                if (level instanceof Level l)
                    l.sendBlockUpdated(pos, state, state, Block.UPDATE_CLIENTS);
            } else if (direction == selfFacing.getCounterClockWise()) {
                leftAttachment = ModBlockProperties.PostType.get(neighborState, true);
                if (level instanceof Level l)
                    l.sendBlockUpdated(pos, state, state, Block.UPDATE_CLIENTS);
            }
        }
        if (direction == Direction.DOWN) {
            updateCanSwing(state, neighborState, level, pos);
        }
    }

    private void updateCanSwing(BlockState state, BlockState neighborState, LevelAccessor level, BlockPos pos) {
        canSwing = isCeiling ? !state.getValue(CeilingHangingSignBlock.ATTACHED) :
                !Amendments.canConnectDown(neighborState, level, pos);
    }

    public void updateAttachments(Level level, BlockPos pos, BlockState state) {
        if (!isCeiling) {
            Direction selfFacing = state.getValue(WallHangingSignBlock.FACING);

            rightAttachment = ModBlockProperties.PostType.get(level.getBlockState(pos.relative(selfFacing.getClockWise())), true);
            leftAttachment = ModBlockProperties.PostType.get(level.getBlockState(pos.relative(selfFacing.getCounterClockWise())), true);
        }
        BlockState below = level.getBlockState(pos.below());
        updateCanSwing(state, below, level, pos);

    }

    public boolean canSwing() {
        return canSwing;
    }

    public void setFrontItem(ItemStack frontItem) {
        this.frontItem = frontItem;
    }

    public ItemStack getFrontItem() {
        return frontItem;
    }

    public void setBackItem(ItemStack backItem) {
        this.backItem = backItem;
    }

    public ItemStack getBackItem() {
        return backItem;
    }

    // Just call from client
    public SwingAnimation getClientAnimation() {
        if (!ClientConfigs.SWINGING_SIGNS.get()) return SwingAnimation.EMPTY;
        if (animation == null) {
            animation = new PendulumAnimation(ClientConfigs.HANGING_SIGN_CONFIG,
                    this::getRotationAxis);
        }
        return animation;
    }
}
