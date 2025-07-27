package net.mehvahdjukaar.amendments.mixins;

import net.mehvahdjukaar.amendments.configs.ClientConfigs;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.DragonBreathParticle;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.renderer.LightTexture;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(DragonBreathParticle.class)
public abstract class DragonBreathParticleMixin extends Particle {
    protected DragonBreathParticleMixin(ClientLevel level, double x, double y, double z) {
        super(level, x, y, z);
    }

    @Override
    protected int getLightColor(float partialTick) {
        int light = super.getLightColor(partialTick);
        if (!ClientConfigs.DRAGON_BREATH_EMISSIBE.get()) {
            return light;
        }
        int sky = LightTexture.sky(light);
        int block = Math.max(LightTexture.block(light), 10);
        return LightTexture.pack(block, sky);
    }
}
