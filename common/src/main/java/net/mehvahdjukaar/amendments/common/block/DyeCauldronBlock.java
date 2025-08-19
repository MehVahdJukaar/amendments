package net.mehvahdjukaar.amendments.common.block;

import net.mehvahdjukaar.amendments.common.item.DyeBottleItem;
import net.mehvahdjukaar.amendments.common.tile.LiquidCauldronBlockTile;
import net.mehvahdjukaar.amendments.reg.ModRegistry;
import net.mehvahdjukaar.moonlight.api.fluids.SoftFluidStack;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.animal.Sheep;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.material.Fluid;

//split mainly needed since on forge we need a 4 layer cauldron
public class DyeCauldronBlock extends ModCauldronBlock {
    public static final IntegerProperty LEVEL = BlockStateProperties.LEVEL_CAULDRON;

    public DyeCauldronBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.defaultBlockState().setValue(BOILING, false).setValue(LEVEL, 1));
    }

    @Override
    public IntegerProperty getLevelProperty() {
        return LEVEL;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(LEVEL);
    }

    @Override
    protected boolean canReceiveStalactiteDrip(Fluid fluid) {
        return false;
    }

    @Override
    protected void receiveStalactiteDrip(BlockState state, Level level, BlockPos pos, Fluid fluid) {
    }

    @Override
    protected void handleEntityInsideFluidSpecial(BlockState state, Level level, BlockPos pos, Entity entity) {
        if (entity instanceof Sheep sheep && level.getBlockEntity(pos) instanceof LiquidCauldronBlockTile te) {
            SoftFluidStack fluid = te.getSoftFluidTank().getFluid();
            if (fluid.is(ModRegistry.DYE_SOFT_FLUID)) {
                DyeColor dye = DyeBottleItem.getClosestDye(fluid);
                if (sheep.getColor() != dye) {
                    sheep.setColor(dye);
                    te.consumeOneLayer();
                    level.gameEvent(entity, GameEvent.BLOCK_CHANGE, pos);
                }
            }
        }
    }

    public static void playDyeSoundAndConsume(BlockState state, BlockPos pos, Level level, Player player, ItemStack stack) {
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


}
