package net.mehvahdjukaar.amendments.common.item;

import net.mehvahdjukaar.amendments.common.entity.SmallDragonFireball;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Snowball;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class DragonChargeItem extends Item {
    public DragonChargeItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand usedHand) {
        ItemStack itemStack = player.getItemInHand(usedHand);
     //   this.playSound(level, blockPos);
        level.playSound((Player) null, player.getX(), player.getY(), player.getZ(), SoundEvents.SNOWBALL_THROW, SoundSource.NEUTRAL, 0.5F, 0.4F / (level.getRandom().nextFloat() * 0.4F + 0.8F));
        if (!level.isClientSide) {
            SmallDragonFireball snowball = new SmallDragonFireball(level, player,0,0,0);
            snowball.shootFromRotation(player, player.getXRot(), player.getYRot(), 0.0F, 1.5F, 1.0F);
            level.addFreshEntity(snowball);
        }

        player.awardStat(Stats.ITEM_USED.get(this));
        if (!player.getAbilities().instabuild) {
            itemStack.shrink(1);
        }

        return InteractionResultHolder.sidedSuccess(itemStack, level.isClientSide());
    }

    private void playSound(Level level, BlockPos pos) {
        RandomSource randomSource = level.getRandom();
        level.playSound((Player) null, pos, SoundEvents.FIRECHARGE_USE, SoundSource.BLOCKS, 1.0F, (randomSource.nextFloat() - randomSource.nextFloat()) * 0.2F + 1.0F);
    }
}
