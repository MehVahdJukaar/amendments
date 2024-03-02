package net.mehvahdjukaar.amendments.client.particles;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.SplashParticle;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.util.FastColor;

public class ColoredSplashParticle implements ParticleProvider<SimpleParticleType> {
    private final SplashParticle.Provider provider;
    private final SpriteSet sprites;

    public ColoredSplashParticle(SpriteSet sprites) {
        this.provider = new SplashParticle.Provider(sprites);
        this.sprites = sprites;
    }


    public Particle createParticle(SimpleParticleType type, ClientLevel level, double x, double y, double z, double color, double aa, double ab) {
        int intColor = (int) color;
        float r = FastColor.ARGB32.red(intColor) / 255f;
        float g = FastColor.ARGB32.green(intColor) / 255f;
        float b = FastColor.ARGB32.blue(intColor) / 255f;

        SplashParticle  p = (SplashParticle) provider.createParticle(type, level, x,y,z,0,0,0);
        p.setColor( r,  g,  b);
        return p;
    }
}
