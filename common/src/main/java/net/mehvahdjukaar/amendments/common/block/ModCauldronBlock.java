package net.mehvahdjukaar.amendments.common.block;

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
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.AbstractCauldronBlock;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

public abstract class ModCauldronBlock extends AbstractCauldronBlock implements EntityBlock {

    public ModCauldronBlock(BlockBehaviour.Properties properties) {
        super(properties, Map.of());
    }

    @Override
    public Item asItem() {
        return Items.CAULDRON;
    }

    public abstract IntegerProperty getLevelProperty();

    @Override
    public int getAnalogOutputSignal(BlockState state, Level level, BlockPos pos) {
        return state.getValue(getLevelProperty());
    }

    @Override
    public void handlePrecipitation(BlockState state, Level level, BlockPos pos, Biome.Precipitation precipitation) {
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new LiquidCauldronBlockTile(pos, state);
    }

    @Override
    public void entityInside(BlockState state, Level level, BlockPos pos, Entity entity) {
        if (isEntityInsideContent(state, pos, entity)) {
            entity.wasTouchingWater = true;

            if (level.isClientSide) return;

            boolean hasToLower = false;
            if (entity.isOnFire()) {
                entity.clearFire();
                if (entity.mayInteract(level, pos)) {
                    hasToLower = true;
                }
            }
            hasToLower = handleEntityInside(state, level, pos, entity);

            if (hasToLower) {
                lowerFillLevel(state, level, pos);
            }
        }
    }

    protected abstract boolean handleEntityInside(BlockState state, Level level, BlockPos pos, Entity entity );

    public void doCraftItem(Level level, BlockPos pos, Player player, InteractionHand hand, LiquidCauldronBlockTile te,
                                   SoftFluidStack fluid, ItemStack stack, ItemStack crafted) {

        if (player instanceof ServerPlayer serverPlayer) {
            player.awardStat(Stats.ITEM_USED.get(stack.getItem()));
            CriteriaTriggers.ITEM_USED_ON_BLOCK.trigger(serverPlayer, pos, stack);
        }
        level.playSound(player, pos, SoundEvents.BUCKET_EMPTY, SoundSource.BLOCKS, 1.0F, 1.3f);


        int recolorBonus = CommonConfigs.DYE_WATER_BONUS.get();
        int amountToRecolor = recolorBonus * fluid.getCount();
        amountToRecolor = Math.min(amountToRecolor, stack.getCount());
        crafted.setCount(amountToRecolor);

        fluid.shrink(Mth.ceil(amountToRecolor / (float) recolorBonus));
        te.setChanged();

        stack.shrink(amountToRecolor);

        if (stack.isEmpty()) {
            player.setItemInHand(hand, crafted);
        } else {
            if (!player.getInventory().add(crafted)) {
                player.drop(crafted, false);
            }
        }
    }
}
