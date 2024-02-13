package net.mehvahdjukaar.amendments.events.behaviors;

import net.mehvahdjukaar.amendments.common.ExtendedHangingSign;
import net.mehvahdjukaar.amendments.common.tile.HangingSignTileExtension;
import net.mehvahdjukaar.amendments.configs.CommonConfigs;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SignApplicator;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.CeilingHangingSignBlock;
import net.minecraft.world.level.block.WallHangingSignBlock;
import net.minecraft.world.level.block.entity.SignBlockEntity;
import net.minecraft.world.level.block.entity.SignText;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;

public class HangingSignDisplayItem implements BlockUse {

    @Override
    public boolean isEnabled() {
        return CommonConfigs.HANGING_SIGN_ITEM.get();
    }

    @Override
    public boolean appliesToBlock(Block block) {
        return block instanceof CeilingHangingSignBlock || block instanceof WallHangingSignBlock;
    }

    @Override
    public InteractionResult tryPerformingAction(BlockState state, BlockPos pos, Level level, Player player, InteractionHand hand, ItemStack stack, BlockHitResult hit) {
        if (!level.isClientSide && level.getBlockEntity(pos) instanceof ExtendedHangingSign e) {
            SignBlockEntity be = ((SignBlockEntity) e);
            HangingSignTileExtension ext = e.getExtension();
            if (!be.isWaxed()) {
                if (stack.getItem() instanceof SignApplicator && !player.isSecondaryUseActive()) {
                    return InteractionResult.PASS;
                }
                boolean front = be.isFacingFrontText(player);
                return interactWithFace(state, pos, level, player, hand, stack, be, ext, front);
            }
        }
        return InteractionResult.PASS;
    }

    private static InteractionResult interactWithFace(BlockState state, BlockPos pos, Level level, Player player,
                                                      InteractionHand hand, ItemStack stack, SignBlockEntity be,
                                                      HangingSignTileExtension ext, boolean front) {
        ItemStack tileItem = front ? ext.getFrontItem() : ext.getBackItem();
        SignText text = front ? be.getFrontText() : be.getBackText();
        boolean hasItem = !tileItem.isEmpty();
        if (!hasItem && !stack.isEmpty()) {
            //add
            if (front) {
                ext.setFrontItem(stack.copyWithCount(1));
            } else {
                ext.setBackItem(stack.copyWithCount(1));
            }
            if (!player.isCreative()) {
                stack.shrink(1);
            }
            //hack so we can set dye
            setDummyMessage(text);
            level.gameEvent(GameEvent.BLOCK_CHANGE, pos, GameEvent.Context.of(player, state));
            player.awardStat(Stats.ITEM_USED.get(stack.getItem()));
            return InteractionResult.CONSUME;
        } else if (hasItem && stack.isEmpty()) {
            //remove
            player.setItemInHand(hand, tileItem.split(1));
            clearAllMessages(text);
            level.gameEvent(GameEvent.BLOCK_CHANGE, pos, GameEvent.Context.of(player, state));

            return InteractionResult.CONSUME;
        }
        return InteractionResult.PASS;
    }

    private static void setDummyMessage(SignText be) {
        setAllMessages(be, "item");
    }

    private static void clearAllMessages(SignText be) {
        setAllMessages(be, "");
    }

    private static void setAllMessages(SignText frontText, String s) {
        MutableComponent literal = Component.literal(s);
        frontText.setMessage(0, literal);
        frontText.setMessage(1, literal);
        frontText.setMessage(2, literal);
        frontText.setMessage(3, literal);
    }
}
