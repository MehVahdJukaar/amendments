package net.mehvahdjukaar.amendments.common;

import net.mehvahdjukaar.candlelight.api.PlatformImpl;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.FlowerPotBlock;


public class FlowerPotHandler {

    @PlatformImpl
    public static Block getEmptyPot(FlowerPotBlock fullPot) {
        throw new AssertionError();
    }

    @PlatformImpl
    public static Block getFullPot(FlowerPotBlock emptyPot, Block flowerBlock) {
        throw new AssertionError();
    }

    @PlatformImpl
    public static boolean isEmptyPot(Block b) {
        throw new AssertionError();
    }

    //move to forge
    @PlatformImpl
    public static void setup() {
        throw new AssertionError();
    }


}
