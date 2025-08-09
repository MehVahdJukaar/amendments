package net.mehvahdjukaar.amendments.mixins;

import net.mehvahdjukaar.amendments.client.TumblingAnimation;
import net.mehvahdjukaar.amendments.common.ProjectileStats;
import net.mehvahdjukaar.amendments.common.entity.IVisualTransformationProvider;
import net.mehvahdjukaar.amendments.configs.ClientConfigs;
import net.mehvahdjukaar.amendments.configs.CommonConfigs;
import net.mehvahdjukaar.moonlight.api.entity.ParticleTrailEmitter;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.projectile.Snowball;
import net.minecraft.world.entity.projectile.ThrowableItemProjectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

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
        if (this.isOnFire()) {
            if (!this.level().isClientSide) {
                this.playEntityOnFireExtinguishedSound();
                this.discard();
            }
        }
        if (level().isClientSide) {
            amendments$trailEmitter.tick(this,
                    (p, v) -> {
                        if (this.isInWater()) return;
                        if (random.nextFloat() < 0.85) return;
                        var gx = random.nextGaussian() * 0.015;
                        var gy = random.nextGaussian() * 0.015;
                        var gz = random.nextGaussian() * 0.015;
                        var px = random.triangle(-0.2, 0.2);
                        var py = random.triangle(-0.2, 0.2);
                        var pz = random.triangle(-0.2, 0.2);
                        level().addParticle(ParticleTypes.SNOWFLAKE, p.x + px, p.y + py + 0.1, p.z + pz,
                                gx, gy, gz);
                    }
            );
            if (ClientConfigs.PROJECTILE_TUMBLE.get()) amendments$tumblingAnimation.tick(random);
        }
    }

    @Inject(method = "onHitEntity", at = @At("HEAD"))
    protected void amendments$addFreezing(EntityHitResult result, CallbackInfo ci) {
        super.onHitEntity(result);
        int freeze = CommonConfigs.SNOWBALL_FREEZE.get();
        Entity entity = result.getEntity();

        if (freeze > 0 && !entity.isOnFire() && entity.canFreeze()) {
            int m = entity.getTicksFrozen();
            entity.setTicksFrozen(Math.min(entity.getTicksRequiredToFreeze(), m + freeze));
        }
    }

    @Inject(method = "handleEntityEvent", at = @At("HEAD"))
    protected void amendments$addParticles(byte id, CallbackInfo ci) {
        if (id == 3) {
            for (int i = 8; i > 0; --i) {
                double x = this.getRandomX(1);
                double y = this.getRandomY();
                double z = this.getRandomZ(1);
                double vx = this.random.nextGaussian() * 0.035;
                double vy = this.random.nextGaussian() * 0.015 * 0.02;
                double vz = this.random.nextGaussian() * 0.035;
                level().addParticle(ParticleTypes.SNOWFLAKE, x, y, z, vx, vy, vz);
            }
        }
    }
}
