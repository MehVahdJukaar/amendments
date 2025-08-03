package net.mehvahdjukaar.amendments.client;

import net.minecraft.util.RandomSource;
import org.joml.Quaternionf;

public class TumblingAnimation {

    // Angular velocity (degrees/tick)
    private float speedX, speedY, speedZ;
    // Current and previous orientations
    private final Quaternionf currentOrientation = new Quaternionf().identity();
    private final Quaternionf previousOrientation = new Quaternionf().identity();
    private boolean initialized = false;
    // Configuration (unchanged)
    private final float minAngularVelMag;
    private final float maxAngularVelMag;
    private final float jitterAmount;

    // Constructor unchanged
    public TumblingAnimation(float minMagnitude, float maxMagnitude, float maxJitterAmount) {
        this.minAngularVelMag = minMagnitude;
        this.maxAngularVelMag = maxMagnitude;
        this.jitterAmount = maxJitterAmount;
    }

    private void initRotationSpeeds(RandomSource rand) {
        // Generate random unit vector direction
        float x = rand.nextFloat() * 2f - 1f;
        float y = rand.nextFloat() * 2f - 1f;
        float z = rand.nextFloat() * 2f - 1f;

        float length = (float) Math.sqrt(x * x + y * y + z * z);
        if (length < 1e-6) {
            // Avoid division by zero, default axis
            x = 1f;
            y = 0f;
            z = 0f;
            length = 1f;
        }

        x /= length;
        y /= length;
        z /= length;

        // Random magnitude in range
        float magnitude = minAngularVelMag + rand.nextFloat() * (maxAngularVelMag - minAngularVelMag);

        speedX = x * magnitude;
        speedY = y * magnitude;
        speedZ = z * magnitude;
    }


    public void tick(RandomSource rand) {
        if (!initialized) {
            initRotationSpeeds(rand);
            initialized = true;
        }

        // Apply jitter & clamp magnitude (same as original)
        speedX += (rand.nextFloat() - 0.5f) * jitterAmount;
        speedY += (rand.nextFloat() - 0.5f) * jitterAmount;
        speedZ += (rand.nextFloat() - 0.5f) * jitterAmount;

        float mag = (float) Math.sqrt(speedX * speedX + speedY * speedY + speedZ * speedZ);
        if (mag < minAngularVelMag) {
            float scale = minAngularVelMag / mag;
            speedX *= scale;
            speedY *= scale;
            speedZ *= scale;
            mag = minAngularVelMag;
        } else if (mag > maxAngularVelMag) {
            float scale = maxAngularVelMag / mag;
            speedX *= scale;
            speedY *= scale;
            speedZ *= scale;
            mag = maxAngularVelMag;
        }

        // Save current orientation for interpolation
        previousOrientation.set(currentOrientation);

        // Skip update if angular velocity is negligible
        if (mag < 1e-6f) return;

        // Convert angular velocity to axis-angle in radians
        float angleRad = (float) Math.toRadians(mag);
        float axisX = speedX / mag;
        float axisY = speedY / mag;
        float axisZ = speedZ / mag;

        // Create rotation quaternion from axis-angle
        Quaternionf deltaQ = new Quaternionf().fromAxisAngleRad(axisX, axisY, axisZ, angleRad);

        // Apply rotation (world space)
        currentOrientation.set(deltaQ.mul(currentOrientation));
        currentOrientation.normalize(); // Prevent drift
    }

    public Quaternionf getRotation(float partialTicks) {
        if (!initialized) return new Quaternionf().identity();
        // Interpolate smoothly between orientations
        return previousOrientation.nlerp(currentOrientation, partialTicks, new Quaternionf());
    }
}
