package net.mehvahdjukaar.amendments.client.model;

import net.mehvahdjukaar.amendments.AmendmentsPlatformStuff;
import net.mehvahdjukaar.amendments.client.ModMaterials;
import net.mehvahdjukaar.amendments.common.tile.LiquidCauldronBlockTile;
import net.mehvahdjukaar.moonlight.api.client.model.BakedQuadBuilder;
import net.mehvahdjukaar.moonlight.api.client.model.CustomBakedModel;
import net.mehvahdjukaar.moonlight.api.client.model.ExtraModelData;
import net.mehvahdjukaar.moonlight.api.client.util.VertexUtil;
import net.mehvahdjukaar.moonlight.api.fluids.SoftFluid;
import net.mehvahdjukaar.moonlight.api.platform.PlatHelper;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelState;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;

import java.util.ArrayList;
import java.util.List;

public class CauldronBakedModel implements CustomBakedModel {
    private static final boolean SINGLE_PASS = PlatHelper.getPlatform().isFabric();

    private final BakedModel cauldron;
    private final BakedModel fluid;
    private final ModelState transform;
    private final boolean hasTranslucent;

    public CauldronBakedModel(BakedModel c, BakedModel fluid, ModelState transform, boolean translucent) {
        this.cauldron = c;
        this.fluid = fluid;
        this.transform = transform;
        this.hasTranslucent = translucent && !SINGLE_PASS;
    }

    @Override
    public List<BakedQuad> getBlockQuads(BlockState state, Direction direction, RandomSource randomSource, RenderType renderType, ExtraModelData extraModelData) {
        List<BakedQuad> quads = new ArrayList<>();
        boolean isTranslucentLayer = renderType == RenderType.translucent();
        if (!hasTranslucent || !isTranslucentLayer) {
            quads.addAll(cauldron.getQuads(state, direction, randomSource));
        }
        if (!hasTranslucent || isTranslucentLayer) {
            List<BakedQuad> liquidQuads = fluid.getQuads(state, direction, randomSource);

            SoftFluid fluid = extraModelData.get(LiquidCauldronBlockTile.FLUID);
            if (fluid != null && !fluid.isEmpty()) {
                TextureAtlasSprite sprite = ModMaterials.get(fluid.getStillTexture()).sprite();
                var b = BakedQuadBuilder.create(sprite);
                for (var q : VertexUtil.swapSprite(liquidQuads, sprite)) {
                    b.fromVanilla(q);
                    b.setDirection(q.getDirection());
                    b.lightEmission(fluid.getLuminosity());
                    b.setAmbientOcclusion(false);
                    quads.add(b.build());
                    //add emissivity. not rally needed since these do give off light too
                }
            }else {
                quads.addAll(AmendmentsPlatformStuff.removeAmbientOcclusion(liquidQuads));
            }
        }
        return quads;
    }

    @Override
    public TextureAtlasSprite getBlockParticle(ExtraModelData extraModelData) {
        return cauldron.getParticleIcon();
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
    public ItemTransforms getTransforms() {
        return ItemTransforms.NO_TRANSFORMS;
    }

    @Override
    public ItemOverrides getOverrides() {
        return ItemOverrides.EMPTY;
    }
}
