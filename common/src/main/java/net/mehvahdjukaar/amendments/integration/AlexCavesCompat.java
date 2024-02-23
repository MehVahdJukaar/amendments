package net.mehvahdjukaar.amendments.integration;

import dev.architectury.injectables.annotations.ExpectPlatform;
import net.mehvahdjukaar.moonlight.api.fluids.SoftFluidStack;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

public class AlexCavesCompat {

    @ExpectPlatform
    public static void acidDamage(SoftFluidStack fluid, Level level, BlockPos pos, BlockState state, Entity entity) {
        throw new AssertionError();
    }

    @ExpectPlatform
    public static void acidParticles(SoftFluidStack fluid, Level level, BlockPos pos, RandomSource rand, double height) {
        throw new AssertionError();
    }
}
