package net.mehvahdjukaar.amendments.common.entity;

import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public class ParticleTrailEmitter {

    private final double idealSpacing;
    private final int maxParticlesPerTick;
    private final double minSpeed;
    private final double gravity;
    private final Emitter emitter;
    private Vec3 lastEmittedPos = null; // Track last emitted particle position

    private double accumulatedDistanceSinceLastParticle;

    private final RollingBuffer<Vec3> previousVelocities = new RollingBuffer<>(3);
    private final RollingBuffer<Vec3> previousPositions = new RollingBuffer<>(3);

    private ParticleTrailEmitter(Builder builder) {
        this.idealSpacing = builder.idealSpacing;
        this.maxParticlesPerTick = builder.maxParticlesPerTick;
        this.minSpeed = builder.minSpeed;
        this.gravity = builder.gravity;
        this.emitter = builder.emitter;
        this.accumulatedDistanceSinceLastParticle = -idealSpacing; // delay first particle emission
    }


    public void tick(Projectile obj) {
        Vec3 currentVel = obj.getDeltaMovement();
        Vec3 currentPos = obj.position();

        previousVelocities.push(currentVel);
        previousPositions.push(currentPos);

        if (!previousPositions.isFull()) return;

        Vec3 prevPos = previousPositions.get(0);
        Vec3 medPos = previousPositions.get(1);
        Vec3 prevVel = previousVelocities.get(0);
        Vec3 medVel = previousVelocities.get(1);

        double segmentLength = prevPos.distanceTo(medPos);
        if (segmentLength < minSpeed) return;

        // Calculate how many particles we should emit
        double totalNeeded = idealSpacing - accumulatedDistanceSinceLastParticle;
        int particlesToEmit = 0;
        double remainingDistance = segmentLength;

        while (remainingDistance >= totalNeeded && particlesToEmit < maxParticlesPerTick) {
            particlesToEmit++;
            totalNeeded += idealSpacing;
            remainingDistance = segmentLength - (totalNeeded - idealSpacing - accumulatedDistanceSinceLastParticle);
        }

        if (particlesToEmit == 0) {
            accumulatedDistanceSinceLastParticle += segmentLength;
            return;
        }

        // Calculate exact emission points
        double accumulated = accumulatedDistanceSinceLastParticle;
        Vec3 lastPos = (lastEmittedPos != null) ? lastEmittedPos : prevPos;

        for (int i = 0; i < particlesToEmit; i++) {
            double distFromStart = accumulated + idealSpacing;
            double t = distFromStart / (accumulatedDistanceSinceLastParticle + segmentLength);
            t = Math.min(Math.max(t, 0), 1);

            Vec3 emitPos = interpolateParabola(prevPos, medPos, t);
            Vec3 emitVel = prevVel.lerp(medVel, t);

            // Calculate actual distance from previous particle
            double actualDistance = emitPos.distanceTo(lastPos);
            double error = actualDistance - idealSpacing;

            // Adjust position to correct spacing
            if (Math.abs(error) > 0.001) {
                Vec3 direction = emitPos.subtract(lastPos).normalize();
                emitPos = lastPos.add(direction.scale(idealSpacing));
            }

            emitter.emitParticle(obj.level(), emitPos, emitVel);

            // Debug output
            System.out.printf("Particle | Dist: %.6f | Ideal: %.6f | Error: %.6f%n",
                    actualDistance, idealSpacing, error);

            lastPos = emitPos;
            accumulated += idealSpacing;
        }

        // Update state for next tick
        lastEmittedPos = lastPos;
        accumulatedDistanceSinceLastParticle = segmentLength - (accumulated - accumulatedDistanceSinceLastParticle);
    }

    private Vec3 interpolateParabola(Vec3 from, Vec3 to, double t) {
        double x = from.x + (to.x - from.x) * t;
        double z = from.z + (to.z - from.z) * t;

        double y0 = from.y;
        double dy = to.y - from.y;
        double estimatedVy = dy + 0.5 * gravity;
        double y = y0 + estimatedVy * t - 0.5 * gravity * t * t;

        return new Vec3(x, y, z);
    }

    public static Builder builder() {
        return new Builder();
    }

    // === Builder Class ===
    public static class Builder {
        private double idealSpacing = 0.5;
        private int maxParticlesPerTick = 5;
        private double minSpeed = 0.0;
        private double gravity = 0.98;
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

        public Builder gravity(double gravity) {
            this.gravity = gravity;
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
