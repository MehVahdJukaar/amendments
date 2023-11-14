package net.mehvahdjukaar.amendments.common.item.behaviors;

import net.mehvahdjukaar.amendments.configs.CommonConfigs;
import net.mehvahdjukaar.amendments.integration.CompatHandler;
import net.mehvahdjukaar.amendments.integration.SuppCompat;
import net.mehvahdjukaar.moonlight.api.util.Utils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BellBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.ChainBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BellAttachType;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

import java.util.function.Predicate;

class BellChainBehavior implements BlockUseOverride {

    @Override
    public boolean isEnabled() {
        return CommonConfigs.BELL_CHAIN_RINGING.get();
    }

    @Override
    public boolean appliesToBlock(Block block) {
        return block instanceof ChainBlock || (CompatHandler.SUPPLEMENTARIES && SuppCompat.isRope(block));
    }

    @Override
    public InteractionResult tryPerformingAction(BlockState state, BlockPos pos, Level world, Player player,
                                                 InteractionHand hand, ItemStack stack, BlockHitResult hit) {
        //bell chains
        if (stack.isEmpty() && hand == InteractionHand.MAIN_HAND) {
            if (findAndRingBell(world, pos, player, 0, s -> s.getBlock() instanceof ChainBlock && s.getValue(ChainBlock.AXIS) == Direction.Axis.Y)) {
                return InteractionResult.sidedSuccess(world.isClientSide);
            }
            return InteractionResult.sidedSuccess(world.isClientSide);
        }
        return InteractionResult.PASS;
    }

    public static boolean findAndRingBell(Level world, BlockPos pos, Player player, int it, Predicate<BlockState> predicate) {

        if (it > CommonConfigs.BELL_CHAIN_LENGTH.get()) return false;
        BlockState state = world.getBlockState(pos);
        Block b = state.getBlock();
        if (predicate.test(state)) {
            return findAndRingBell(world, pos.above(), player, it + 1, predicate);
        } else if (b instanceof BellBlock bellBlock && it != 0) {
            Direction d = state.getValue(BellBlock.FACING);
            var att = state.getValue(BellBlock.ATTACHMENT);
            if (att == BellAttachType.SINGLE_WALL || att == BellAttachType.DOUBLE_WALL ||
                    !Utils.getID(b).getNamespace().equals("create")) {
                d = d.getClockWise();
            }
            BlockHitResult hit = new BlockHitResult(new Vec3(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5),
                    d, pos, true);
            return bellBlock.onHit(world, state, hit, player, true);
        }
        return false;
    }

}
