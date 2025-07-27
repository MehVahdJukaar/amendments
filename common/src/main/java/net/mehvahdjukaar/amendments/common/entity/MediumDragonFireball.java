package net.mehvahdjukaar.amendments.common.entity;

import net.mehvahdjukaar.amendments.configs.CommonConfigs;
import net.mehvahdjukaar.amendments.reg.ModRegistry;
import net.mehvahdjukaar.moonlight.api.entity.ImprovedProjectileEntity;
import net.mehvahdjukaar.moonlight.api.entity.ParticleTrailEmitter;
import net.minecraft.client.particle.DragonBreathParticle;
import net.minecraft.core.particles.ParticleTypes;
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
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import org.joml.Quaterniond;
import org.joml.Quaternionf;
import org.joml.Vector3d;
import org.joml.Vector3f;

public class MediumDragonFireball extends ImprovedProjectileEntity {

    private final ParticleTrailEmitter trailEmitter = ParticleTrailEmitter.builder()
            .spacing(0.7)
            .maxParticlesPerTick(5)
            .minSpeed(0.0)
            .build();

    public MediumDragonFireball(EntityType<? extends MediumDragonFireball> entityType, Level level) {
        super(entityType, level);
    }

    public MediumDragonFireball(Level level, LivingEntity shooter) {
        super(ModRegistry.MEDIUM_DRAGON_FIREBALL.get(), shooter, level);
    }

    private int particleCount = 0;

    @Override
    public void spawnTrailParticles() {
        super.spawnTrailParticles();
        trailEmitter.tick(this, (p, motion) -> {
            if (this.isInWater()) return;

            float coneAngle = Mth.DEG_TO_RAD *10;
            float spiralIncrement = 0.1f;
            float speed = 0.1f;

            // Normalize motion vector
            Vector3f dir = motion.toVector3f().normalize();

            // Compute a perpendicular vector to `dir`
            Vector3f up = new Vector3f(0, 1, 0);
            if (Math.abs(dir.dot(up)) > 0.99) {
                up.set(1, 0, 0); // fallback if dir is nearly parallel to Y
            }
            Vector3f tangent = dir.cross(up, new Vector3f()).normalize();

            // Create rotation around the motion vector to rotate the tangent
            float spiralAngle = particleCount * spiralIncrement; // tweak 0.3 to control spiral density
            Quaternionf spiralRotation = new Quaternionf().fromAxisAngleRad(dir, spiralAngle);
            tangent.rotate(spiralRotation);

            // Tilt the vector toward the dir to form the cone (mix between tangent and dir)
            Vector3f finalDir = new Vector3f(dir).mul(Mth.cos(coneAngle)).add(tangent.mul(Mth.sin(coneAngle)))
                    .normalize().mul(speed);

            // Spawn particle with the calculated direction
            level().addParticle(ParticleTypes.DRAGON_BREATH,
                    p.x,
                    p.y,
                    p.z,
                    random.nextGaussian() * 0.04,
                    random.nextGaussian() * 0.04,
                    random.nextGaussian() * 0.04);

            particleCount++;
        });
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

                //TODO: fix particles not breaking end crystals
                //TODO: particle effect here
               // this.level().levelEvent(2006, this.blockPosition(), this.isSilent() ? -1 : 1);
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
    protected Item getDefaultItem() {
        return ModRegistry.DRAGON_CHARGE.get();
    }


}
