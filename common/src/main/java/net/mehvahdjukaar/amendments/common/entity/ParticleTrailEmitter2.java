package net.mehvahdjukaar.amendments.common.entity;

import net.mehvahdjukaar.moonlight.api.misc.RollingBuffer;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public class ParticleTrailEmitter2 {
    private final double idealSpacing;
    private final int maxParticlesPerTick;
    private final double minSpeed;
    private Vec3 lastEmittedPos = null;
    private double accumulatedDistanceSinceLastParticle;
    private final RollingBuffer<Vec3> previousVelocities = new RollingBuffer(2);
    private final RollingBuffer<Vec3> previousPositions = new RollingBuffer(2);

    private ParticleTrailEmitter2( ParticleTrailEmitter2.Builder builder) {
        this.idealSpacing = builder.idealSpacing;
        this.maxParticlesPerTick = builder.maxParticlesPerTick;
        this.minSpeed = builder.minSpeed;
        this.accumulatedDistanceSinceLastParticle = -this.idealSpacing;
    }

    public void tick(Projectile obj, ParticleOptions particleOptions) {
        this.tick(obj, particleOptions, true);
    }

    public void tick(Projectile obj, ParticleOptions particleOptions, boolean followSpeed) {
        this.tick(obj, (position, velocity) -> {
            Level level = obj.level();
            if (followSpeed) {
                level.addParticle(particleOptions, position.x, position.y, position.z, velocity.x, velocity.y, velocity.z);
            } else {
                level.addParticle(particleOptions, position.x, position.y, position.z, 0.0, 0.0, 0.0);
            }

        });
    }

    public void tick(Projectile obj, ParticleTrailEmitter2.Emitter emitter) {
        Vec3 currentVel = obj.getDeltaMovement();
        Vec3 currentPos = obj.position();
        this.previousVelocities.push(currentVel);
        this.previousPositions.push(currentPos);
        if (this.previousPositions.size() >= 2) {
            Vec3 prevPos = (Vec3)this.previousPositions.get(0);
            Vec3 currentPosBuf = (Vec3)this.previousPositions.get(1);
            Vec3 prevVel = (Vec3)this.previousVelocities.get(0);
            double segmentLength = prevPos.distanceTo(currentPosBuf);
            if (!(segmentLength < this.minSpeed)) {
                float h = obj.getBbHeight() / 2.0F;
                double totalAvailable = this.accumulatedDistanceSinceLastParticle + segmentLength;
                int particlesToEmit = (int)(totalAvailable / this.idealSpacing);
                particlesToEmit = Math.min(particlesToEmit, this.maxParticlesPerTick);
                if (particlesToEmit == 0) {
                    this.accumulatedDistanceSinceLastParticle += segmentLength;
                } else {
                    Vec3 lastPos = this.lastEmittedPos != null ? this.lastEmittedPos : prevPos;
                    double spacingSum = 0.0;

                    for(int i = 1; i <= particlesToEmit; ++i) {
                        double targetDist = (double)i * this.idealSpacing - this.accumulatedDistanceSinceLastParticle;
                        double t = targetDist / segmentLength;
                        t = Math.max(0.0, Math.min(1.0, t));
                        Vec3 emitPos = prevPos.lerp(currentPosBuf, t);
                        Vec3 emitVel = prevVel.lerp((Vec3)this.previousVelocities.get(1), t);
                        Vec3 direction = emitPos.subtract(lastPos).normalize();
                        Vec3 perfectPos = lastPos.add(direction.scale(this.idealSpacing)).add(0.0, (double)h, 0.0);
                        emitter.emitParticle(perfectPos, emitVel);
                        lastPos = perfectPos;
                        spacingSum += this.idealSpacing;
                    }

                    this.lastEmittedPos = lastPos;
                    this.accumulatedDistanceSinceLastParticle = totalAvailable - spacingSum;
                }
            }
        }
    }

    public static ParticleTrailEmitter2.Builder builder() {
        return new ParticleTrailEmitter2.Builder();
    }

    public static class Builder {
        private double idealSpacing = 0.5;
        private int maxParticlesPerTick = 5;
        private double minSpeed = 0.0;

        public Builder() {
        }

        public ParticleTrailEmitter2.Builder spacing(double spacing) {
            this.idealSpacing = spacing;
            return this;
        }

        public ParticleTrailEmitter2.Builder maxParticlesPerTick(int max) {
            this.maxParticlesPerTick = max;
            return this;
        }

        public ParticleTrailEmitter2.Builder minSpeed(double speed) {
            this.minSpeed = speed;
            return this;
        }

        public ParticleTrailEmitter2 build() {
            return new ParticleTrailEmitter2(this);
        }
    }

    public interface Emitter {
        void emitParticle(Vec3 var1, Vec3 var2);
    }
}