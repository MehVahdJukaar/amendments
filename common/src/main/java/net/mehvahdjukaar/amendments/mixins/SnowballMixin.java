package net.mehvahdjukaar.amendments.mixins;

import net.mehvahdjukaar.amendments.common.ProjectileStats;
import net.mehvahdjukaar.amendments.common.entity.IVisualTransformationProvider;
import net.mehvahdjukaar.amendments.common.entity.TumblingAnimation;
import net.mehvahdjukaar.amendments.configs.ClientConfigs;
import net.mehvahdjukaar.moonlight.api.entity.ParticleTrailEmitter;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.projectile.Snowball;
import net.minecraft.world.entity.projectile.ThrowableItemProjectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(Snowball.class)
public abstract class SnowballMixin extends ThrowableItemProjectile implements IVisualTransformationProvider {

    @Unique
    private final ParticleTrailEmitter amendments$trailEmitter = ProjectileStats.makeSnowballTrialEmitter();
    @Unique
    private final TumblingAnimation amendments$tumblingAnimation = ProjectileStats.makeFasterTumbler();

    public SnowballMixin(EntityType<? extends ThrowableItemProjectile> entityType, Level level) {
        super(entityType, level);
    }

    @Override
    public Matrix4f amendments$getVisualTransformation(float partialTicks) {
        return new Matrix4f().rotate(this.amendments$tumblingAnimation.getRotation(partialTicks));
    }

    @Override
    public void tick() {
        super.tick();
        if (level().isClientSide) {
            amendments$trailEmitter.tick(this,
                    (p, v) -> {
                        if (this.isInWater()) return;
                        if(random.nextFloat()<0.8)return;
                        var gx = random.nextGaussian() * 0.02;
                        var gy = random.nextGaussian() * 0.02;
                        var gz = random.nextGaussian() * 0.02;
                        level().addParticle(ParticleTypes.SNOWFLAKE, p.x, p.y, p.z,
                                gx, gy, gz);
                    }
            );
            if (ClientConfigs.PROJECTILE_TUMBLE.get())  amendments$tumblingAnimation.tick(random);
        }
    }

    @Override
    protected void onHitEntity(EntityHitResult result) {
        super.onHitEntity(result);
        //TODO:
    }

    @Override
    protected void onHit(HitResult result) {
        super.onHit(result);
        //TODO: extra particles
    }
}
