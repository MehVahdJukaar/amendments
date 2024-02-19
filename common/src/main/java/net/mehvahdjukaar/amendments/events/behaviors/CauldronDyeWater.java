package net.mehvahdjukaar.amendments.events.behaviors;

import net.mehvahdjukaar.amendments.common.block.DyeCauldronBlock;
import net.mehvahdjukaar.amendments.common.item.DyeBottleItem;
import net.mehvahdjukaar.amendments.common.tile.LiquidCauldronBlockTile;
import net.mehvahdjukaar.amendments.configs.CommonConfigs;
import net.mehvahdjukaar.amendments.reg.ModRegistry;
import net.mehvahdjukaar.moonlight.api.fluids.SoftFluidStack;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DyeItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LayeredCauldronBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.BlockHitResult;

import static net.mehvahdjukaar.amendments.common.tile.LiquidCauldronBlockTile.mixDye;

public class CauldronDyeWater implements BlockUse {

    //block use as it has way too many items that could trigger

    @Override
    public boolean isEnabled() {
        return CommonConfigs.DYE_WATER.get();
    }

    @Override
    public boolean appliesToBlock(Block block) {
        return Blocks.WATER_CAULDRON == block;
    }

    @Override
    public InteractionResult tryPerformingAction(BlockState state, BlockPos pos, Level level, Player player, InteractionHand hand, ItemStack stack, BlockHitResult hit) {
        if (state.is(Blocks.WATER_CAULDRON)) {
            if (stack.getItem() instanceof DyeItem dye) {
                Integer l = state.getValue(LayeredCauldronBlock.LEVEL);
                level.setBlockAndUpdate(pos, ModRegistry.DYE_CAULDRON.get().defaultBlockState().setValue(DyeCauldronBlock.LEVEL, l));
                if (level.getBlockEntity(pos) instanceof LiquidCauldronBlockTile te) {
                    te.getSoftFluidTank().setFluid(DyeBottleItem.toFluidStack(dye.getDyeColor(), l));
                }

                playDyeUseSound(state, pos, level, player, stack);

                return InteractionResult.sidedSuccess(level.isClientSide);
            }
        }
        return InteractionResult.PASS;
    }

    private static void playDyeUseSound(BlockState state, BlockPos pos, Level level, Player player, ItemStack stack) {
        if (player instanceof ServerPlayer serverPlayer) {
            level.gameEvent(GameEvent.BLOCK_CHANGE, pos, GameEvent.Context.of(player, state));
            player.awardStat(Stats.ITEM_USED.get(stack.getItem()));
            CriteriaTriggers.ITEM_USED_ON_BLOCK.trigger(serverPlayer, pos, stack);
        }

        level.playSound(player, pos, SoundEvents.DYE_USE, SoundSource.BLOCKS, 1.0F, 1.0F);
        level.playSound(player, pos, SoundEvents.BUCKET_EMPTY, SoundSource.BLOCKS, 1.0F, 1.3f);


        if (!player.isCreative()) {
            stack.shrink(1);
        }
    }

    public static InteractionResult addDyeToCauldron(Level level, LiquidCauldronBlockTile tile, ItemStack stack,
                                                     Player player, DyeItem dyeItem) {
        SoftFluidStack fluid = tile.getSoftFluidTank().getFluid();
        if (!level.isClientSide()) {
            int count = fluid.getCount();
            if (count == 3) fluid.setCount(2); //hack!!
            SoftFluidStack dummyStack = DyeBottleItem.toFluidStack(dyeItem.getDyeColor(), 1);

            mixDye(fluid, dummyStack);

            fluid.setCount(count);
            tile.setChanged();
        }

        playDyeUseSound(tile.getBlockState(), tile.getBlockPos(), level, player, stack);

        return InteractionResult.sidedSuccess(level.isClientSide);
    }

}
