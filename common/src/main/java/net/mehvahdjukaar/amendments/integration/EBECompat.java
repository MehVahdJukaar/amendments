package net.mehvahdjukaar.amendments.integration;

import net.mehvahdjukaar.amendments.configs.ClientConfigs;
import net.minecraft.world.level.block.BellBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.CeilingHangingSignBlock;
import net.minecraft.world.level.block.SignBlock;
import net.minecraft.world.level.block.WallHangingSignBlock;

public class EBECompat {

    //EBE can still make its models. doesnt matter only thing that breaks is the text
    public static boolean rendersOurselves(Block block) {
        if (block instanceof CeilingHangingSignBlock || block instanceof WallHangingSignBlock) {
            return ClientConfigs.hasCustomHangingSignRenderer();
        }
        if (block instanceof SignBlock) return ClientConfigs.PIXEL_CONSISTENT_SIGNS.get();
        if (block instanceof BellBlock) return ClientConfigs.BELL_CONNECTION.get();
        return false;
    }
}
