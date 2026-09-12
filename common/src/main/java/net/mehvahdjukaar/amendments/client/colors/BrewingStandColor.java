package net.mehvahdjukaar.amendments.client.colors;

import net.mehvahdjukaar.amendments.configs.ClientConfigs;
import net.minecraft.client.color.block.BlockTintSource;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.level.block.entity.BrewingStandBlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import java.util.ArrayList;
import java.util.List;

public record BrewingStandColor(int slot) implements BlockTintSource {

    public static List<BlockTintSource> layers() {
        List<BlockTintSource> list = new ArrayList<>();
        list.add((BlockTintSource) state -> -1);
        for (int i = 0; i < 3; i++) list.add(new BrewingStandColor(i));
        return list;
    }

    @Override
    public int color(BlockState state) {
        return 0xffffff;
    }

    @Override
    public int colorInWorld(BlockState state, BlockAndTintGetter level, BlockPos pos) {
        if (level == null || pos == null) return 0xffffff;
        if (!(level.getBlockEntity(pos) instanceof BrewingStandBlockEntity br)) return 0xffffff;
        ItemStack item = br.getItem(this.slot);
        PotionContents contents = item.get(DataComponents.POTION_CONTENTS);
        if (contents == null) return -1;
        //TODO: use dynamic pack
        if (!ClientConfigs.COLORED_BREWING_STAND.get()) return 0xff3434;
        return contents.getColor();
    }
}
