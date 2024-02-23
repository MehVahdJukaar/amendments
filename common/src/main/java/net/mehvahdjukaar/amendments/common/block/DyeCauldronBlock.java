package net.mehvahdjukaar.amendments.common.block;

import net.mehvahdjukaar.amendments.common.LiquidMixer;
import net.mehvahdjukaar.amendments.common.item.DyeBottleItem;
import net.mehvahdjukaar.amendments.common.recipe.RecipeUtils;
import net.mehvahdjukaar.amendments.common.tile.LiquidCauldronBlockTile;
import net.mehvahdjukaar.amendments.configs.CommonConfigs;
import net.mehvahdjukaar.amendments.reg.ModRegistry;
import net.mehvahdjukaar.moonlight.api.fluids.SoftFluidStack;
import net.mehvahdjukaar.moonlight.api.fluids.SoftFluidTank;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.animal.Sheep;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.DyeItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.phys.BlockHitResult;

public class DyeCauldronBlock extends ModCauldronBlock {
    public static final IntegerProperty LEVEL = BlockStateProperties.LEVEL_CAULDRON;

    public DyeCauldronBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.getStateDefinition().any().setValue(LEVEL, 1));
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
    public boolean isFull(BlockState state) {
        return state.getValue(LEVEL) == 3;
    }

    @Override
    protected boolean canReceiveStalactiteDrip(Fluid fluid) {
        return false;
    }

    @Override
    protected double getContentHeight(BlockState state) {
        return (6.0 + (double) state.getValue(LEVEL) * 3.0) / 16.0;
    }

    @Override
    protected void receiveStalactiteDrip(BlockState state, Level level, BlockPos pos, Fluid fluid) {
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        if (level.getBlockEntity(pos) instanceof LiquidCauldronBlockTile te) {
            if (te.handleInteraction(player, hand)) {
                return InteractionResult.sidedSuccess(level.isClientSide);
            }
            if (!CommonConfigs.CAULDRON_CRAFTING.get()) return InteractionResult.PASS;

            SoftFluidTank tank = te.getSoftFluidTank();
            SoftFluidStack fluid = tank.getFluid();

            if (fluid.is(ModRegistry.DYE_SOFT_FLUID.get())) {
                ItemStack stack = player.getItemInHand(hand);
                //always allows adding dye. they dont add water
                if (stack.getItem() instanceof DyeItem di) {
                    return addDye(level, te, stack, player, di);
                }

                //try recoloring
                var crafted = RecipeUtils.craftWithFluidAndDye(level, tank.getFluid(), stack);
                if (crafted != null) {
                    this.doCraftItem(level, pos, player, hand, te, fluid, stack, crafted.getFirst(), crafted.getSecond(),
                            CommonConfigs.DYE_RECIPES_PER_LAYER.get());
                    return InteractionResult.sidedSuccess(level.isClientSide);
                }
            }
        }
        return InteractionResult.PASS;
    }

    @Override
    protected void handleEntityInside(BlockState state, Level level, BlockPos pos, Entity entity) {
        if (entity instanceof Sheep sheep && level.getBlockEntity(pos) instanceof LiquidCauldronBlockTile te) {
            SoftFluidStack fluid = te.getSoftFluidTank().getFluid();
            if (fluid.is(ModRegistry.DYE_SOFT_FLUID.get())) {
                DyeColor dye = DyeBottleItem.getClosestDye(fluid);
                if (sheep.getColor() != dye) {
                    sheep.setColor(dye);
                    te.consumeOneLayer();
                }
            }
        }
    }

    @Override
    public BlockState updateStateOnFluidChange(BlockState state, Level level, BlockPos pos, SoftFluidStack fluid) {
        int height = fluid.getCount();
        if (fluid.isEmpty()) {
            state = Blocks.CAULDRON.defaultBlockState();
        } else {
            state = state.setValue(DyeCauldronBlock.LEVEL, height);
        }
        return state;
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

    public static InteractionResult addDye(Level level, LiquidCauldronBlockTile tile, ItemStack stack, Player player, DyeItem dyeItem) {
        SoftFluidStack fluid = tile.getSoftFluidTank().getFluid();
        if (!level.isClientSide()) {
            int count = fluid.getCount();
            if (count == 3) fluid.setCount(2); //hack!!
            SoftFluidStack dummyStack = DyeBottleItem.toFluidStack(dyeItem.getDyeColor(), 1);

            LiquidMixer.mixDye(fluid, dummyStack);

            fluid.setCount(count);
            tile.setChanged();
        }

        playDyeSoundAndConsume(tile.getBlockState(), tile.getBlockPos(), level, player, stack);

        return InteractionResult.sidedSuccess(level.isClientSide);
    }
}
