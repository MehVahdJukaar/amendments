package net.mehvahdjukaar.amendments.mixins;

import net.mehvahdjukaar.amendments.common.FireballStats;
import net.mehvahdjukaar.amendments.common.entity.IVisualRotationProvider;
import net.mehvahdjukaar.amendments.common.entity.MediumDragonFireball;
import net.mehvahdjukaar.amendments.common.entity.MediumFireball;
import net.mehvahdjukaar.amendments.common.entity.TumblingAnimation;
import net.mehvahdjukaar.amendments.configs.ClientConfigs;
import net.mehvahdjukaar.amendments.reg.ModRegistry;
import net.mehvahdjukaar.moonlight.api.entity.ParticleTrailEmitter;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.DragonBreathParticle;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.projectile.AbstractHurtingProjectile;
import net.minecraft.world.entity.projectile.DragonFireball;
import net.minecraft.world.entity.projectile.ItemSupplier;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;
import org.joml.Quaternionf;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

@Mixin(DragonFireball.class)
public abstract class DragonFireballMixin extends AbstractHurtingProjectile implements  IVisualRotationProvider {

    //will spawn twice as many particles. not ideal but the ball is bigger anyway
    @Unique
    private final ParticleTrailEmitter amendments$trailEmitter = MediumDragonFireball.makeTrialEmitter(true);
    @Unique
    private final TumblingAnimation amendments$tumblingAnimation = FireballStats.makeTumbler();

    public DragonFireballMixin(EntityType<? extends AbstractHurtingProjectile> entityType, double x, double y, double z, double offsetX, double offsetY, double offsetZ, Level level) {
        super(entityType, x, y, z, offsetX, offsetY, offsetZ, level);
    }

    @Override
    public Quaternionf amendments$getVisualRotation(float partialTicks) {
        return this.amendments$tumblingAnimation.getRotation(partialTicks);
    }

    @Override
    public void tick() {
        super.tick();
        if(level().isClientSide){
            amendments$trailEmitter.tick(this, (p, motion) -> {
                if (this.isInWater()) return;

                // Spawn particle with the calculated direction
                level().addParticle(ParticleTypes.DRAGON_BREATH,
                        p.x, p.y, p.z,
                        random.nextGaussian() * 0.05,
                        random.nextGaussian() * 0.05,
                        random.nextGaussian() * 0.05);
            });
        }
    }
}
