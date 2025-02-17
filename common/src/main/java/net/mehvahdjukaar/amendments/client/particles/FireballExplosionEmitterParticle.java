package net.mehvahdjukaar.amendments.client.particles;

import net.mehvahdjukaar.amendments.reg.ModRegistry;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.NoRenderParticle;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.core.particles.SimpleParticleType;

public class FireballExplosionEmitterParticle extends NoRenderParticle {
    private final double size;
    private int life;

    FireballExplosionEmitterParticle(ClientLevel clientLevel, double x, double y, double z, double size) {
        super(clientLevel, x, y, z, 0.0, 0.0, 0.0);
        this.size = size+1;
    }

    @Override
    public void tick() {
        int lifeTime = 8;
        for (int i = 0; i < 6; ++i) {
            double d = this.x + (this.random.nextDouble() - this.random.nextDouble()) * size;
            double e = this.y + (this.random.nextDouble() - this.random.nextDouble()) * size;
            double f = this.z + (this.random.nextDouble() - this.random.nextDouble()) * size;
            this.level.addParticle(ModRegistry.FIREBALL_EXPLOSION_PARTICLE.get(), d, e, f, (float) this.life / (float) lifeTime, 0.0, 0.0);
        }

        ++this.life;
        if (this.life == lifeTime) {
            this.remove();
        }

    }

    public static class Factory implements ParticleProvider<SimpleParticleType> {
        public Factory(SpriteSet sprite) {
        }

        public Particle createParticle(SimpleParticleType type, ClientLevel level, double x, double y, double z, double size, double ySpeed, double zSpeed) {
            return new FireballExplosionEmitterParticle(level, x, y, z, size);
        }
    }
}

