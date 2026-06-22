package net.mehvahdjukaar.amendments.events;

import net.mehvahdjukaar.amendments.configs.CommonConfigs;
import net.mehvahdjukaar.amendments.events.behaviors.InteractEvents;
import net.mehvahdjukaar.amendments.integration.CompatHandler;
import net.mehvahdjukaar.amendments.integration.SoulFiredCompat;
import net.mehvahdjukaar.amendments.reg.ModTags;
import net.mehvahdjukaar.moonlight.api.block.ILightable;
import net.mehvahdjukaar.moonlight.api.misc.EventCalled;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ModEvents {


    //block placement should stay low in priority to allow other more important mod interaction that use the event
    @EventCalled
    public static InteractionResult onRightClickBlock(Player player, Level level, InteractionHand hand, BlockHitResult hitResult) {
        if (!player.isSpectator()) { //is this check even needed?
            return InteractEvents.onItemUsedOnBlock(player, level,
                    player.getItemInHand(hand), hand, hitResult);
        }
        return InteractionResult.PASS;
    }

    @EventCalled
    public static InteractionResult onRightClickBlockHP(Player player, Level level, InteractionHand hand, BlockHitResult hitResult) {
        if (!player.isSpectator()) {
            return InteractEvents.onItemUsedOnBlockHP(player, level,
                    player.getItemInHand(hand), hand, hitResult);
        }
        return InteractionResult.PASS;
    }

    @EventCalled
    public static InteractionResultHolder<ItemStack> onUseItem(Player player, Level level, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!player.isSpectator()) {
            return InteractEvents.onItemUseLP(player, level, hand, stack);
        }
        return InteractionResultHolder.pass(stack);
    }

    public static InteractionResult onAttackEntity(Player player, Level level, InteractionHand hand,
                                                   Entity target, @Nullable EntityHitResult entityHitResult) {
        var ret = torchEntity(player, level, target, player.getItemInHand(hand));
        if (ret.consumesAction()) return ret;
        if (CommonConfigs.TORCH_FIRE_OFFHAND.get()) {
            InteractionHand other = hand == InteractionHand.MAIN_HAND ? InteractionHand.OFF_HAND : InteractionHand.MAIN_HAND;
            ret = torchEntity(player, level, target, player.getItemInHand(other));
            if (ret.consumesAction()) return ret;
        }
        return InteractionResult.PASS;
    }

    private static @NotNull InteractionResult torchEntity(Player player, Level level, Entity target, ItemStack stack) {
        if (!stack.is(ModTags.SET_ENTITY_ON_FIRE) || !CommonConfigs.TORCH_FIRE.get()) {
            return InteractionResult.PASS;
        }
        if (level.isClientSide() || !(target instanceof LivingEntity living)
                || !target.isAttackable() || target.skipAttackInteraction(player)) {
            return InteractionResult.PASS;
        }
        int seconds = CommonConfigs.TORCH_FIRE_DURATION.get();
        int ticks = seconds * 20;
        if (CompatHandler.SOUL_FIRED) {
            SoulFiredCompat.setSecondsOnFire(living, seconds, stack);
        } else {
            living.setRemainingFireTicks(Math.max(living.getRemainingFireTicks(), ticks));
        }
        if (stack.is(ILightable.FLINT_AND_STEELS)) {
            target.playSound(SoundEvents.FLINTANDSTEEL_USE, 1.0F, level.getRandom().nextFloat() * 0.4F + 0.8F);
        } else {
            target.playSound(SoundEvents.FIRECHARGE_USE, 0.5F, 1.3F + level.getRandom().nextFloat() * 0.2F);
        }
        return InteractionResult.PASS;
    }
}