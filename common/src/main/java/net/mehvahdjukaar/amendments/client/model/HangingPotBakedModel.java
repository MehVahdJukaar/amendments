package net.mehvahdjukaar.amendments.client.model;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.mehvahdjukaar.moonlight.api.block.MimicBlockTile;
import net.mehvahdjukaar.moonlight.api.client.model.CustomBlockModel;
import net.mehvahdjukaar.moonlight.api.client.model.CustomUnbakedModel;
import net.mehvahdjukaar.moonlight.api.client.model.ExtraModelData;
import net.mehvahdjukaar.moonlight.api.client.model.QuadEmitter;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.ModelBaker;
import net.minecraft.client.resources.model.ResolvableModel;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public class HangingPotBakedModel implements CustomBlockModel {

    private final BlockStateModel rope;

    public HangingPotBakedModel(BlockStateModel rope) {
        this.rope = rope;
    }

    @Override
    public void emitQuads(QuadEmitter emitter, @Nullable BlockAndTintGetter level, @Nullable BlockPos pos,
                          @Nullable BlockState state, RandomSource random, ExtraModelData data) {
        if (state == null) return;
        BlockState mimic = data.get(MimicBlockTile.MIMIC_KEY);
        if (mimic != null) {
            emitter.emitAll(Minecraft.getInstance().getModelManager().getBlockStateModelSet().get(mimic), level, pos, mimic, random);
        }
        emitter.emitAll(this.rope, level, pos, state, random);
    }

    @Override
    public TextureAtlasSprite getParticle(ExtraModelData data) {
        BlockState mimic = data.get(MimicBlockTile.MIMIC_KEY);
        if (mimic != null && !mimic.isAir()) {
            return Minecraft.getInstance().getModelManager().getBlockStateModelSet().get(mimic).particleMaterial().sprite();
        }
        return this.rope.particleMaterial().sprite();
    }

    @Override
    public @Nullable Object geometryKey(BlockAndTintGetter level, BlockPos pos, BlockState state,
                                        RandomSource random, ExtraModelData data) {
        return data.get(MimicBlockTile.MIMIC_KEY);
    }

    public record Unbaked(BlockStateModel.Unbaked rope) implements CustomUnbakedModel {

        public static final MapCodec<Unbaked> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
                BlockStateModel.Unbaked.CODEC.fieldOf("rope").forGetter(Unbaked::rope)
        ).apply(i, Unbaked::new));

        @Override
        public CustomBlockModel bake(ModelBaker baker) {
            return new HangingPotBakedModel(this.rope.bake(baker));
        }

        @Override
        public MapCodec<? extends CustomUnbakedModel> codec() {
            return CODEC;
        }

        @Override
        public void resolveDependencies(ResolvableModel.Resolver resolver) {
            this.rope.resolveDependencies(resolver);
        }
    }
}
