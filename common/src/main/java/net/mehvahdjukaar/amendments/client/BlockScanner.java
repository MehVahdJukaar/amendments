package net.mehvahdjukaar.amendments.client;

import com.google.common.collect.ImmutableSet;
import net.mehvahdjukaar.amendments.common.block.WallLanternBlock;
import net.mehvahdjukaar.amendments.integration.CompatHandler;
import net.mehvahdjukaar.amendments.integration.FarmersDelightCompat;
import net.mehvahdjukaar.amendments.integration.SuppCompat;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.TorchBlock;
import net.minecraft.world.level.block.WallTorchBlock;
import org.jetbrains.annotations.NotNull;

import java.util.Set;

public class BlockScanner {

    private static final Set<Block> lanterns;
    private static final Set<Block> torches;
    private static final Set<Block> candleHolders;
    private static final Set<Block> fdSigns;

    //TODO: make data driven, on world reload
    //statically initialized because its needed very early. Pls no concurrency issues
    static {
        ImmutableSet.Builder<Block> lanternBuilder = ImmutableSet.builder();
        ImmutableSet.Builder<Block> torchesBuilder = ImmutableSet.builder();
        ImmutableSet.Builder<Block> candleBuilder = ImmutableSet.builder();
        ImmutableSet.Builder<Block> fdSignsBuilder = ImmutableSet.builder();
        //TODO:use registry event instead
        for (Block block : BuiltInRegistries.BLOCK) {
            if (WallLanternBlock.isValidBlock(block)) lanternBuilder.add(block);

            else if (block instanceof TorchBlock && !(block instanceof WallTorchBlock) || (
                    CompatHandler.SUPPLEMENTARIES && SuppCompat.isSconce(block))) {
                torchesBuilder.add(block);
            } else if (CompatHandler.SUPPLEMENTARIES && SuppCompat.isCandleHolder(block)) {
                candleBuilder.add(block);
            } else if (CompatHandler.FARMERS_DELIGHT && FarmersDelightCompat.isStandingSign(block)) {
                fdSignsBuilder.add(block);
            }
        }
        torchesBuilder.add(Blocks.REDSTONE_TORCH);
        lanterns = lanternBuilder.build();
        torches = torchesBuilder.build();
        candleHolders = candleBuilder.build();
        fdSigns = fdSignsBuilder.build();
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

    public static Set<Block> getFdSigns(){
        return fdSigns;
    }
}
