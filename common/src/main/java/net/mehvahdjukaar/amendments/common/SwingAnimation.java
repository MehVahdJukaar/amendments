package net.mehvahdjukaar.amendments.common;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.joml.Vector3f;

import java.util.function.Function;

//all client stuff
public abstract class SwingAnimation {

    private final Function<BlockState, Vector3f> axisGetter;
    protected float angle = 0;
    protected float prevAngle = 0;

    protected SwingAnimation(Function<BlockState, Vector3f> axisGetter) {
        this.axisGetter = axisGetter;
    }

    protected Vector3f getRotationAxis(BlockState state) {
        return axisGetter.apply(state);
    }

    public abstract void tick(Level pLevel, BlockPos pPos, BlockState pState);

    public abstract boolean hitByEntity(Entity entity, BlockState state, BlockPos pos);

    public abstract float getAngle(float partialTicks);

    public abstract void setAngle(float angle);

    public abstract void reset();

    public static SwingAnimation EMPTY = new SwingAnimation(null) {

        @Override
        public void tick(Level pLevel, BlockPos pPos, BlockState pState) {
        }

        @Override
        public boolean hitByEntity(Entity entity, BlockState state, BlockPos pos) {
            return false;
        }

        @Override
        public float getAngle(float partialTicks) {
            return 0;
        }

        @Override
        public void setAngle(float angle) {
        }

        @Override
        public void reset() {

        }
    };
}
