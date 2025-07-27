package net.mehvahdjukaar.amendments.common.entity;

import net.mehvahdjukaar.amendments.reg.ModRegistry;
import net.mehvahdjukaar.moonlight.api.entity.ImprovedProjectileEntity;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

public class MediumFireball extends ImprovedProjectileEntity {

    private final ParticleTrailEmitter trailEmitter = ParticleTrailEmitter.builder()
            .spacing(0.5)
            .maxParticlesPerTick(20)
            .minSpeed(0)
            .particle(this::spawnFireTrailParticle)
            .build();

    public MediumFireball(Level level, LivingEntity shooter) {
        super(ModRegistry.MEDIUM_FIREBALL.get(), shooter, level);
    }

    public MediumFireball(double x, double y, double z, Level level) {
        super(ModRegistry.MEDIUM_FIREBALL.get(), x, y, z, level);
    }

    public MediumFireball(EntityType<MediumFireball> mediumFireballEntityType, Level level) {
        super(mediumFireballEntityType, level);
    }

    private void spawnFireTrailParticle(Level level, Vec3 pos, Vec3 vel) {
        if (this.isInWater()) return;
        level.addParticle(ParticleTypes.END_ROD,
                pos.x, pos.y, pos.z,
                0,0,0);
    }

    @Override
    public void spawnTrailParticles() {
        super.spawnTrailParticles();
        trailEmitter.tick(this);
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
