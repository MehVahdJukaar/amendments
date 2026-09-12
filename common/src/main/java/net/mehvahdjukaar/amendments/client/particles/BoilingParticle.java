package net.mehvahdjukaar.amendments.client.particles;

import net.mehvahdjukaar.moonlight.api.util.math.MthUtils;
import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.SingleQuadParticle;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.client.renderer.state.level.QuadParticleRenderState;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.ARGB;
import net.minecraft.util.LightCoordsUtil;
import net.minecraft.util.RandomSource;

public class BoilingParticle extends SingleQuadParticle {

    private final SpriteSet sprites;
    private final TextureAtlasSprite shineSprite;
    private final double waterLevel;
    private final double growFactor;
    private final int lightLevel;
    private int maxTry = 200;

    BoilingParticle(ClientLevel level, double x, double y, double z, double waterLevel, int lightLevel, SpriteSet sprites) {
        super(level, x, y, z, sprites.first());
        this.setSize(0.0625F, 0.0625F);
        this.quadSize *= this.random.nextFloat() * 0.4F + 0.16F;
        this.xd = (level.getRandom().nextFloat() * 2.0 - 1.0) * 0.005;
        this.yd = (level.getRandom().nextFloat() * 2.0 - 1.0) * 0.005;
        this.zd = (level.getRandom().nextFloat() * 2.0 - 1.0) * 0.005;
        this.lifetime = (int) (MthUtils.nextWeighted(level.getRandom(), 26, 1, 5));
        this.sprites = sprites;
        this.setSpriteFromAge(this.sprites);
        this.shineSprite = sprites.get(0, 6);
        this.waterLevel = waterLevel;
        this.growFactor = 0.002 + level.getRandom().nextFloat() * 0.004;
        this.lightLevel = lightLevel;
    }

    @Override
    public void tick() {

        this.xo = this.x;
        this.yo = this.y;
        this.zo = this.z;

        if (this.maxTry-- <= 0) {
            this.remove();
            return;
        }

        this.move(this.xd, this.yd, this.zd);

        if (y < waterLevel) this.yd += 0.002;
        else {
            y = waterLevel;
            this.quadSize += growFactor;
            this.yd = 0;
            if (this.lifetime-- <= 0) {
                this.remove();
                return;
            }
        }

        this.yd *= 0.995;
        this.xd *= 0.98;
        this.zd *= 0.98;

        if (!this.level.getBlockState(BlockPos.containing(this.x, this.y, this.z)).is(BlockTags.CAULDRONS)) {
            this.remove();
        }

        this.setSpriteFromAge(this.sprites);
    }

    @Override
    public void extract(QuadParticleRenderState renderState, Camera renderInfo, float partialTicks) {
        if (this.lifetime - this.age >= 4) {
            var old = sprite;
            float oldRed = rCol;
            float oldGreen = gCol;
            float oldBlue = bCol;
            this.setColor(1, 1, 1);
            this.setSprite(shineSprite);
            super.extract(renderState, renderInfo, partialTicks);
            this.setColor(oldRed, oldGreen, oldBlue);
            this.setSprite(old);
        }

        super.extract(renderState, renderInfo, partialTicks);
    }

    @Override
    protected int getLightCoords(float partialTick) {
        int lightColor = super.getLightCoords(partialTick);
        int sky = LightCoordsUtil.sky(lightColor);
        int block = LightCoordsUtil.block(lightColor);
        return LightCoordsUtil.pack(Math.max(lightLevel, block), sky);
    }

    @Override
    public void setSpriteFromAge(SpriteSet sprite) {
        if (!this.removed) {
            int newAge = Math.max(1, 5 - (this.lifetime - this.age));
            this.setSprite(sprite.get(newAge, 5));
        }
    }

    @Override
    protected Layer getLayer() {
        return Layer.OPAQUE_TERRAIN;
    }

    public static class Provider implements ParticleProvider<SimpleParticleType> {
        private final SpriteSet sprite;

        public Provider(SpriteSet sprites) {
            this.sprite = sprites;
        }

        @Override
        public Particle createParticle(SimpleParticleType type, ClientLevel level, double x, double endY, double z,
                                       double color, double startY, double light, RandomSource random) {
            int intColor = (int) color;
            float r = ARGB.red(intColor) / 255f;
            float g = ARGB.green(intColor) / 255f;
            float b = ARGB.blue(intColor) / 255f;

            var particle = new BoilingParticle(level, x, startY, z, endY,(int)light, sprite);
            particle.setColor(r, g, b);
            return particle;
        }
    }
}
