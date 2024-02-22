package net.mehvahdjukaar.amendments.integration.forge;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.teamabnormals.blueprint.common.world.modification.structure.StructureRepaletter;
import com.teamabnormals.blueprint.common.world.modification.structure.StructureRepalleterManager;
import net.mehvahdjukaar.amendments.Amendments;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public class BlueprintIntegration {

    public static void init() {
        StructureRepalleterManager.registerSerializer(Amendments.res("blockstate_replace"),
                BlockStateRepaletter.CODEC);
    }

    public record BlockStateRepaletter(Block replacesBlock, BlockState replacesWith,
                                       float chance) implements StructureRepaletter {

        public static final Codec<BlockStateRepaletter> CODEC = RecordCodecBuilder.create(i -> i.group(
                BuiltInRegistries.BLOCK.byNameCodec().fieldOf("replaces_block").forGetter(BlockStateRepaletter::replacesBlock),
                BlockState.CODEC.fieldOf("replaces_with").forGetter(BlockStateRepaletter::replacesWith),
                Codec.FLOAT.optionalFieldOf("chance", 1f).forGetter(BlockStateRepaletter::chance)
        ).apply(i, BlockStateRepaletter::new));


        //this stupid system doesn't even allow changing tile entities
        @Nullable
        @Override
        public BlockState getReplacement(ServerLevelAccessor serverLevelAccessor, BlockState state, RandomSource randomSource) {
            return state.is(this.replacesBlock) && randomSource.nextFloat() < chance ? replacesWith : null;
        }

        @Override
        public Codec<? extends StructureRepaletter> codec() {
            return CODEC;
        }
    }
}

