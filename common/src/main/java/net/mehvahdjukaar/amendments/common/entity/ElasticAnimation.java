package net.mehvahdjukaar.amendments.common.entity;

import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.spongepowered.asm.mixin.Unique;

public class ElasticAnimation {

    @Unique
    private float amendments$squosh = 1;
    @Unique
    private float amendments$oldSquosh = 1;
    @Unique
    private float amendments$squish = 1;
    @Unique
    private float amendments$oldSquish = 1;
    @Unique
    private int amendments$squishTicks = 0;


    public void tick(Vec3 movement) {

        amendments$squishTicks = Math.max(0, amendments$squishTicks - 1);

        double speed = Mth.clamp(movement.lengthSqr() * 5, 0, 1);
        amendments$oldSquosh = amendments$squosh;
        amendments$oldSquish = amendments$squish;
        amendments$squosh = (float) (1 + speed - 0.75 * amendments$squishTicks / 20f);
        amendments$squish = 1 / Mth.sqrt(amendments$squosh);
    }


    public Vector3f getScale(float partialTicks) {
        float squishFactor = Mth.lerp(partialTicks, amendments$oldSquish, amendments$squish);
        float squoshFactor = Mth.lerp(partialTicks, amendments$oldSquosh, amendments$squosh);
        return new Vector3f(squishFactor, squishFactor, squoshFactor);
    }

    public void setSquishDownTicks(int i) {
        amendments$squishTicks = i;
    }
}
