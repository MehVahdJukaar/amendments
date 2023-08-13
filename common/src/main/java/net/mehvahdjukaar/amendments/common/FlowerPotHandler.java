package net.mehvahdjukaar.amendments.common;

import dev.architectury.injectables.annotations.ExpectPlatform;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.FlowerPotBlock;
import org.jetbrains.annotations.Contract;


public class FlowerPotHandler {

    @Contract
    @ExpectPlatform
    public static Block getEmptyPot(FlowerPotBlock fullPot) {
        throw new AssertionError();
    }

    @Contract
    @ExpectPlatform
    public static Block getFullPot(FlowerPotBlock emptyPot, Block flowerBlock) {
        throw new AssertionError();
    }

    @ExpectPlatform
    public static boolean isEmptyPot(Block b) {
        throw new AssertionError();
    }

    //move to forge
    @ExpectPlatform
    public static void setup() {
        throw new AssertionError();
    }


}
