package net.mehvahdjukaar.amendments.mixins;

import net.mehvahdjukaar.amendments.common.block.BoilingWaterCauldronBlock;
import net.minecraft.core.cauldron.CauldronInteractions;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.Slice;

import java.util.function.Function;

@Mixin(Blocks.class)
public class BlocksMixin {

    //the ctor is inside a lambda passed to register so we swap the factory instead
    @ModifyArg(method = "<clinit>", index = 1, at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/level/block/Blocks;register(Ljava/lang/String;Ljava/util/function/Function;Lnet/minecraft/world/level/block/state/BlockBehaviour$Properties;)Lnet/minecraft/world/level/block/Block;",
            ordinal = 0
    ),
            slice = @Slice(
                    from = @At(
                            value = "CONSTANT",
                            args = "stringValue=water_cauldron"
                    )
            )
    )
    private static Function<BlockBehaviour.Properties, Block> amendments$overrideCauldron(Function<BlockBehaviour.Properties, Block> factory) {
        return p -> new BoilingWaterCauldronBlock(Biome.Precipitation.RAIN, CauldronInteractions.WATER, p);
    }
}
