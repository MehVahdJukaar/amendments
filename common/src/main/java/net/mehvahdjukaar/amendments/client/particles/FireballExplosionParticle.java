package net.mehvahdjukaar.amendments.client.particles;

import com.google.common.base.Suppliers;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.*;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.util.RandomSource;

import java.util.List;
import java.util.function.Supplier;

public class FireballExplosionParticle extends TextureSheetParticle {
    private final SpriteSet sprites;

    protected FireballExplosionParticle(ClientLevel clientLevel, double d, double e, double f, double size, SpriteSet spriteSet) {
        super(clientLevel, d, e, f, 0.0, 0.0, 0.0);
        this.lifetime = 5 + this.random.nextInt(3);
        this.quadSize = 2.0F * (1.0F - (float) size * 0.5F);
        this.sprites = spriteSet;
        this.bCol = 0.7f + this.random.nextFloat() * 0.3f;
        this.gCol = 0.9f + this.random.nextFloat() * 0.1f;
        this.setSpriteFromAge(spriteSet);
    }

    @Override
    public int getLightColor(float partialTick) {
        return LightTexture.FULL_BRIGHT;
    }

    @Override
    public void tick() {
        this.xo = this.x;
        this.yo = this.y;
        this.zo = this.z;
        if (this.age++ >= this.lifetime) {
            this.remove();
        } else {
            this.setSpriteFromAge(this.sprites);
        }
    }

    @Override
    public ParticleRenderType getRenderType() {
        return ParticleRenderType.PARTICLE_SHEET_LIT;
    }

    public static class Factory implements ParticleProvider<SimpleParticleType> {
        private final Supplier<SpriteSet> sprites1;
        private final Supplier<SpriteSet> sprites2;
        private final Supplier<SpriteSet> sprites3;

        public Factory(SpriteSet sprites) {
            int masSprites = 5 * 3;
            sprites1 = Suppliers.memoize(() -> new MutableSpriteSet(new TextureAtlasSprite[]{
                    sprites.get(1, masSprites),
                    sprites.get(2, masSprites),
                    sprites.get(3, masSprites),
                    sprites.get(4, masSprites),
                    sprites.get(5, masSprites)
            }));
            sprites2 = Suppliers.memoize(() -> new MutableSpriteSet(new TextureAtlasSprite[]{
                    sprites.get(6, masSprites),
                    sprites.get(7, masSprites),
                    sprites.get(8, masSprites),
                    sprites.get(9, masSprites),
                    sprites.get(10, masSprites)
            }));
            sprites3 = Suppliers.memoize(() -> new MutableSpriteSet(new TextureAtlasSprite[]{
                    sprites.get(11, masSprites),
                    sprites.get(12, masSprites),
                    sprites.get(13, masSprites),
                    sprites.get(14, masSprites),
                    sprites.get(15, masSprites)
            }));
        }

        public Particle createParticle(SimpleParticleType type, ClientLevel level, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed) {
            // making 3 random sub sprite sets
            int i = level.random.nextInt(2);
            if (i == 0) {
                return new FireballExplosionParticle(level, x, y, z, xSpeed, this.sprites1.get());
            } else if (i == 1) {
                return new FireballExplosionParticle(level, x, y, z, xSpeed, this.sprites2.get());
            } else {
                return new FireballExplosionParticle(level, x, y, z, xSpeed, this.sprites3.get());
            }
        }
    }

    static class MutableSpriteSet implements SpriteSet {
        private final List<TextureAtlasSprite> sprites;

        public MutableSpriteSet(TextureAtlasSprite[] s1) {
            sprites = List.of(s1);
        }

        @Override
        public TextureAtlasSprite get(int age, int lifetime) {
            return this.sprites.get(age * (this.sprites.size() - 1) / lifetime);
        }

        @Override
        public TextureAtlasSprite get(RandomSource random) {
            return this.sprites.get(random.nextInt(this.sprites.size()));
        }

    }
}
