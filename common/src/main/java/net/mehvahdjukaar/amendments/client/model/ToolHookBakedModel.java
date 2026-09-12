package net.mehvahdjukaar.amendments.client.model;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.mehvahdjukaar.moonlight.api.client.model.CustomBlockModel;
import net.mehvahdjukaar.moonlight.api.client.model.CustomUnbakedModel;
import net.mehvahdjukaar.moonlight.api.client.model.ExtraModelData;
import net.mehvahdjukaar.moonlight.api.client.model.QuadEmitter;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.ModelBaker;
import net.minecraft.client.resources.model.ResolvableModel;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;

public class ToolHookBakedModel implements CustomBlockModel {

    private final BlockStateModel tripwireHook;

    public ToolHookBakedModel(BlockStateModel tripwireHook) {
        this.tripwireHook = tripwireHook;
    }

    @Override
    public void emitQuads(QuadEmitter emitter, @Nullable BlockAndTintGetter level, @Nullable BlockPos pos,
                          @Nullable BlockState state, RandomSource random, ExtraModelData data) {
        emitter.transform(new Matrix4f().translate(0, 5 / 16f, 0));
        emitter.emitAll(this.tripwireHook, level, pos, state, random);
        emitter.transform((Matrix4f) null);
    }

    @Override
    public TextureAtlasSprite getParticle(ExtraModelData data) {
        return this.tripwireHook.particleMaterial().sprite();
    }

    public record Unbaked(BlockStateModel.Unbaked hook) implements CustomUnbakedModel {

        public static final MapCodec<Unbaked> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
                BlockStateModel.Unbaked.CODEC.fieldOf("hook").forGetter(Unbaked::hook)
        ).apply(i, Unbaked::new));

        @Override
        public CustomBlockModel bake(ModelBaker baker) {
            return new ToolHookBakedModel(this.hook.bake(baker));
        }

        @Override
        public MapCodec<? extends CustomUnbakedModel> codec() {
            return CODEC;
        }

        @Override
        public void resolveDependencies(ResolvableModel.Resolver resolver) {
            this.hook.resolveDependencies(resolver);
        }
    }
}
