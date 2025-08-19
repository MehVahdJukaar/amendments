package net.mehvahdjukaar.amendments.common.recipe;

import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class CompactItemSet {

    private final List<ItemStack> items = new ArrayList<>();

    public void add(@NotNull ItemStack item) {
        item = item.copy(); //make sure we don't modify the original item
        //add an item merging onto existing if possible. if not add a new item. remeber items have a max size so add all you can and add remainder later
        for (ItemStack existing : items) {
            if (ItemStack.isSameItemSameComponents(existing, item)) {
                int maxSize = existing.getMaxStackSize();
                int newCount = existing.getCount() + item.getCount();
                if (newCount <= maxSize) {
                    existing.setCount(newCount);
                    return;
                } else {
                    existing.setCount(maxSize);
                    item.setCount(newCount - maxSize);
                }
            }
        }
        if (!item.isEmpty()) {
            items.add(item);
        }

    }

    public List<ItemStack> toList() {
        return items.stream()
                .map(ItemStack::copy)
                .toList();
    }

}
