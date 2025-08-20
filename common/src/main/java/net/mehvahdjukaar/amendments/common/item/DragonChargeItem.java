package net.mehvahdjukaar.amendments.common.item;

import net.mehvahdjukaar.amendments.common.ProjectileStats;
import net.mehvahdjukaar.amendments.common.entity.MediumDragonFireball;
import net.mehvahdjukaar.amendments.common.entity.MediumFireball;
import net.mehvahdjukaar.amendments.configs.CommonConfigs;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Position;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ProjectileItem;
import net.minecraft.world.level.Level;

public class DragonChargeItem extends Item implements ProjectileItem {
    public DragonChargeItem(Properties properties) {
        super(properties);
    }


    @Override
    public Projectile asProjectile(Level level, Position pos, ItemStack stack, Direction direction) {
        MediumFireball snowball = new MediumFireball(level, pos.x(), pos.y(), pos.z());
        snowball.setItem(stack);
        return snowball;
    }

    @Override
    public ProjectileItem.DispenseConfig createDispenseConfig() {
        return ProjectileStats.DISPENSER_CONFIG;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand usedHand) {
        ItemStack itemStack = player.getItemInHand(usedHand);
        //   this.playSound(level, blockPos);
        //same as in ThrowableProjectile

        level.playSound(null, player.getX(), player.getEyeY() - 0.1, player.getZ(), SoundEvents.SNOWBALL_THROW, SoundSource.NEUTRAL, 0.5F, 0.4F / (level.getRandom().nextFloat() * 0.4F + 0.8F));
        player.getCooldowns().addCooldown(this, CommonConfigs.CHARGES_COOLDOWN.get());
        if (!level.isClientSide) {
            MediumDragonFireball snowball = new MediumDragonFireball(level, player);
            snowball.shootFromRotation(player, player.getXRot(), player.getYRot(), 0.0F,
                    ProjectileStats.THROWN_SPEED, 1.0F);
            level.addFreshEntity(snowball);
        }

        player.awardStat(Stats.ITEM_USED.get(this));
        if (!player.getAbilities().instabuild) {
            itemStack.shrink(1);
        }

        return InteractionResultHolder.sidedSuccess(itemStack, level.isClientSide());
    }

    //TODO: custom throw sounds to wind charges too
    private void playSound(Level level, BlockPos pos) {
        RandomSource randomSource = level.getRandom();
        level.playSound(null, pos, SoundEvents.FIRECHARGE_USE, SoundSource.BLOCKS, 1.0F, (randomSource.nextFloat() - randomSource.nextFloat()) * 0.2F + 1.0F);
    }
}
