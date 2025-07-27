package net.mehvahdjukaar.amendments.common.entity;

import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import org.joml.Quaternionf;

public class TumblingAnimation {

    // Angular velocity vector (degrees per tick)
    private float speedX, speedY, speedZ;

    // Current rotation angles (degrees)
    private float rotX = 0f, rotY = 0f, rotZ = 0f;
    private float lastRotX = 0f, lastRotY = 0f, lastRotZ = 0f;


    private boolean initialized = false;
    // Configuration
    private final float minAngularVelMag;
    private final float maxAngularVelMag;
    private final float jitterAmount; // max jitter added to speeds per tick

    public TumblingAnimation(float minMagnitude, float maxMagnitude, float jitterAmount) {
        this.minAngularVelMag = minMagnitude;
        this.maxAngularVelMag = maxMagnitude;
        this.jitterAmount = jitterAmount;
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

    // Call every tick to update rotation
    public void tick(RandomSource rand) {
        if (!initialized) {
            initRotationSpeeds(rand);
            initialized = true;
        }
        // Add jitter to speeds

        speedX += (rand.nextFloat() - 0.5f) * jitterAmount;
        speedY += (rand.nextFloat() - 0.5f) * jitterAmount;
        speedZ += (rand.nextFloat() - 0.5f) * jitterAmount;

        // Clamp speeds magnitude but keep direction
        float mag = (float) Math.sqrt(speedX * speedX + speedY * speedY + speedZ * speedZ);
        if (mag < minAngularVelMag) {
            float scale = minAngularVelMag / mag;
            speedX *= scale;
            speedY *= scale;
            speedZ *= scale;
        } else if (mag > maxAngularVelMag) {
            float scale = maxAngularVelMag / mag;
            speedX *= scale;
            speedY *= scale;
            speedZ *= scale;
        }

        // Store last rotation angles
        lastRotX = rotX;
        lastRotY = rotY;
        lastRotZ = rotZ;

        // Increment rotation angles
        rotX = (rotX + speedX) % 360f;
        rotY = (rotY + speedY) % 360f;
        rotZ = (rotZ + speedZ) % 360f;
    }

    // Return current rotation as a Quaternion
    public Quaternionf getRotation(float partialTicks) {
        if (!initialized) {
            return new Quaternionf().identity();
        }

        // Interpolate between last and current rotation
        float interpX = Mth.lerp(partialTicks, lastRotX, rotX);
        float interpY = Mth.lerp(partialTicks, lastRotY, rotY);
        float interpZ = Mth.lerp(partialTicks, lastRotZ, rotZ);

        return new Quaternionf()
                .rotateXYZ(
                        Mth.DEG_TO_RAD * interpX,
                        Mth.DEG_TO_RAD * interpY,
                        Mth.DEG_TO_RAD * interpZ
                );
    }
}