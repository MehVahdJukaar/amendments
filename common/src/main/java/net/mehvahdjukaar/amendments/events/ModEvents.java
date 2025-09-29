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
        //TODO:check
        ItemStack stack = player.getItemInHand(hand);
        if (CommonConfigs.TORCH_FIRE_OFFHAND.get()) {
            ItemStack offHand = hand == InteractionHand.MAIN_HAND ? player.getOffhandItem() : player.getMainHandItem();
            var ret = torchEntity(player, level, target, offHand);
            if (ret.consumesAction()) return ret;
        }
        return torchEntity(player, level, target, stack);
    }

    private static @NotNull InteractionResult torchEntity(Player player, Level level, Entity target, ItemStack stack) {
        if (stack.is(ModTags.SET_ENTITY_ON_FIRE) &&
                target.isAttackable() && !target.skipAttackInteraction(player) && target instanceof LivingEntity) {
            if (!target.isOnFire() && CommonConfigs.TORCH_FIRE.get()) {
                int duration = CommonConfigs.TORCH_FIRE_DURATION.get();
                if (CompatHandler.SOUL_FIRED) {
                    SoulFiredCompat.setSecondsOnFire(target, duration, stack);
                } else {
                    target.setRemainingFireTicks(duration);
                }
                if (stack.is(ILightable.FLINT_AND_STEELS)) {
                    target.playSound(SoundEvents.FLINTANDSTEEL_USE, 1.0F, level.getRandom().nextFloat() * 0.4F + 0.8F);
                } else {
                    target.playSound(SoundEvents.FIRECHARGE_USE, 0.5F, 1.3F + level.getRandom().nextFloat() * 0.2F);
                }
            }
        }
        return InteractionResult.PASS;
    }
}