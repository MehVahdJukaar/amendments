package net.mehvahdjukaar.amendments.common.recipe;


import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.player.StackedContents;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class DummyContainer implements CraftingContainer {

    private final List<ItemStack> items = new ArrayList<>();
    private final int dimension;

    private DummyContainer(Collection<ItemStack> items) {
        this.items.addAll(items);
        this.dimension = Mth.ceil(Math.sqrt(items.size()));
    }

    public static DummyContainer surround(ItemStack dye, ItemStack toRecolor) {
        return new DummyContainer(List.of(
                toRecolor,
                toRecolor,
                toRecolor,
                toRecolor,
                dye,
                toRecolor,
                toRecolor,
                toRecolor,
                toRecolor));
    }

    public static DummyContainer of(ItemStack... items) {
        return new DummyContainer(List.of(items));
    }
    public static DummyContainer of(Collection<ItemStack> items) {
        return new DummyContainer(items);
    }

    @Override
    public int getContainerSize() {
        return items.size();
    }

    @Override
    public boolean isEmpty() {
        return items.isEmpty();
    }

    @Override
    public ItemStack getItem(int slot) {
        if (slot >= this.getContainerSize()) return ItemStack.EMPTY;
        return items.get(slot);
    }

    @Override
    public ItemStack removeItem(int slot, int amount) {
        return null;
    }

    @Override
    public ItemStack removeItemNoUpdate(int slot) {
        return null;
    }

    @Override
    public void setItem(int slot, ItemStack stack) {
    }

    @Override
    public void setChanged() {
    }

    @Override
    public boolean stillValid(Player player) {
        return true;
    }

    @Override
    public void clearContent() {
    }

    @Override
    public int getWidth() {
        return dimension;
    }

    @Override
    public int getHeight() {
        return dimension;
    }

    @Override
    public List<ItemStack> getItems() {
        return items;
    }

    @Override
    public void fillStackedContents(StackedContents helper) {

    }
}
