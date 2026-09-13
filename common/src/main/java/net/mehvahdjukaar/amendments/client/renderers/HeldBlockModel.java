package net.mehvahdjukaar.amendments.client.renderers;

import com.mojang.blaze3d.vertex.PoseStack;
import net.mehvahdjukaar.moonlight.api.client.util.RenderUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;

//these items are drawn with their block model so they show up 3d in hand
class HeldBlockModel {

    private static final RandomSource RANDOM = RandomSource.create();

    static void submit(BlockState state, PoseStack poseStack, SubmitNodeCollector collector, int light) {
        BlockStateModel model = Minecraft.getInstance().getModelManager().getBlockStateModelSet().get(state);
        RenderUtil.submitBlockModel(poseStack, collector, model, null, null, state, RANDOM, true,
                light, OverlayTexture.NO_OVERLAY);
    }
}
