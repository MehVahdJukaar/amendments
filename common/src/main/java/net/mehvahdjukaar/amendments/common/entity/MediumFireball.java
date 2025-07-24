package net.mehvahdjukaar.amendments.common.entity;

import net.mehvahdjukaar.amendments.reg.ModRegistry;
import net.mehvahdjukaar.moonlight.api.entity.ImprovedProjectileEntity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.HitResult;

public class MediumFireball extends ImprovedProjectileEntity {

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
        if (!isInWater()) {
            level().addParticle(ModRegistry.FIREBALL_TRAIL_PARTICLE.get(),
                    this.getX(), this.getY(), this.getZ(),
                    0, 0, 0);
        }
    }

    @Override
    protected void onHit(HitResult result) {
        super.onHit(result);
        level().addParticle(ModRegistry.FIREBALL_EMITTER_PARTICLE.get(),
                this.getX(), this.getY(), this.getZ(),
                0, 0, 0);
        //create fire only explosion
    }

    @Override
    protected Item getDefaultItem() {
        return Items.FIRE_CHARGE.asItem();
    }
}
