package net.mehvahdjukaar.amendments.common;

import net.mehvahdjukaar.amendments.configs.CommonConfigs;
import net.mehvahdjukaar.amendments.reg.ModTags;
import net.mehvahdjukaar.candlelight.api.PlatformImpl;
import net.minecraft.core.component.DataComponents;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.BundleContents;
import net.minecraft.world.item.component.ItemContainerContents;
import net.minecraft.world.level.block.ShulkerBoxBlock;
import net.minecraft.world.level.block.entity.ShulkerBoxBlockEntity;

public class HeldContainerPickup {

    public static boolean absorb(Player player, ItemStack picked) {
        var mode = CommonConfigs.HELD_CONTAINER_PICKUP.get();
        if (mode == CommonConfigs.PickupHands.OFF || player.isSpectator()) return false;

        int before = picked.getCount();
        if (mode != CommonConfigs.PickupHands.MAIN_HAND) {
            insertIntoHandItem(player, InteractionHand.OFF_HAND, picked);
        }
        if (mode != CommonConfigs.PickupHands.OFFHAND && !picked.isEmpty()) {
            insertIntoHandItem(player, InteractionHand.MAIN_HAND, picked);
        }

        if (picked.getCount() == before) return false;
        if (CommonConfigs.HELD_CONTAINER_PICKUP_SOUND.get()) {
            var random = player.getRandom();
            player.level().playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.BUNDLE_INSERT,
                    SoundSource.PLAYERS, 0.2f, (random.nextFloat() - random.nextFloat()) * 1.4f + 2f);
        }
        return true;
    }

    private static void insertIntoHandItem(Player player, InteractionHand hand, ItemStack picked) {
        ItemStack container = player.getItemInHand(hand);
        if (container.getCount() != 1 || container.is(ModTags.HELD_PICKUP_BLACKLIST)) {
            return;
        }

        int inserted = insertIntoItemStorage(player, hand, picked);
        if (inserted > 0) {
            picked.shrink(inserted);
            return;
        }

        var bundle = container.get(DataComponents.BUNDLE_CONTENTS);
        if (bundle != null) {
            var mutable = new BundleContents.Mutable(bundle);
            if (mutable.tryInsert(picked) > 0) {
                container.set(DataComponents.BUNDLE_CONTENTS, mutable.toImmutable());
            }
            return;
        }

        boolean isShulker = container.getItem() instanceof BlockItem b && b.getBlock() instanceof ShulkerBoxBlock;
        if (isShulker && picked.getItem().canFitInsideContainerItems()) {
            insertIntoShulker(container, picked);
        }
    }

    private static void insertIntoShulker(ItemStack shulker, ItemStack picked) {
        var container = new SimpleContainer(ShulkerBoxBlockEntity.CONTAINER_SIZE);
        shulker.getOrDefault(DataComponents.CONTAINER, ItemContainerContents.EMPTY).copyInto(container.getItems());

        ItemStack left = container.addItem(picked);
        if (left.getCount() != picked.getCount()) {
            shulker.set(DataComponents.CONTAINER, ItemContainerContents.fromItems(container.getItems()));
            picked.setCount(left.getCount());
        }
    }

    @PlatformImpl
    public static int insertIntoItemStorage(Player player, InteractionHand hand, ItemStack toInsert) {
        throw new AssertionError();
    }
}
