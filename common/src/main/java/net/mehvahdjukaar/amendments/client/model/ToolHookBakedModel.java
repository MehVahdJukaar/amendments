package net.mehvahdjukaar.amendments.client.model;

import com.mojang.math.Axis;
import net.mehvahdjukaar.amendments.reg.ModBlockProperties;
import net.mehvahdjukaar.moonlight.api.client.model.BakedQuadsTransformer;
import net.mehvahdjukaar.moonlight.api.client.model.CustomBakedModel;
import net.mehvahdjukaar.moonlight.api.client.model.ExtraModelData;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ItemModelShaper;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelState;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.DiggerItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import org.joml.Matrix4f;

import java.util.ArrayList;
import java.util.List;

public class ToolHookBakedModel implements CustomBakedModel {
    private final BakedModel tripwireHook;
    private final ItemModelShaper itemModelShaper;
    private final ModelState rotation;

    public ToolHookBakedModel(BakedModel tripwireHook, ModelState state) {
        this.tripwireHook = tripwireHook;
        this.itemModelShaper = Minecraft.getInstance().getItemRenderer().getItemModelShaper();
        this.rotation = state;
    }

    @Override
    public List<BakedQuad> getBlockQuads(BlockState state, Direction side, RandomSource rand, RenderType renderType, ExtraModelData data) {

        List<BakedQuad> quads = new ArrayList<>();

        try {
            var tripwireQuads = tripwireHook.getQuads(state, side, rand);
            Matrix4f mat = new Matrix4f();
            mat.translate(0, 5 / 16f, 0);

            BakedQuadsTransformer transformer = BakedQuadsTransformer.create()
                    .applyingTransform(mat);
            quads.addAll(transformer.transformAll(tripwireQuads));

        } catch (Exception ignored) {
        }

        if (side == null) {
            try {
                boolean fancy = Boolean.TRUE.equals(data.get(ModBlockProperties.FANCY));
                ItemStack item = data.get(ModBlockProperties.ITEM);
                if (!fancy && !item.isEmpty()) {

                    var itemModel = itemModelShaper.getItemModel(item);

                    float scale = 12 / 16f;
                    Matrix4f mat = new Matrix4f();
                    float x = item.getItem() instanceof DiggerItem ? 1 / 16f : 0;

                    mat.translate(1f, 1f, 1f);
                    mat.mul(rotation.getRotation().getMatrix());
                    mat.rotate(Axis.ZP.rotationDegrees(225));
                    mat.scale(scale, scale, scale);
                    mat.translate(-1f, -1f, -1f);
                    mat.translate(-x, 0, 1.4f / (16f * scale));

                    BakedQuadsTransformer transformer = BakedQuadsTransformer.create()
                            .applyingTransform(mat);

                    quads.addAll(transformer.transformAll(itemModel.getQuads(null, null, rand)));

                }
            } catch (Exception ignored) {
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
        return tripwireHook.getParticleIcon();
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
