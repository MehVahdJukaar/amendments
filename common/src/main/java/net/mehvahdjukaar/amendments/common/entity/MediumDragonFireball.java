package net.mehvahdjukaar.amendments.common.entity;

import net.mehvahdjukaar.amendments.client.TumblingAnimation;
import net.mehvahdjukaar.amendments.common.ProjectileStats;
import net.mehvahdjukaar.amendments.configs.ClientConfigs;
import net.mehvahdjukaar.amendments.configs.CommonConfigs;
import net.mehvahdjukaar.amendments.reg.ModRegistry;
import net.mehvahdjukaar.moonlight.api.entity.ImprovedProjectileEntity;
import net.mehvahdjukaar.moonlight.api.entity.ParticleTrailEmitter;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.AreaEffectCloud;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;

public class MediumDragonFireball extends ImprovedProjectileEntity implements IVisualTransformationProvider {

    private final ParticleTrailEmitter trailEmitter = makeTrialEmitter(false);
    private final TumblingAnimation tumblingAnimation = ProjectileStats.makeTumbler();

    public MediumDragonFireball(EntityType<? extends MediumDragonFireball> entityType, Level level) {
        super(entityType, level);
    }

    public MediumDragonFireball(Level level, LivingEntity shooter) {
        super(ModRegistry.MEDIUM_DRAGON_FIREBALL.get(), shooter, level);
    }

    public static ParticleTrailEmitter makeTrialEmitter(boolean isLarge) {
        return ParticleTrailEmitter.builder()
                .spacing(0.7)
                .maxParticlesPerTick(5)
                .minSpeed(0.0)
                .build();
    }

    @Override
    public void spawnTrailParticles() {
        super.spawnTrailParticles();
        trailEmitter.tick(this, (p, motion) -> {
            if (this.isInWater()) return;

            // Spawn particle with the calculated direction
            level().addParticle(ParticleTypes.DRAGON_BREATH,
                    p.x, p.y, p.z,
                    random.nextGaussian() * 0.04,
                    random.nextGaussian() * 0.04,
                    random.nextGaussian() * 0.04);
        });
        if (ClientConfigs.CHARGES_TUMBLE.get()) tumblingAnimation.tick(this.random);
    }

    @Override
    public Matrix4f amendments$getVisualTransformation(float partialTicks) {
        return new Matrix4f().rotate(tumblingAnimation.getRotation(partialTicks));
    }

    @Override
    protected void onHit(HitResult result) {
        super.onHit(result);
        if (result.getType() != HitResult.Type.ENTITY || !this.ownedBy(((EntityHitResult) result).getEntity())) {
            if (!this.level().isClientSide) {
                if (!this.isSilent()) {
                    level().broadcastEntityEvent(this, (byte) 3);
                    this.playSound(SoundEvents.DRAGON_FIREBALL_EXPLODE, 1.0F, random.nextFloat() * 0.1F + 0.9F);
                }

                spawnCloud();

                this.discard();
            }
        }
    }

    @Override
    protected void onHitEntity(EntityHitResult result) {
        super.onHitEntity(result);
        var entity = result.getEntity();
        //arbitrary damage on impact
        entity.hurt(level().damageSources().indirectMagic(this, this.getOwner()), 2);
    }

    private void spawnCloud() {
        AreaEffectCloud areaEffectCloud = new AreaEffectCloud(this.level(), this.getX(), this.getY(), this.getZ());
        Entity entity = this.getOwner();
        if (entity instanceof LivingEntity) {
            areaEffectCloud.setOwner((LivingEntity) entity);
        }

        areaEffectCloud.setParticle(ParticleTypes.DRAGON_BREATH);
        areaEffectCloud.setRadius(1.0F);
        areaEffectCloud.setDuration(40);
        areaEffectCloud.setWaitTime(0);
        areaEffectCloud.setRadiusPerTick((6.5F - areaEffectCloud.getRadius()) / (float) areaEffectCloud.getDuration());
        areaEffectCloud.addEffect(new MobEffectInstance(MobEffects.HARM, 1, 1));
        this.level().addFreshEntity(areaEffectCloud);

    }

    @Override
    public @Nullable ItemStack getPickResult() {
        return super.getPickResult();
    }

    @Override
    public boolean isPickable() {
        return CommonConfigs.DEFLECT_FIRE_CHARGES.get();
    }

    @Override
    public float getPickRadius() {
        return 0;
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        return false;
    }

    @Override
    protected Item getDefaultItem() {
        return ModRegistry.DRAGON_CHARGE.get();
    }


    @Override
    public void handleEntityEvent(byte id) {
        super.handleEntityEvent(id);
        Level level = level();
        if (id == 3) {
            for (int x = 0; x < 30; ++x) {
                float f = 0.2f;
                float speed = random.nextFloat() * f;
                float angle = random.nextFloat() * 6.2831855F;
                double sx = (Mth.cos(angle) * speed);
                double sy = 0.01 + random.nextDouble() * f * 0.2;
                double sz = (Mth.sin(angle) * speed);
                level.addParticle(ParticleTypes.DRAGON_BREATH,
                        this.getX() + sx * 0.1, this.getY() + 0.3, this.getZ() + sz * 0.1, sx, sy, sz);
            }
        }
    }
}
