package net.mehvahdjukaar.amendments.common.entity;

import net.mehvahdjukaar.amendments.configs.CommonConfigs;
import net.mehvahdjukaar.amendments.reg.ModRegistry;
import net.mehvahdjukaar.moonlight.api.util.math.MthUtils;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.AreaEffectCloud;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.projectile.Fireball;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class SmallDragonFireball extends Fireball {
    public static final float SPLASH_RANGE = 4.0F;

    public SmallDragonFireball(EntityType<? extends SmallDragonFireball> entityType, Level level) {
        super(entityType, level);
    }

    public SmallDragonFireball(Level level, LivingEntity shooter, double offsetX, double offsetY, double offsetZ) {
        super(ModRegistry.SMALL_DRAGON_FIREBALL.get(), shooter, offsetX, offsetY, offsetZ, level);
    }

    protected void onHit(HitResult result) {
        super.onHit(result);
        if (result.getType() != HitResult.Type.ENTITY || !this.ownedBy(((EntityHitResult) result).getEntity())) {
            if (!this.level().isClientSide) {
                List<LivingEntity> list = this.level().getEntitiesOfClass(LivingEntity.class, this.getBoundingBox().inflate(4.0, 2.0, 4.0));
                AreaEffectCloud areaEffectCloud = new AreaEffectCloud(this.level(), this.getX(), this.getY(), this.getZ());
                Entity entity = this.getOwner();
                if (entity instanceof LivingEntity) {
                    areaEffectCloud.setOwner((LivingEntity) entity);
                }

                areaEffectCloud.setParticle(ParticleTypes.DRAGON_BREATH);
                areaEffectCloud.setRadius(3.0F);
                areaEffectCloud.setDuration(600);
                areaEffectCloud.setRadiusPerTick((7.0F - areaEffectCloud.getRadius()) / (float) areaEffectCloud.getDuration());
                areaEffectCloud.addEffect(new MobEffectInstance(MobEffects.HARM, 1, 1));
                if (!list.isEmpty()) {

                    for (LivingEntity livingEntity : list) {
                        double d = this.distanceToSqr(livingEntity);
                        if (d < 16.0) {
                            areaEffectCloud.setPos(livingEntity.getX(), livingEntity.getY(), livingEntity.getZ());
                            break;
                        }
                    }
                }

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

    @Override
    protected ParticleOptions getTrailParticle() {
        return ParticleTypes.DRAGON_BREATH;
    }

    @Override
    protected boolean shouldBurn() {
        return false;
    }

    @Override
    public void tick() {
        //TODO:refactor
        spawnTrailParticles(this, this.position(), this.position().add(this.getDeltaMovement()));
        super.tick();
    }

    private int particleCooldown;

    public static void spawnTrailParticles(SmallDragonFireball entity, Vec3 currentPos, Vec3 newPos) {
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

                        entity.level().addAlwaysVisibleParticle(ModRegistry.DRAGON_FIREBALL_TRAIL_PARTICLE.get(), true,
                                x, y, z, 1, 0.01, 0);
                        entity.level().addAlwaysVisibleParticle(ModRegistry.DRAGON_FIREBALL_TRAIL_PARTICLE.get(), true,
                                x - movement.x, y - movement.y, z - movement.z, 1, 0.01, 0);

                        entity.level().addAlwaysVisibleParticle(ModRegistry.DRAGON_FIREBALL_TRAIL_PARTICLE.get(), true,
                                x - movement.x / 2f, y - movement.y / 2f, z - movement.z / 2f, 1, 0.01, 0);

                        entity.level().addAlwaysVisibleParticle(ModRegistry.DRAGON_FIREBALL_TRAIL_PARTICLE.get(), true,
                                x - movement.x / 4f, y - movement.y / 4f, z - movement.z / 4f, 1, 0.01, 0);


                        entity.level().addAlwaysVisibleParticle(ModRegistry.DRAGON_FIREBALL_TRAIL_PARTICLE.get(), true,
                                x - movement.x * 3 / 4f, y - movement.y * 3 / 4f, z - movement.z * 3 / 4f, 1, 0.01, 0);
                    }
                }
            }
        }
    }
}
