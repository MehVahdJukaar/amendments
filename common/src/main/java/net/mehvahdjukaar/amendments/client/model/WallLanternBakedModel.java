package net.mehvahdjukaar.amendments.client.model;

import com.mojang.math.Transformation;
import net.mehvahdjukaar.amendments.client.WallLanternModelsManager;
import net.mehvahdjukaar.amendments.common.block.WallLanternBlock;
import net.mehvahdjukaar.amendments.common.tile.WallLanternBlockTile;
import net.mehvahdjukaar.amendments.reg.ModBlockProperties;
import net.mehvahdjukaar.moonlight.api.block.MimicBlock;
import net.mehvahdjukaar.moonlight.api.client.model.CustomBlockModel;
import net.mehvahdjukaar.moonlight.api.client.model.ExtraModelData;
import net.mehvahdjukaar.moonlight.api.client.model.QuadEmitter;
import net.mehvahdjukaar.moonlight.api.client.util.RenderUtil;
import net.mehvahdjukaar.moonlight.api.client.util.RotHlpr;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
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
        mat.rotate(RotHlpr.rot(state.getValue(WallLanternBlock.FACING)));
        mat.translate(0, 2 / 16f, 2 / 16f);
        mat.rotate(RotHlpr.Y90);

        emitter.transform(new Transformation(mat));
        emitter.emitAll(lanternModel(state, mimic), level, pos, mimic, random);
        emitter.transform((Matrix4f) null);
    }

    @Override
    public TextureAtlasSprite getParticle(ExtraModelData data) {
        BlockState mimic = data.get(ModBlockProperties.MIMIC);
        if (mimic != null && !mimic.isAir()) return RenderUtil.getBlockModel(mimic).particleMaterial().sprite();
        return this.support.particleMaterial().sprite();
    }

    @Override
    public @Nullable Object geometryKey(BlockAndTintGetter level, BlockPos pos, BlockState state,
                                        RandomSource random, ExtraModelData data) {
        return new Key(data.get(ModBlockProperties.MIMIC), data.get(WallLanternBlockTile.IS_FANCY), state);
    }

    private record Key(@Nullable BlockState mimic, @Nullable Boolean fancy, BlockState state) {
    }

    private static BlockStateModel lanternModel(BlockState wallLantern, BlockState mimic) {
        return WallLanternModelsManager.getLanternModel(((WallLanternBlock) wallLantern.getBlock()).type, mimic);
    }
}
