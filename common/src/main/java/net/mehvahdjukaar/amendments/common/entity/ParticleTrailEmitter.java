package net.mehvahdjukaar.amendments.common.entity;

import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public class ParticleTrailEmitter {

    private final double idealSpacing;
    private final int maxParticlesPerTick;
    private final double minSpeed;
    private final Emitter emitter;
    private Vec3 lastEmittedPos = null; // Track last emitted particle position

    private double accumulatedDistanceSinceLastParticle;

    private final RollingBuffer<Vec3> previousVelocities = new RollingBuffer<>(3);
    private final RollingBuffer<Vec3> previousPositions = new RollingBuffer<>(3);

    private ParticleTrailEmitter(Builder builder) {
        this.idealSpacing = builder.idealSpacing;
        this.maxParticlesPerTick = builder.maxParticlesPerTick;
        this.minSpeed = builder.minSpeed;
        this.emitter = builder.emitter;
        this.accumulatedDistanceSinceLastParticle = -idealSpacing; // delay first particle emission
    }


    public void tick(Projectile obj) {
        Vec3 currentVel = obj.getDeltaMovement();
        Vec3 currentPos = obj.position();

        previousVelocities.push(currentVel);
        previousPositions.push(currentPos);

        if (previousPositions.size() < 2) return;

        Vec3 prevPos = previousPositions.get(0);
        Vec3 currentPosBuf = previousPositions.get(1);
        Vec3 prevVel = previousVelocities.get(0);

        double segmentLength = prevPos.distanceTo(currentPosBuf);
        if (segmentLength < minSpeed) return;

        // Calculate how many particles we can emit
        double totalAvailable = accumulatedDistanceSinceLastParticle + segmentLength;
        int particlesToEmit = (int)(totalAvailable / idealSpacing);
        particlesToEmit = Math.min(particlesToEmit, maxParticlesPerTick);

        if (particlesToEmit == 0) {
            accumulatedDistanceSinceLastParticle += segmentLength;
            return;
        }

        // Calculate exact emission points
        Vec3 lastPos = (lastEmittedPos != null) ? lastEmittedPos : prevPos;
        double spacingSum = 0;

        for (int i = 1; i <= particlesToEmit; i++) {
            double targetDist = i * idealSpacing - accumulatedDistanceSinceLastParticle;
            double t = targetDist / segmentLength;
            t = Math.max(0, Math.min(1, t)); // Clamp to segment bounds

            Vec3 emitPos = prevPos.lerp(currentPosBuf, t);
            Vec3 emitVel = prevVel.lerp(previousVelocities.get(1), t);

            // Ensure perfect spacing
            Vec3 direction = emitPos.subtract(lastPos).normalize();
            Vec3 perfectPos = lastPos.add(direction.scale(idealSpacing));

            emitter.emitParticle(obj.level(), perfectPos, emitVel);

            // Debug output
            double actualDist = perfectPos.distanceTo(lastPos);
            System.out.printf("Particle %d | Dist: %.6f | Ideal: %.6f%n",
                    i, actualDist, idealSpacing);

            lastPos = perfectPos;
            spacingSum += idealSpacing;
        }

        // Update state
        lastEmittedPos = lastPos;
        accumulatedDistanceSinceLastParticle = totalAvailable - spacingSum;
    }

    public static Builder builder() {
        return new Builder();
    }

    // === Builder Class ===
    public static class Builder {
        private double idealSpacing = 0.5;
        private int maxParticlesPerTick = 5;
        private double minSpeed = 0.0;
        private Emitter emitter = (level, position, velocity) -> {
            // Default emitter does nothing
        };

        public Builder particle(Emitter emitter) {
            this.emitter = emitter;
            return this;
        }

        public Builder particle(ParticleOptions opt) {
            return particle(opt, true);
        }

        public Builder particle(ParticleOptions opt, boolean followsSpeed) {
            this.emitter = (level, position, velocity) -> {
                if (followsSpeed) {
                    level.addParticle(opt, position.x, position.y, position.z, velocity.x, velocity.y, velocity.z);
                } else {
                    level.addParticle(opt, position.x, position.y, position.z, 0, 0, 0);
                }
            };
            return this;
        }

        public Builder spacing(double spacing) {
            this.idealSpacing = spacing;
            return this;
        }

        public Builder maxParticlesPerTick(int max) {
            this.maxParticlesPerTick = max;
            return this;
        }

        public Builder minSpeed(double speed) {
            this.minSpeed = speed;
            return this;
        }

        public ParticleTrailEmitter build() {
            return new ParticleTrailEmitter(this);
        }
    }

    public interface Emitter {
        void emitParticle(Level level, Vec3 position, Vec3 velocity);
    }
}
