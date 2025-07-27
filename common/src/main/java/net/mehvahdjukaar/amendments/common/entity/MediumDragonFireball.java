package net.mehvahdjukaar.amendments.common.entity;

import net.mehvahdjukaar.amendments.Amendments;
import net.mehvahdjukaar.amendments.configs.CommonConfigs;
import net.mehvahdjukaar.amendments.reg.ModRegistry;
import net.mehvahdjukaar.moonlight.api.entity.ImprovedProjectileEntity;
import net.minecraft.core.particles.ParticleTypes;
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

public class MediumDragonFireball extends ImprovedProjectileEntity {


    private final ParticleTrailEmitter trailEmitter = ParticleTrailEmitter.builder()
            .spacing(0.25)
            .maxParticlesPerTick(5)
            .minSpeed(0.0)
            .particle(ParticleTypes.FLAME)
            .build();

    public MediumDragonFireball(EntityType<? extends MediumDragonFireball> entityType, Level level) {
        super(entityType, level);
    }

    public MediumDragonFireball(Level level, LivingEntity shooter) {
        super(ModRegistry.MEDIUM_DRAGON_FIREBALL.get(), shooter, level);
    }


    @Override
    public void spawnTrailParticles() {
        super.spawnTrailParticles();
        trailEmitter.tick(this);
    }

    @Override
    protected void onHit(HitResult result) {
        super.onHit(result);
        if (result.getType() != HitResult.Type.ENTITY || !this.ownedBy(((EntityHitResult) result).getEntity())) {
            if (!this.level().isClientSide) {
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

                this.level().levelEvent(2006, this.blockPosition(), this.isSilent() ? -1 : 1);
                this.level().addFreshEntity(areaEffectCloud);
                this.discard();
            }
        }
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


    /*
    public static void spawnTrailParticles(MediumDragonFireball entity, Vec3 currentPos, Vec3 newPos) {
        if (!entity.noPhysics) {
            double d = entity.getDeltaMovement().length();
            if (entity.tickCount > 1 && d * entity.tickCount > 1.5) {
                if (false) {

                    Vec3 rot = new Vec3(0.325, 0, 0).yRot(entity.tickCount * 0.32f);

                    Vec3 movement = entity.getDeltaMovement();
                    Vec3 offset = MthUtils.changeBasisN(movement, rot);

                    double px = newPos.x + offset.x;
                    double py = newPos.y + offset.y; //+ this.getBbHeight() / 2d;
                    double pz = newPos.z + offset.z;

                    movement = movement.scale(0.25);
                    entity.level().addParticle(ParticleTypes.LARGE_SMOKE, px, py, pz, movement.x, movement.y, movement.z);
                } else {
                    double interval = 4 / (d * 0.95 + 0.05);
                    if (true || entity.particleCooldown > interval) {
                        entity.particleCooldown -= interval;
                        double x = currentPos.x;
                        double y = currentPos.y + entity.getBbHeight() / 2d;
                        double z = currentPos.z;
                        Vec3 movement = entity.getDeltaMovement();
                        movement = movement.scale(1);
                        var r = entity.level().random;
                        entity.level().addAlwaysVisibleParticle(ParticleTypes.DRAGON_BREATH, true,
                                x, y, z, r.nextGaussian() * 0.02, r.nextGaussian() * 0.02, r.nextGaussian() * 0.02);

                        entity.level().addAlwaysVisibleParticle(ParticleTypes.DRAGON_BREATH, true,
                                x - movement.x, y - movement.y, z - movement.z, r.nextGaussian() * 0.02, r.nextGaussian() * 0.02, r.nextGaussian() * 0.02);

                        entity.level().addAlwaysVisibleParticle(ParticleTypes.DRAGON_BREATH, true,
                                x - movement.x / 2f, y - movement.y / 2f, z - movement.z / 2f, r.nextGaussian() * 0.02, r.nextGaussian() * 0.02, r.nextGaussian() * 0.02);

                        entity.level().addAlwaysVisibleParticle(ParticleTypes.DRAGON_BREATH, true,
                                x - movement.x / 4f, y - movement.y / 4f, z - movement.z / 4f, r.nextGaussian() * 0.02, r.nextGaussian() * 0.02, r.nextGaussian() * 0.02);


                        entity.level().addAlwaysVisibleParticle(ParticleTypes.DRAGON_BREATH, true,
                                x - movement.x * 3 / 4f, y - movement.y * 3 / 4f, z - movement.z * 3 / 4f, r.nextGaussian() * 0.02, r.nextGaussian() * 0.02, r.nextGaussian() * 0.02);
                    }
                }
            }
        }
    }
*/

    @Override
    protected Item getDefaultItem() {
        return ModRegistry.DRAGON_CHARGE.get();
    }








}
