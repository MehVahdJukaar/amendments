package net.mehvahdjukaar.amendments.events.behaviors;

import net.mehvahdjukaar.amendments.common.ExtendedHangingSign;
import net.mehvahdjukaar.amendments.common.tile.HangingSignTileExtension;
import net.mehvahdjukaar.amendments.configs.CommonConfigs;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
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
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.BlockHitResult;

public class HangingSignDisplayItem implements BlockUse {

    // just here because im too lazy to move to static class. we arent using the block use anymore
    public static final HangingSignDisplayItem INSTANCE = new HangingSignDisplayItem();

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
        if (!isEnabled()) return InteractionResult.PASS;
        if (level.getBlockEntity(pos) instanceof ExtendedHangingSign e) {
            SignBlockEntity be = ((SignBlockEntity) e);
            HangingSignTileExtension ext = e.amendments$getExtension();
            if (!be.isWaxed()) {
                boolean front = be.isFacingFrontText(player);

                ItemStack item = front ? ext.getFrontItem() : ext.getBackItem();
                if (!stack.isEmpty() && !item.isEmpty()) {
                    //prevent editing text
                    return InteractionResult.FAIL;
                }
                if (stack.getItem() instanceof SignApplicator) {
                    if (!player.isSecondaryUseActive()) return InteractionResult.PASS;
                }

                return interactWithFace(state, pos, level, player, hand, stack, be, ext, front);
            }
        }
        return InteractionResult.PASS;
    }

    private static InteractionResult interactWithFace(BlockState state, BlockPos pos, Level level, Player player,
                                                      InteractionHand hand, ItemStack stack, SignBlockEntity be,
                                                      HangingSignTileExtension ext, boolean front) {
        ItemStack tileItem = front ? ext.getFrontItem() : ext.getBackItem();
        boolean hasItem = !tileItem.isEmpty();
        if (!hasItem && !stack.isEmpty()) {
            level.playSound(player, pos, SoundEvents.ITEM_FRAME_ADD_ITEM, SoundSource.BLOCKS, 1.0F,
                    level.random.nextFloat() * 0.1F + 0.95F);

            if (level.isClientSide) return InteractionResult.SUCCESS;

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
            setMessagesAndUpdate(be, front, "item");

            level.gameEvent(GameEvent.BLOCK_CHANGE, pos, GameEvent.Context.of(player, state));
            player.awardStat(Stats.ITEM_USED.get(stack.getItem()));

            return InteractionResult.CONSUME;
        } else if (hasItem && stack.isEmpty()) {
            if (level.isClientSide) return InteractionResult.SUCCESS;

            //remove
            player.setItemInHand(hand, tileItem.split(1));
            setMessagesAndUpdate(be, front, "");

            level.gameEvent(GameEvent.BLOCK_CHANGE, pos, GameEvent.Context.of(player, state));

            return InteractionResult.CONSUME;
        }
        return InteractionResult.PASS;
    }

    //this also updates
    private static void setMessagesAndUpdate(SignBlockEntity sign, boolean front, String s) {
        sign.updateText(signText -> {
            MutableComponent literal = Component.literal(s);
            return signText.setMessage(0, literal)
                    .setMessage(1, literal)
                    .setMessage(2, literal)
                    .setMessage(3, literal);
        }, front);

    }
}
