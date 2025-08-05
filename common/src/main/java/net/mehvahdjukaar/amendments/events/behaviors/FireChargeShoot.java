package net.mehvahdjukaar.amendments.events.behaviors;

import net.mehvahdjukaar.amendments.common.ProjectileStats;
import net.mehvahdjukaar.amendments.common.entity.MediumFireball;
import net.mehvahdjukaar.amendments.configs.CommonConfigs;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.FireChargeItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;

public class FireChargeShoot implements ItemUse{

    @Override
    public boolean isEnabled() {
        return CommonConfigs.THROWABLE_FIRE_CHARGES.get();
    }

    @Override
    public boolean appliesToItem(Item item) {
        return item == Items.FIRE_CHARGE;
    }

    @Override
    public InteractionResult tryPerformingAction(Level level, Player player, InteractionHand usedHand, ItemStack stack, BlockHitResult hit) {
        ItemStack itemStack = player.getItemInHand(usedHand);
        //   this.playSound(level, blockPos);
        //same as in ThrowableProjectile
        level.playSound(null, player.getX(), player.getEyeY() - 0.1, player.getZ(), SoundEvents.FIRECHARGE_USE, SoundSource.NEUTRAL, 0.5F, 0.6f+ (0.4F / (level.getRandom().nextFloat() * 0.4F + 0.8F)));
        if (!level.isClientSide) {
            MediumFireball ball = new MediumFireball(level, player);
            ball.shootFromRotation(player, player.getXRot(), player.getYRot(), 0.0F, ProjectileStats.THROWN_SPEED, 1f);
            level.addFreshEntity(ball);
        }

        player.awardStat(Stats.ITEM_USED.get(stack.getItem()));
        if (!player.getAbilities().instabuild) {
            itemStack.shrink(1);
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }

}
