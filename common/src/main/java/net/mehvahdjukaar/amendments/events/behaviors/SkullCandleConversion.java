package net.mehvahdjukaar.amendments.events.behaviors;

import net.mehvahdjukaar.amendments.common.tile.CandleSkullBlockTile;
import net.mehvahdjukaar.amendments.configs.CommonConfigs;
import net.mehvahdjukaar.amendments.integration.CompatHandler;
import net.mehvahdjukaar.amendments.integration.CompatObjects;
import net.mehvahdjukaar.amendments.reg.ModRegistry;
import net.mehvahdjukaar.moonlight.api.util.Utils;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.SkullBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

public class SkullCandleConversion implements ItemUseOnBlock {

    @Override
    public boolean altersWorld() {
        return true;
    }

    @Override
    public boolean placesBlock() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return CommonConfigs.SKULL_CANDLES.get();
    }

    @Override
    public boolean appliesToItem(Item item) {
        return isCorrectCandle(item);
    }

    public static boolean isCorrectCandle(Item item) {
        if (item instanceof BlockItem bi && bi.getBlock() instanceof CandleBlock) {
            var n = Utils.getID(item).getNamespace();
            return (n.equals("minecraft") || n.equals("tinted") ||
                    item == CompatObjects.SOUL_CANDLE_ITEM.get() ||
                    item == CompatObjects.SPECTACLE_CANDLE_ITEM.get());
        }
        return false;
    }

    @Override
    public InteractionResult tryPerformingAction(Level world, Player player, InteractionHand hand,
                                                 ItemStack stack, BlockHitResult hit) {
        BlockPos pos = hit.getBlockPos();

        if (world.getBlockEntity(pos) instanceof SkullBlockEntity oldTile) {
            BlockState state = oldTile.getBlockState();
            if ((state.getBlock() instanceof AbstractSkullBlock skullBlock && skullBlock.getType() != SkullBlock.Types.DRAGON)) {

                ItemStack copy = stack.copy();

                Block b;
                if (skullBlock instanceof WallSkullBlock) {
                    if (CompatHandler.BUZZIER_BEES && stack.getItem() == CompatObjects.SOUL_CANDLE_ITEM.get()) {
                        b = ModRegistry.SKULL_CANDLE_SOUL_WALL.get();
                    } else b = ModRegistry.SKULL_CANDLE_WALL.get();
                } else {
                    if (CompatHandler.BUZZIER_BEES && stack.getItem() == CompatObjects.SOUL_CANDLE_ITEM.get()) {
                        b = ModRegistry.SKULL_CANDLE_SOUL.get();
                    } else b = ModRegistry.SKULL_CANDLE.get();
                }

                InteractionResult result = InteractEvents.replaceSimilarBlock(b,
                        player, stack, pos, world, state, SoundType.CANDLE,
                        true, SkullBlock.ROTATION, WallSkullBlock.FACING);

                if (result.consumesAction()) {
                    if (world.getBlockEntity(pos) instanceof CandleSkullBlockTile tile) {
                        tile.initialize(oldTile, copy, player, hand);
                    }
                }
                return result;
            }
        }
        return InteractionResult.PASS;
    }

}

