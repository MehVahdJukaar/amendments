package net.mehvahdjukaar.amendments.client.model;

import net.mehvahdjukaar.amendments.common.tile.CarpetedBlockTile;
import net.mehvahdjukaar.amendments.reg.ModRegistry;
import net.mehvahdjukaar.moonlight.api.client.model.CustomBlockModel;
import net.mehvahdjukaar.moonlight.api.client.model.ExtraModelData;
import net.mehvahdjukaar.moonlight.api.client.model.QuadEmitter;
import net.mehvahdjukaar.moonlight.api.client.util.RenderUtil;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.geometry.BakedQuad;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public class CarpetedBlockModel implements CustomBlockModel {

    private final BlockStateModel carpet;

    public CarpetedBlockModel(BlockStateModel carpet) {
        this.carpet = carpet;
    }

    @Override
    public void emitQuads(QuadEmitter emitter, @Nullable BlockAndTintGetter level, @Nullable BlockPos pos,
                          @Nullable BlockState state, RandomSource random, ExtraModelData data) {
        if (state == null) return;

        BlockState mimic = data.get(CarpetedBlockTile.MIMIC_KEY);
        if (mimic != null) {
            emitter.emitBlock(mimic, level, pos, random);
        }

        BlockState carpetBlock = data.get(CarpetedBlockTile.CARPET_KEY);
        if (carpetBlock == null) {
            emitter.emitAll(this.carpet, level, pos, state, random);
            return;
        }
        boolean occl = state.is(ModRegistry.CARPET_SLAB.get());
        TextureAtlasSprite sprite = particle(carpetBlock);
        for (BakedQuad q : CustomBlockModel.collectQuads(this.carpet, level, pos, state, random)) {
            emitter.fromQuad(q)
                    .sprite(sprite)
                    .tintIndex(16)
                    .ambientOcclusion(occl)
                    .emit();
        }
    }

    @Override
    public TextureAtlasSprite getParticle(ExtraModelData data) {
        BlockState mimic = data.get(CarpetedBlockTile.MIMIC_KEY);
        if (mimic != null && !mimic.isAir()) return particle(mimic);
        return this.carpet.particleMaterial().sprite();
    }

    @Override
    public @Nullable Object geometryKey(BlockAndTintGetter level, BlockPos pos, BlockState state,
                                        RandomSource random, ExtraModelData data) {
        return new Key(data.get(CarpetedBlockTile.MIMIC_KEY), data.get(CarpetedBlockTile.CARPET_KEY), state);
    }

    private record Key(@Nullable BlockState mimic, @Nullable BlockState carpet, BlockState state) {
    }

    private static TextureAtlasSprite particle(BlockState state) {
        return RenderUtil.getBlockModel(state).particleMaterial().sprite();
    }
}
