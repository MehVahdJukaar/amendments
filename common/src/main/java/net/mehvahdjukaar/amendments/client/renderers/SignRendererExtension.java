package net.mehvahdjukaar.amendments.client.renderers;

import com.mojang.blaze3d.vertex.PoseStack;
import net.mehvahdjukaar.moonlight.api.client.util.RotHlpr;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.StandingSignBlock;
import net.minecraft.world.level.block.WallHangingSignBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

public class SignRendererExtension {

    public static final Vec3 TEXT_OFFSET = new Vec3(0.0D, -1 / 32f - (1 / 32f) / 3, 1 / 16f + 0.001);


    public static void renderSignBlockModelInGui(GuiGraphics guiGraphics, boolean isWall, BlockState state, boolean flipped) {
        if (isWall) {
            state = state.setValue(WallHangingSignBlock.FACING, Direction.SOUTH);
        } else {
            state = state.setValue(StandingSignBlock.ROTATION, 0);
        }
        PoseStack pose = guiGraphics.pose();
        pose.scale(93.75f, -93.75f, 93.75f);


        pose.translate(0, 0, -0.125D);

        if (flipped) {
            pose.mulPose(RotHlpr.Y180);
        }
        pose.translate(-0.5F,
                -0.5F - 14 / 64f + (isWall ? 7 / 32f : 0),
                -0.5F);

        Minecraft.getInstance().getBlockRenderer().getModelRenderer()
                .renderModel(pose.last(),
                        guiGraphics.bufferSource().getBuffer(RenderType.cutout()),
                        state,
                        Minecraft.getInstance().getBlockRenderer().getBlockModel(state),
                        1, 1, 1, 15728880, OverlayTexture.NO_OVERLAY);
    }


    public static void translateWall(PoseStack poseStack) {
        poseStack.translate(0, 0.125f,0);


    }
}
