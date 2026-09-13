package net.mehvahdjukaar.amendments.mixins;

import net.mehvahdjukaar.amendments.configs.ClientConfigs;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.DragonBreathParticle;
import net.minecraft.client.particle.Particle;
import net.minecraft.util.LightCoordsUtil;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(DragonBreathParticle.class)
public abstract class DragonBreathParticleMixin extends Particle {
    protected DragonBreathParticleMixin(ClientLevel level, double x, double y, double z) {
        super(level, x, y, z);
    }

    @Override
    protected int getLightCoords(float partialTick) {
        int light = super.getLightCoords(partialTick);
        if (!ClientConfigs.DRAGON_BREATH_EMISSIBE.get()) {
            return light;
        }
        int sky = LightCoordsUtil.sky(light);
        int block = Math.max(LightCoordsUtil.block(light), 10);
        return LightCoordsUtil.pack(block, sky);
    }
}
