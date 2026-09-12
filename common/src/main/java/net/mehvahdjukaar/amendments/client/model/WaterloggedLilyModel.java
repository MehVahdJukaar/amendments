package net.mehvahdjukaar.amendments.client.model;

import com.mojang.serialization.MapCodec;
import net.mehvahdjukaar.amendments.common.block.WaterloggedLilyBlock;
import net.mehvahdjukaar.amendments.common.tile.WaterloggedLilyBlockTile;
import net.mehvahdjukaar.amendments.configs.ClientConfigs;
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
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;

public class WaterloggedLilyModel implements CustomBlockModel {

    @Override
    public void emitQuads(QuadEmitter emitter, @Nullable BlockAndTintGetter level, @Nullable BlockPos pos,
                          @Nullable BlockState state, RandomSource random, ExtraModelData data) {
        BlockState mimic = data.get(WaterloggedLilyBlockTile.MIMIC_KEY);
        if (mimic == null || mimic.isAir() || state == null) return;

        double dy = 1 + (state.getValue(WaterloggedLilyBlock.EXTENDED) ? 0 : ClientConfigs.LILY_OFFSET.get());
        emitter.transform(new Matrix4f().translate(0, (float) dy, 0));
        emitter.emitAll(blockModel(mimic), level, pos, mimic, random);
        emitter.transform((Matrix4f) null);
    }

    @Override
    public TextureAtlasSprite getParticle(ExtraModelData data) {
        BlockState mimic = data.get(WaterloggedLilyBlockTile.MIMIC_KEY);
        if (mimic != null && !mimic.isAir()) return blockModel(mimic).particleMaterial().sprite();
        return blockModel(Blocks.LILY_PAD.defaultBlockState()).particleMaterial().sprite();
    }

    @Override
    public @Nullable Object geometryKey(BlockAndTintGetter level, BlockPos pos, BlockState state,
                                        RandomSource random, ExtraModelData data) {
        return data.get(WaterloggedLilyBlockTile.MIMIC_KEY);
    }

    private static BlockStateModel blockModel(BlockState state) {
        return Minecraft.getInstance().getBlockRenderer().getBlockModel(state);
    }

    public record Unbaked() implements CustomUnbakedModel {

        public static final MapCodec<Unbaked> CODEC = MapCodec.unit(Unbaked::new);

        @Override
        public CustomBlockModel bake(ModelBaker baker) {
            return new WaterloggedLilyModel();
        }

        @Override
        public MapCodec<? extends CustomUnbakedModel> codec() {
            return CODEC;
        }

        @Override
        public void resolveDependencies(ResolvableModel.Resolver resolver) {
        }
    }
}
