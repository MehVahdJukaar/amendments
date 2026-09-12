package net.mehvahdjukaar.amendments.client.particles;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.SingleQuadParticle;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.util.LightCoordsUtil;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;

public class FireballTrailParticle extends SingleQuadParticle {
    private final SpriteSet sprites;

    protected FireballTrailParticle(
            ClientLevel clientLevel,
            double x, double y, double z, double size, SpriteSet sprites
    ) {
        super(clientLevel, x, y, z, 0.0, 0.0, 0.0, sprites.first());
        this.friction = 0.96F;
        this.speedUpWhenYMotionIsBlocked = true;
        this.sprites = sprites;
        this.xd *= 0.1;
        this.yd *= 0.1;
        this.zd *= 0.1;
        this.quadSize = (float) (size) + (0.2f * clientLevel.getRandom().nextFloat());
        this.quadSize = Math.max(0.1f, this.quadSize);
        this.lifetime = 20;
        this.setSpriteFromAge(sprites);
        this.roll = Mth.PI * clientLevel.getRandom().nextFloat();
        this.oRoll = roll;
    }

    @Override
    public Layer getLayer() {
        return Layer.OPAQUE_TERRAIN;
    }

    @Override
    public int getLightCoords(float partialTick) {
        int block = (int) Mth.map(this.age + partialTick, 0, this.lifetime, 15, 0);
        int light = super.getLightCoords(partialTick);
        block = Math.max(LightCoordsUtil.block(light), block);
        return LightCoordsUtil.pack(block, LightCoordsUtil.sky(light));
    }

    @Override
    public float getQuadSize(float scaleFactor) {
        return this.quadSize * Mth.clamp(((float) this.age + scaleFactor) / (float) this.lifetime * 32.0F, 0.0F, 1.0F);
    }

    @Override
    public void tick() {
        super.tick();
        this.setSpriteFromAge(this.sprites);
    }

    public static class Provider implements ParticleProvider<SimpleParticleType> {
        private final SpriteSet sprites;

        public Provider(SpriteSet sprite) {
            this.sprites = sprite;
        }

        @Override
        public Particle createParticle(SimpleParticleType type, ClientLevel level, double x, double y, double z,
                                       double size, double ySpeed, double zSpeed, RandomSource random) {
            return new FireballTrailParticle(level, x, y, z, size, sprites);
        }
    }
}
