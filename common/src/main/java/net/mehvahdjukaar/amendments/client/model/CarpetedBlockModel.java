package net.mehvahdjukaar.amendments.client.model;

import net.mehvahdjukaar.amendments.common.tile.CarpetedBlockTile;
import net.mehvahdjukaar.moonlight.api.client.model.BakedQuadsTransformer;
import net.mehvahdjukaar.moonlight.api.client.model.CustomBakedModel;
import net.mehvahdjukaar.moonlight.api.client.model.ExtraModelData;
import net.mehvahdjukaar.moonlight.api.platform.PlatHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.BlockModelShaper;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelState;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class CarpetedBlockModel implements CustomBakedModel {
    private final BakedModel carpet;
    private final BlockModelShaper blockModelShaper;

    public CarpetedBlockModel(BakedModel carpet, ModelState state) {
        this.carpet = carpet;
        this.blockModelShaper = Minecraft.getInstance().getBlockRenderer().getBlockModelShaper();
    }

    @Override
    public List<BakedQuad> getBlockQuads(BlockState state, Direction side, RandomSource rand, @Nullable RenderType renderType,
                                         ExtraModelData data) {
        List<BakedQuad> quads = new ArrayList<>();
        boolean fabric = PlatHelper.getPlatform().isFabric();

        if (state == null) return quads;
        try {
            BlockState mimic = data.get(CarpetedBlockTile.MIMIC_KEY);

            if (mimic != null) {
                RenderType originalRenderType = ItemBlockRenderTypes.getChunkRenderType(mimic);
                // only when on its original render layer or when we do block breaking anim (null render type)
                if (originalRenderType == renderType || renderType == null || fabric) {
                    BakedModel model = blockModelShaper.getBlockModel(mimic);
                    quads.addAll(model.getQuads(mimic, side, rand));
                }
            }
        } catch (Exception ignored) {
        }


        if (renderType == RenderType.solid() || renderType == null || fabric) {
            //only outputs carpet on the solid layer
            try {
                BlockState carpetBlock = data.get(CarpetedBlockTile.CARPET_KEY);
                List<BakedQuad> carpetQuads = carpet.getQuads(state, side, rand);

                if (!carpetQuads.isEmpty()) {
                    if (carpetBlock != null) {
                        TextureAtlasSprite sprite = getCarpetSprite(carpetBlock);
                        BakedQuadsTransformer transformer = BakedQuadsTransformer.create()
                                .applyingSprite(sprite)
                                .applyingAmbientOcclusion(false);
                        carpetQuads = transformer.transformAll(carpetQuads);
                    }
                    quads.addAll(carpetQuads);
                }
            } catch (Exception ignored) {
                int error = 1;
            }
        }

        return quads;
    }

    private TextureAtlasSprite getCarpetSprite(BlockState carpetBlock) {
        return blockModelShaper.getBlockModel(carpetBlock).getParticleIcon();
    }

    @Override
    public boolean useAmbientOcclusion() {
        return true;
    }

    @Override
    public boolean isGui3d() {
        return true;
    }

    @Override
    public boolean usesBlockLight() {
        return true;
    }

    @Override
    public boolean isCustomRenderer() {
        return false;
    }

    @Override
    public TextureAtlasSprite getBlockParticle(ExtraModelData data) {
        BlockState mimic = data.get(CarpetedBlockTile.MIMIC_KEY);
        if (mimic != null && !mimic.isAir()) {
            BakedModel model = blockModelShaper.getBlockModel(mimic);
            try {
                return model.getParticleIcon();
            } catch (Exception ignored) {
            }
        }
        return carpet.getParticleIcon();
    }

    @Override
    public ItemOverrides getOverrides() {
        return ItemOverrides.EMPTY;
    }

    @Override
    public ItemTransforms getTransforms() {
        return ItemTransforms.NO_TRANSFORMS;
    }

}
