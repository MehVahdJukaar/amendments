package net.mehvahdjukaar.amendments.client.model;

import net.mehvahdjukaar.moonlight.api.block.MimicBlockTile;
import net.mehvahdjukaar.moonlight.api.client.model.CustomBlockModel;
import net.mehvahdjukaar.moonlight.api.client.model.ExtraModelData;
import net.mehvahdjukaar.moonlight.api.client.model.QuadEmitter;
import net.mehvahdjukaar.moonlight.api.client.util.RenderUtil;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
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
            emitter.emitBlock(mimic, level, pos, random);
        }
        emitter.emitAll(this.rope, level, pos, state, random);
    }

    @Override
    public TextureAtlasSprite getParticle(ExtraModelData data) {
        BlockState mimic = data.get(MimicBlockTile.MIMIC_KEY);
        if (mimic != null && !mimic.isAir()) {
            return RenderUtil.getBlockModel(mimic).particleMaterial().sprite();
        }
        return this.rope.particleMaterial().sprite();
    }

    @Override
    public @Nullable Object geometryKey(BlockAndTintGetter level, BlockPos pos, BlockState state,
                                        RandomSource random, ExtraModelData data) {
        return data.get(MimicBlockTile.MIMIC_KEY);
    }
}
