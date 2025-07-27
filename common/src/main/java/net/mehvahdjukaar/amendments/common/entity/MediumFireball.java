package net.mehvahdjukaar.amendments.common.entity;

import net.mehvahdjukaar.amendments.common.FireballStats;
import net.mehvahdjukaar.amendments.reg.ModRegistry;
import net.mehvahdjukaar.moonlight.api.entity.ImprovedProjectileEntity;
import net.mehvahdjukaar.moonlight.api.entity.ParticleTrailEmitter;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import org.jetbrains.annotations.Nullable;
import org.joml.Quaternionf;

public class MediumFireball extends ImprovedProjectileEntity implements IVisualRotationProvider {

    private final ParticleTrailEmitter trailEmitter = makeTrialEmitter();
    public final TumblingAnimation tumblingAnimation = FireballStats.makeTumbler();

    public static ParticleTrailEmitter makeTrialEmitter() {
        return ParticleTrailEmitter.builder()
                .spacing(0.5)
                .maxParticlesPerTick(20)
                .minSpeed(0)
                .build();
    }

    public MediumFireball(Level level, LivingEntity shooter) {
        super(ModRegistry.MEDIUM_FIREBALL.get(), shooter, level);

    }

    public MediumFireball(double x, double y, double z, Level level) {
        super(ModRegistry.MEDIUM_FIREBALL.get(), x, y, z, level);

    }

    public MediumFireball(EntityType<MediumFireball> mediumFireballEntityType, Level level) {
        super(mediumFireballEntityType, level);
    }

    @Override
    public void spawnTrailParticles() {
        super.spawnTrailParticles();
        trailEmitter.tick(this, (p, v) -> {
            if (this.isInWater()) return;
            level().addParticle(ModRegistry.FIREBALL_TRAIL_PARTICLE.get(), p.x, p.y, p.z, 0, 0, 0);
        });
        this.tumblingAnimation.tick(random);
    }

    @Override
    public Quaternionf amendments$getVisualRotation(float partialTicks) {
        return this.tumblingAnimation.getRotation(partialTicks);

    }

    @Override
    protected void onHit(HitResult result) {
        super.onHit(result);
        //just on client?
        level().addAlwaysVisibleParticle(ModRegistry.FIREBALL_EMITTER_PARTICLE.get(),
                this.getX(), this.getY(), this.getZ(),
                0, 0, 0);

        //create fire only explosion
    }

    @Override
    protected void onHitEntity(EntityHitResult result) {
        super.onHitEntity(result);
        if (!this.level().isClientSide) {
            //actually replace this with explosion
            var entity = result.getEntity();
            Entity fireballOwner = this.getOwner();
            int fireTick = entity.getRemainingFireTicks();
            entity.setSecondsOnFire(5); //same as blaze charge
            //TODO: figure out damage types and fire duration for all 3 fireballs + explosion
            if (!entity.hurt(fireballDamage(fireballOwner), 1.0F)) {
                entity.setRemainingFireTicks(fireTick);
            } else if (fireballOwner instanceof LivingEntity le) {
                this.doEnchantDamageEffects(le, entity);
            }

        }
    }

    private DamageSource fireballDamage(@Nullable Entity thrower) {
        var sources = this.damageSources();
        return thrower == null ? sources.source(DamageTypes.UNATTRIBUTED_FIREBALL, this)
                : sources.source(DamageTypes.FIREBALL, this, thrower);
    }

    @Override
    protected Item getDefaultItem() {
        return Items.FIRE_CHARGE.asItem();
    }
}
