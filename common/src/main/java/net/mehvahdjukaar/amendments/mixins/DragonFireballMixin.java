package net.mehvahdjukaar.amendments.mixins;

import net.mehvahdjukaar.amendments.client.TumblingAnimation;
import net.mehvahdjukaar.amendments.common.ProjectileStats;
import net.mehvahdjukaar.amendments.common.entity.IVisualTransformationProvider;
import net.mehvahdjukaar.amendments.configs.ClientConfigs;
import net.mehvahdjukaar.moonlight.api.entity.ParticleTrailEmitter;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.particles.PowerParticleOption;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.projectile.hurtingprojectile.AbstractHurtingProjectile;
import net.minecraft.world.entity.projectile.hurtingprojectile.DragonFireball;
import net.minecraft.world.level.Level;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(DragonFireball.class)
public abstract class DragonFireballMixin extends AbstractHurtingProjectile implements IVisualTransformationProvider {

    //will spawn twice as many particles. not ideal but the ball is bigger anyway
    @Unique
    private final ParticleTrailEmitter amendments$trailEmitter = ProjectileStats.makeDragonTrialEmitter(true);
    @Unique
    private final TumblingAnimation amendments$tumblingAnimation = ProjectileStats.makeTumbler();

    protected DragonFireballMixin(EntityType<? extends AbstractHurtingProjectile> entityType, Level level) {
        super(entityType, level);
    }

    @Override
    public Matrix4f amendments$getVisualTransformation(float partialTicks) {
        return new Matrix4f().rotate(amendments$tumblingAnimation.getRotation(partialTicks));
    }

    @Override
    public void tick() {
        super.tick();
        if (level().isClientSide()) {
            if (ClientConfigs.DRAGON_FIREBALL_TRAIL.get()) {
                amendments$trailEmitter.tick(this, (p, motion) -> {
                    if (this.isInWater()) return;

                    // Spawn particle with the calculated direction
                    level().addParticle(PowerParticleOption.create(ParticleTypes.DRAGON_BREATH, 1.0F),
                            p.x, p.y, p.z,
                            random.nextGaussian() * 0.05,
                            random.nextGaussian() * 0.05,
                            random.nextGaussian() * 0.05);
                });
            }
            if (ClientConfigs.CHARGES_TUMBLE.get()) amendments$tumblingAnimation.tick(random);
        }
    }
}
