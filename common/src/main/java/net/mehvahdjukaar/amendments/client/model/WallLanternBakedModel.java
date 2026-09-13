package net.mehvahdjukaar.amendments.client.model;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.mehvahdjukaar.amendments.common.tile.WallLanternBlockTile;
import net.mehvahdjukaar.amendments.reg.ModBlockProperties;
import net.mehvahdjukaar.moonlight.api.block.MimicBlock;
import net.mehvahdjukaar.moonlight.api.client.model.CustomBlockModel;
import net.mehvahdjukaar.moonlight.api.client.model.CustomUnbakedModel;
import net.mehvahdjukaar.moonlight.api.client.model.ExtraModelData;
import net.mehvahdjukaar.moonlight.api.client.model.QuadEmitter;
import net.mehvahdjukaar.moonlight.api.client.util.RotHlpr;
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
import org.joml.Matrix4f;

public class WallLanternBakedModel implements CustomBlockModel {

    private final BlockStateModel support;

    public WallLanternBakedModel(BlockStateModel support) {
        this.support = support;
    }

    @Override
    public void emitQuads(QuadEmitter emitter, @Nullable BlockAndTintGetter level, @Nullable BlockPos pos,
                          @Nullable BlockState state, RandomSource random, ExtraModelData data) {
        emitter.emitAll(this.support, level, pos, state, random);

        if (state == null) return;
        if (Boolean.TRUE.equals(data.get(WallLanternBlockTile.IS_FANCY))) return;

        BlockState mimic = data.get(ModBlockProperties.MIMIC);
        if (mimic == null || mimic.isAir() || mimic.getBlock() instanceof MimicBlock) return;

        Matrix4f mat = new Matrix4f();
        mat.translate(0, 2 / 16f, 2 / 16f);
        mat.rotate(RotHlpr.Y90);

        emitter.transform(mat);
        emitter.emitAll(blockModel(mimic), level, pos, mimic, random);
        emitter.transform((Matrix4f) null);
    }

    @Override
    public TextureAtlasSprite getParticle(ExtraModelData data) {
        BlockState mimic = data.get(ModBlockProperties.MIMIC);
        if (mimic != null && !mimic.isAir()) return blockModel(mimic).particleMaterial().sprite();
        return this.support.particleMaterial().sprite();
    }

    @Override
    public @Nullable Object geometryKey(BlockAndTintGetter level, BlockPos pos, BlockState state,
                                        RandomSource random, ExtraModelData data) {
        return new Key(data.get(ModBlockProperties.MIMIC), data.get(WallLanternBlockTile.IS_FANCY), state);
    }

    private record Key(@Nullable BlockState mimic, @Nullable Boolean fancy, BlockState state) {
    }

    private static BlockStateModel blockModel(BlockState state) {
        return Minecraft.getInstance().getModelManager().getBlockStateModelSet().get(state);
    }

    public record Unbaked(BlockStateModel.Unbaked support) implements CustomUnbakedModel {

        public static final MapCodec<Unbaked> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
                BlockStateModel.Unbaked.CODEC.fieldOf("support").forGetter(Unbaked::support)
        ).apply(i, Unbaked::new));

        @Override
        public CustomBlockModel bake(ModelBaker baker) {
            return new WallLanternBakedModel(this.support.bake(baker));
        }

        @Override
        public MapCodec<? extends CustomUnbakedModel> codec() {
            return CODEC;
        }

        @Override
        public void resolveDependencies(ResolvableModel.Resolver resolver) {
            this.support.resolveDependencies(resolver);
        }
    }
}
