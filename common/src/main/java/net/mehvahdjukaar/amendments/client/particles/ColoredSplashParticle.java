package net.mehvahdjukaar.amendments.client.particles;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.client.particle.WaterDropParticle;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.util.ARGB;
import net.minecraft.util.LightCoordsUtil;
import net.minecraft.util.RandomSource;

public class ColoredSplashParticle extends WaterDropParticle {
    private final int lightLevel;

    ColoredSplashParticle(ClientLevel clientLevel, double d, double e, double f, int light, SpriteSet sprites) {
        super(clientLevel, d, e, f, sprites.get(clientLevel.getRandom()));
        this.gravity = 0.04F;
        this.lightLevel = light;
    }

    @Override
    protected int getLightCoords(float partialTick) {
        int lightColor = super.getLightCoords(partialTick);
        int sky = LightCoordsUtil.sky(lightColor);
        int block = LightCoordsUtil.block(lightColor);
        return LightCoordsUtil.pack(Math.max(lightLevel, block), sky);
    }

    public static class Provider implements ParticleProvider<SimpleParticleType> {
        private final SpriteSet sprites;

        public Provider(SpriteSet sprites) {
            this.sprites = sprites;
        }

        @Override
        public Particle createParticle(SimpleParticleType type, ClientLevel level, double x, double y, double z,
                                       double color, double unused, double light, RandomSource random) {
            int intColor = (int) color;
            float r = ARGB.red(intColor) / 255f;
            float g = ARGB.green(intColor) / 255f;
            float b = ARGB.blue(intColor) / 255f;

            ColoredSplashParticle p = new ColoredSplashParticle(level, x, y, z, (int) light, this.sprites);
            p.setColor(r, g, b);
            return p;
        }
    }
}
