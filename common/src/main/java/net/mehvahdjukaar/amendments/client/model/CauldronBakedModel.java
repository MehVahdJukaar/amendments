package net.mehvahdjukaar.amendments.client.model;

import net.mehvahdjukaar.amendments.AmendmentsClient;
import net.mehvahdjukaar.amendments.common.tile.LiquidCauldronBlockTile;
import net.mehvahdjukaar.amendments.configs.ClientConfigs;
import net.mehvahdjukaar.moonlight.api.client.model.BakedQuadsTransformer;
import net.mehvahdjukaar.moonlight.api.client.model.CustomBakedModel;
import net.mehvahdjukaar.moonlight.api.client.model.ExtraModelData;
import net.mehvahdjukaar.moonlight.api.fluids.BuiltInSoftFluids;
import net.mehvahdjukaar.moonlight.api.fluids.SoftFluid;
import net.mehvahdjukaar.moonlight.api.platform.ClientHelper;
import net.mehvahdjukaar.moonlight.api.platform.PlatHelper;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelState;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
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
            BakedQuadsTransformer transformer = BakedQuadsTransformer.create();
            // has custom fluid
            if (fluid != null && !fluid.isEmptyFluid()) {
                ResourceLocation stillTexture = fluid.getStillTexture();
                if (ClientConfigs.POTION_TEXTURE.get() && fluid == BuiltInSoftFluids.POTION.get()) {
                    stillTexture = AmendmentsClient.POTION_TEXTURE;
                }else if(fluid == BuiltInSoftFluids.MUSHROOM_STEW.get()){
                    stillTexture = AmendmentsClient.MUSHROOM_STEW;
                }else if(fluid == BuiltInSoftFluids.BEETROOT_SOUP.get()){
                    stillTexture = AmendmentsClient.BEETROOT_SOUP;
                }else if(fluid == BuiltInSoftFluids.RABBIT_STEW.get()){
                    stillTexture = AmendmentsClient.RABBIT_STEW;
                } else if(fluid == BuiltInSoftFluids.SUS_STEW.get()){
                    stillTexture = AmendmentsClient.SUS_STEW;
                }
                TextureAtlasSprite sprite = ClientHelper.getBlockMaterial(stillTexture).sprite();
                transformer.applyingAmbientOcclusion(false)
                        .applyingEmissivity(fluid.getEmissivity())
                        .applyingSprite(sprite);
            } else {
                transformer.applyingAmbientOcclusion(false);
            }
            quads.addAll(transformer.transformAll(liquidQuads));
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
