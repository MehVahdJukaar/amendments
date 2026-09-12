package net.mehvahdjukaar.amendments.client.renderers;

import com.mojang.math.Axis;
import com.mojang.math.Transformation;
import net.minecraft.client.renderer.blockentity.state.SignRenderState;
import net.minecraft.world.level.block.StandingSignBlock;
import net.minecraft.world.level.block.WallSignBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.RotationSegment;
import org.joml.Matrix4f;
import org.joml.Vector3f;

public class SignRendererExtension {

    public static final Vector3f TEXT_OFFSET = new Vector3f(0, -1 / 32f - (1 / 32f) / 3, 1 / 16f + 0.001f);

    //vanilla StandingSignRenderer text scale
    private static final float TEXT_SCALE = 0.010416667F;

    //pushes the text out of the block model, which sits deeper than the entity model
    private static final float WALL_OFFSET = 0.125f;

    public static void makePixelConsistent(SignRenderState state, BlockState blockState) {
        boolean wall = blockState.getBlock() instanceof WallSignBlock;
        float angle = wall ? blockState.getValue(WallSignBlock.FACING).toYRot() :
                RotationSegment.convertToDegrees(blockState.getValue(StandingSignBlock.ROTATION));

        state.transformations = new SignRenderState.SignTransformations(state.transformations.body(),
                textTransformation(angle, wall, true), textTransformation(angle, wall, false));
        ((ExtendedSignRenderState) state).amendments$setPixelConsistent(true);
    }

    private static Transformation textTransformation(float angle, boolean wall, boolean front) {
        Matrix4f matrix = new Matrix4f().translate(0.5f, 0.5f, 0.5f).rotate(Axis.YP.rotationDegrees(-angle));
        if (wall) matrix.translate(0, -0.3125f + WALL_OFFSET, -0.4375f);
        if (!front) matrix.rotate(Axis.YP.rotationDegrees(180));
        matrix.translate(TEXT_OFFSET);
        matrix.scale(TEXT_SCALE, -TEXT_SCALE, TEXT_SCALE);
        return new Transformation(matrix);
    }
}
