package net.mehvahdjukaar.amendments.client;

import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;

public class ElasticAnimation {

    private static final int MAX_SQUISH_TICKS = 10;
    private float squosh = 1;
    private float oldSquosh = 1;
    private float squish = 1;
    private float oldSquish = 1;
    private int squishTicks = 0;


    public void tick(Vec3 movement) {

        squishTicks = Math.max(0, squishTicks - 1);

        double speed = Mth.clamp(movement.lengthSqr() * 3, 0, 1);
        oldSquosh = squosh;
        oldSquish = squish;
        squosh = (float) Math.max(0.3, (1 + speed - (1.0f * squishTicks / MAX_SQUISH_TICKS)));
        squish = 1 / Mth.sqrt(squosh);
    }


    public Vector3f getScale(float partialTicks) {
        float squishFactor = Mth.lerp(partialTicks, oldSquish, squish);
        float squoshFactor = Mth.lerp(partialTicks, oldSquosh, squosh);
        return new Vector3f(squishFactor, squishFactor, squoshFactor);
    }

    public void setSquishedDown() {
        squishTicks = MAX_SQUISH_TICKS;
    }
}
