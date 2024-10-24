package net.mehvahdjukaar.amendments.client.model;

import net.mehvahdjukaar.amendments.AmendmentsClient;
import net.mehvahdjukaar.amendments.client.WallLanternModelsManager;
import net.mehvahdjukaar.amendments.common.tile.WallLanternBlockTile;
import net.mehvahdjukaar.amendments.reg.ModBlockProperties;
import net.mehvahdjukaar.moonlight.api.block.MimicBlock;
import net.mehvahdjukaar.moonlight.api.client.model.BakedQuadsTransformer;
import net.mehvahdjukaar.moonlight.api.client.model.CustomBakedModel;
import net.mehvahdjukaar.moonlight.api.client.model.ExtraModelData;
import net.minecraft.client.Minecraft;
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
import org.joml.Matrix4f;

import java.util.ArrayList;
import java.util.List;

public class WallLanternBakedModel implements CustomBakedModel {
    private final BakedModel support;
    private final BlockModelShaper blockModelShaper;
    private final ModelState rotation;

    public WallLanternBakedModel(BakedModel support, ModelState state) {
        this.support = support;
        this.blockModelShaper = Minecraft.getInstance().getBlockRenderer().getBlockModelShaper();
        this.rotation = state;
    }

    @Override
    public List<BakedQuad> getBlockQuads(BlockState state, Direction side, RandomSource rand, RenderType renderType, ExtraModelData data) {

        List<BakedQuad> quads = new ArrayList<>();
        BlockState mimic = data.get(ModBlockProperties.MIMIC);
        if (mimic == null) return quads;

        //support
        var supportQuads = support.getQuads(state, side, rand);
        if (!supportQuads.isEmpty()) {
            var sprite = WallLanternModelsManager.getTexture(mimic.getBlock());
            if (sprite != null) {
                BakedQuadsTransformer transformer = BakedQuadsTransformer.create()
                        .applyingSprite(sprite);
                supportQuads = transformer.transformAll(supportQuads);
            }
            quads.addAll(supportQuads);
        }

        //mimic
        boolean fancy = Boolean.TRUE.equals(data.get(WallLanternBlockTile.IS_FANCY));
        if (!fancy && side == null) {
            //add all quads to null side
            if (!(mimic.getBlock() instanceof MimicBlock) && !mimic.isAir() && state != null) {
                BakedModel model = WallLanternModelsManager.getModel(blockModelShaper, mimic);
                List<BakedQuad> allQuads = AmendmentsClient.getAllModelQuads(model, mimic, rand);

                Matrix4f mat = new Matrix4f();
                mat.mul(rotation.getRotation().getMatrix());

                mat.translate(0, 2 / 16f, 2 / 16f);

                BakedQuadsTransformer transformer = BakedQuadsTransformer.create()
                        .applyingTransform(mat);
                quads.addAll(transformer.transformAll(allQuads));
            }
        }

        return quads;
    }

    @Override
    public boolean useAmbientOcclusion() {
        return true;
    }

    @Override
    public boolean isGui3d() {
        return false;
    }

    @Override
    public boolean usesBlockLight() {
        return false;
    }

    @Override
    public boolean isCustomRenderer() {
        return false;
    }

    @Override
    public TextureAtlasSprite getBlockParticle(ExtraModelData data) {
        BlockState mimic = data.get(ModBlockProperties.MIMIC);
        if (mimic != null && !mimic.isAir()) {

            BakedModel model = blockModelShaper.getBlockModel(mimic);
            try {
                return model.getParticleIcon();
            } catch (Exception ignored) {
            }

        }
        return support.getParticleIcon();
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
