package net.mehvahdjukaar.amendments.common.recipe;


import net.minecraft.core.NonNullList;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.player.StackedContents;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.ItemStack;

import java.util.Collection;
import java.util.List;

public class DummyContainer implements CraftingContainer {

    private final NonNullList<ItemStack> stacks;
    private final int dimension;

    private DummyContainer(List<ItemStack> items) {
        this.dimension = Mth.ceil(Math.sqrt(items.size()));
        this.stacks = NonNullList.withSize(dimension * dimension, ItemStack.EMPTY);
        for(int i = 0; i < items.size(); i++){
            this.stacks.set(i, items.get(i));
        }
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

    public static DummyContainer of(List<ItemStack> items) {
        return new DummyContainer(items);
    }

    @Override
    public int getContainerSize() {
        return stacks.size();
    }

    @Override
    public boolean isEmpty() {
        return stacks.isEmpty();
    }

    @Override
    public ItemStack getItem(int slot) {
        if (slot >= this.getContainerSize()) return ItemStack.EMPTY;
        return stacks.get(slot);
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
        return stacks;
    }

    @Override
    public void fillStackedContents(StackedContents helper) {

    }
}
