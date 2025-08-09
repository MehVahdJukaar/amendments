package net.mehvahdjukaar.amendments.mixins;

import net.mehvahdjukaar.amendments.client.TumblingAnimation;
import net.mehvahdjukaar.amendments.common.ProjectileStats;
import net.mehvahdjukaar.amendments.common.entity.IVisualTransformationProvider;
import net.mehvahdjukaar.amendments.configs.ClientConfigs;
import net.mehvahdjukaar.amendments.reg.ModRegistry;
import net.mehvahdjukaar.moonlight.api.entity.ParticleTrailEmitter;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.projectile.AbstractHurtingProjectile;
import net.minecraft.world.entity.projectile.Fireball;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

// just here because it annoyed me that you can pick these
@Mixin(Fireball.class)
public abstract class FireballMixin extends AbstractHurtingProjectile implements IVisualTransformationProvider {

    @Unique
    private boolean amendments$isExtinguished = false;

    @Unique
    private final ParticleTrailEmitter amendments$trailEmitter = ProjectileStats.makeFireballTrialEmitter();
    @Unique
    private final TumblingAnimation amendments$tumblingAnimation = ProjectileStats.makeTumbler();

    protected FireballMixin(EntityType<? extends AbstractHurtingProjectile> entityType, Level level) {
        super(entityType, level);
    }

    @Override
    public Matrix4f amendments$getVisualTransformation(float partialTicks) {
        return new Matrix4f().rotate(this.amendments$tumblingAnimation.getRotation(partialTicks));
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
    protected boolean shouldBurn() {
        if (amendments$isExtinguished && this.getType() == EntityType.FIREBALL) {
            return false;
        }
        return super.shouldBurn();
    }

    @Override
    public void tick() {
        super.tick();
        if (level().isClientSide) {
            if (ClientConfigs.GHAST_FIREBALL_TRAIL.get()) {
                amendments$trailEmitter.tick(this,
                        (p, v) -> {
                            if (this.isInWater()) return;
                            level().addParticle(ModRegistry.FIREBALL_TRAIL_PARTICLE.get(), p.x, p.y, p.z,
                                    this.getBbWidth() * 0.8, 0, 0);
                        }
                );
            }
            if (ClientConfigs.CHARGES_TUMBLE.get()) amendments$tumblingAnimation.tick(random);
        }
        if (!this.amendments$isExtinguished && this.isInWater()) {
            this.amendments$isExtinguished = true;
            if (!level().isClientSide()) {
                level().broadcastEntityEvent(this, (byte) 67);
                level().playSound(null, this.getX(), this.getY(), this.getZ(), SoundEvents.GENERIC_EXTINGUISH_FIRE, SoundSource.BLOCKS, 0.5F, 1.5F);
                if (this.getType() == EntityType.SMALL_FIREBALL) {
                    this.discard();
                }
            }
        }
    }

}
