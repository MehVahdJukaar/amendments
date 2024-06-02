package net.mehvahdjukaar.amendments.client;

import com.google.common.collect.ImmutableSet;
import net.mehvahdjukaar.amendments.common.block.WallLanternBlock;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.TorchBlock;
import net.minecraft.world.level.block.WallTorchBlock;
import org.jetbrains.annotations.NotNull;

import java.util.Set;

public class BlockScanner {

    private static Set<Block> lanterns;
    private static Set<Block> torches;

    public static void scanBlocks() {
        ImmutableSet.Builder<Block> lanternBuilder = ImmutableSet.builder();
        ImmutableSet.Builder<Block> torchesBuilder = ImmutableSet.builder();
        for (Block block : BuiltInRegistries.BLOCK) {
            if (WallLanternBlock.isValidBlock(block)) lanternBuilder.add(block);

            if(block instanceof TorchBlock && !(block instanceof WallTorchBlock)){
                torchesBuilder.add(block);
            }
        }
        lanterns = lanternBuilder.build();
        torches = torchesBuilder.build();
    }

    @NotNull
    public static Set<Block> getLanterns() {
        return lanterns;
    }

    @NotNull
    public static Set<Block> getTorches() {
        return torches;
    }
}
