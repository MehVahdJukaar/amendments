package net.mehvahdjukaar.amendments.mixins;

import net.mehvahdjukaar.amendments.common.entity.MediumFireball;
import net.mehvahdjukaar.amendments.configs.ClientConfigs;
import net.mehvahdjukaar.amendments.reg.ModRegistry;
import net.mehvahdjukaar.moonlight.api.entity.ParticleTrailEmitter;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.AbstractHurtingProjectile;
import net.minecraft.world.entity.projectile.Fireball;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

// just here because it annoyed me that you can pick these
@Mixin(Fireball.class)
public abstract class FireballMixin extends AbstractHurtingProjectile {

    @Unique
    private final ParticleTrailEmitter amendments$trailEmitter = MediumFireball.makeTrialEmitter();

    protected FireballMixin(EntityType<? extends AbstractHurtingProjectile> entityType, Level level) {
        super(entityType, level);
    }

    public FireballMixin(EntityType<? extends AbstractHurtingProjectile> entityType, double x, double y, double z, double offsetX, double offsetY, double offsetZ, Level level) {
        super(entityType, x, y, z, offsetX, offsetY, offsetZ, level);
    }

    public FireballMixin(EntityType<? extends AbstractHurtingProjectile> entityType, LivingEntity shooter, double offsetX, double offsetY, double offsetZ, Level level) {
        super(entityType, shooter, offsetX, offsetY, offsetZ, level);
    }


    @Shadow
    public abstract ItemStack getItem();

    @Override
    public @Nullable ItemStack getPickResult() {
        return getItem().copyWithCount(1);
    }

    @Override
    public boolean displayFireAnimation() {
        if (level().isClientSide &&
                (this.getType() == EntityType.FIREBALL || this.getType() == EntityType.SMALL_FIREBALL) &&
                ClientConfigs.FIREBALL_3D.get()) {
            return false;
        }
        return super.displayFireAnimation();
    }

    @Override
    public void tick() {
        super.tick();
        amendments$trailEmitter.tick(this,
                (p, v) -> {
                    if (this.isInWater()) return;
                    level().addParticle(ModRegistry.FIREBALL_TRAIL_PARTICLE.get(), p.x, p.y, p.z, 0, 0, 0);
                }
        );
    }
}
