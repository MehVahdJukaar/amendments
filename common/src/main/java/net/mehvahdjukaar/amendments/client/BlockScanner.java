package net.mehvahdjukaar.amendments.client;

import com.google.common.collect.ImmutableSet;
import net.mehvahdjukaar.amendments.common.block.WallLanternBlock;
import net.mehvahdjukaar.amendments.integration.CompatHandler;
import net.mehvahdjukaar.amendments.integration.SuppCompat;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.TorchBlock;
import net.minecraft.world.level.block.WallTorchBlock;
import org.jetbrains.annotations.NotNull;

import java.util.Set;

public class BlockScanner {

    private static Set<Block> lanterns;
    private static Set<Block> torches;
    private static Set<Block> candleHolders;

    public static void scanBlocks() {
        ImmutableSet.Builder<Block> lanternBuilder = ImmutableSet.builder();
        ImmutableSet.Builder<Block> torchesBuilder = ImmutableSet.builder();
        ImmutableSet.Builder<Block> candleBuilder = ImmutableSet.builder();
        for (Block block : BuiltInRegistries.BLOCK) {
            if (WallLanternBlock.isValidBlock(block)) lanternBuilder.add(block);

            else if (block instanceof TorchBlock && !(block instanceof WallTorchBlock) || (
                    CompatHandler.SUPPLEMENTARIES && SuppCompat.isSconce(block))) {
                torchesBuilder.add(block);
            } else if (CompatHandler.SUPPLEMENTARIES && SuppCompat.isCandleHolder(block)) {
                candleBuilder.add(block);
            }
        }
        lanterns = lanternBuilder.build();
        torches = torchesBuilder.build();
        candleHolders = candleBuilder.build();
    }

    @NotNull
    public static Set<Block> getLanterns() {
        return lanterns;
    }

    @NotNull
    public static Set<Block> getTorches() {
        return torches;
    }

    @NotNull
    public static Set<Block> getCandleHolders() {
        return candleHolders;
    }
}
