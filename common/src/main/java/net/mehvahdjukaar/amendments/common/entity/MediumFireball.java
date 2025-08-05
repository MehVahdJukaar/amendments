package net.mehvahdjukaar.amendments.common.entity;

import net.mehvahdjukaar.amendments.client.TumblingAnimation;
import net.mehvahdjukaar.amendments.common.ProjectileStats;
import net.mehvahdjukaar.amendments.configs.ClientConfigs;
import net.mehvahdjukaar.amendments.configs.CommonConfigs;
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
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;

public class MediumFireball extends ImprovedProjectileEntity implements IVisualTransformationProvider {

    private final ParticleTrailEmitter trailEmitter = ProjectileStats.makeFireballTrialEmitter();
    private final TumblingAnimation tumblingAnimation = ProjectileStats.makeTumbler();

    public MediumFireball(Level level, LivingEntity shooter) {
        super(ModRegistry.MEDIUM_FIREBALL.get(), shooter, level);
        if (!CommonConfigs.FIRE_CHARGE_GRAVITY.get()) {
            this.setNoGravity(true);
        }
    }

    public MediumFireball(EntityType<MediumFireball> mediumFireballEntityType, Level level) {
        super(mediumFireballEntityType, level);
        if (!CommonConfigs.FIRE_CHARGE_GRAVITY.get()) {
            this.setNoGravity(true);
        }
    }

    @Override
    public void spawnTrailParticles() {
        super.spawnTrailParticles();
        if (!level().isClientSide) return;
        trailEmitter.tick(this, (p, v) -> {
            if (this.isInWater()) return;
            level().addParticle(ModRegistry.FIREBALL_TRAIL_PARTICLE.get(), p.x, p.y, p.z,
                    this.getBbWidth(), 0, 0);
        });

        if (ClientConfigs.CHARGES_TUMBLE.get()) this.tumblingAnimation.tick(random);
    }

    @Override
    public Matrix4f amendments$getVisualTransformation(float partialTicks) {
        return new Matrix4f().rotate(this.tumblingAnimation.getRotation(partialTicks));
    }

    @Override
    public void tick() {
        super.tick();
        this.setSecondsOnFire(1);
    }

    @Override
    public boolean displayFireAnimation() {
        if (level().isClientSide && ClientConfigs.FIREBALL_3D.get()) {
            return false;
        }
        return super.displayFireAnimation();
    }

    @Override
    protected void onHit(HitResult result) {
        super.onHit(result);
        if (!this.level().isClientSide) {
            boolean bl = this.level().getGameRules().getBoolean(GameRules.RULE_MOBGRIEFING);
            var settings = new FireballExplosion.ExtraSettings();
            settings.hasKnockback = false;
            settings.soundVolume = ProjectileStats.PLAYER_FIREBALL.soundVolume();
            settings.onFireTicks = ProjectileStats.PLAYER_FIREBALL.indirectHitFireTicks();
            settings.maxDamage = ProjectileStats.PLAYER_FIREBALL.normalExplosionRadius() + 1;
            FireballExplosion.explodeServer(this.level(), this, null, null,
                    this.getX(), this.getY(), this.getZ(), (float) 1,
                    bl, Level.ExplosionInteraction.NONE, settings);
            this.discard();
        }
    }

    @Override
    protected void onHitEntity(EntityHitResult result) {
        super.onHitEntity(result);
        if (!this.level().isClientSide) {
            //actually replace this with explosion
            var entity = result.getEntity();
            Entity fireballOwner = this.getOwner();
            int fireTick = entity.getRemainingFireTicks();
            entity.setSecondsOnFire(ProjectileStats.PLAYER_FIREBALL.directHitFireTicks()); //same as blaze charge
            if (!entity.hurt(fireballDamage(fireballOwner), ProjectileStats.PLAYER_FIREBALL.damageOnHit())) {
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
