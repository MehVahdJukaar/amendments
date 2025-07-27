package net.mehvahdjukaar.amendments.common.entity;

import net.mehvahdjukaar.amendments.reg.ModRegistry;
import net.mehvahdjukaar.moonlight.api.entity.ImprovedProjectileEntity;
import net.mehvahdjukaar.moonlight.api.entity.ParticleTrailEmitter;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.HitResult;

public class MediumFireball extends ImprovedProjectileEntity {

    private final ParticleTrailEmitter trailEmitter = makeTrialEmitter();

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
    protected Item getDefaultItem() {
        return Items.FIRE_CHARGE.asItem();
    }
}
