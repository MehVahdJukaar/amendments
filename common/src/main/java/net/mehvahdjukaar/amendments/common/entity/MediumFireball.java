package net.mehvahdjukaar.amendments.common.entity;

import net.mehvahdjukaar.amendments.client.TumblingAnimation;
import net.mehvahdjukaar.amendments.common.ProjectileStats;
import net.mehvahdjukaar.amendments.configs.ClientConfigs;
import net.mehvahdjukaar.amendments.configs.CommonConfigs;
import net.mehvahdjukaar.amendments.reg.ModRegistry;
import net.mehvahdjukaar.moonlight.api.entity.ImprovedProjectileEntity;
import net.mehvahdjukaar.moonlight.api.entity.ParticleTrailEmitter;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.SnowballItem;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;

public class MediumFireball extends ImprovedProjectileEntity implements IVisualTransformationProvider {

    private final ParticleTrailEmitter trailEmitter = ProjectileStats.makeFireballTrialEmitter();
    private final TumblingAnimation tumblingAnimation = ProjectileStats.makeTumbler();
    private boolean isExtinguished = false;

    public MediumFireball(Level level, LivingEntity shooter) {
        super(ModRegistry.MEDIUM_FIREBALL.get(), shooter, level);
        if (!CommonConfigs.FIRE_CHARGE_GRAVITY.get()) {
            this.setNoGravity(true);
        }
    }

    public MediumFireball(Level level, double x, double y, double z) {
        super(ModRegistry.MEDIUM_FIREBALL.get(), x, y, z, level);
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
            if (this.isExtinguished) return;
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
        if (!this.isExtinguished && this.isInWater()) {
            this.isExtinguished = true;
            if (!level().isClientSide()) {
                this.clearFire();
                this.playEntityOnFireExtinguishedSound();
                if (this.getType() == EntityType.SMALL_FIREBALL) {
                    this.discard();
                }
            }
        }
        if (!this.isExtinguished) this.igniteForSeconds(1);
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
            if (!this.isExtinguished) {
                boolean bl = this.level().getGameRules().getBoolean(GameRules.RULE_MOBGRIEFING);
                var settings = new FireballExplosion.ExtraSettings();
                settings.hasKnockback = false;
                settings.soundVolume = ProjectileStats.PLAYER_FIREBALL.soundVolume();
                settings.onFireSeconds = ProjectileStats.PLAYER_FIREBALL.indirectHitFireSeconds();
                settings.maxDamage = ProjectileStats.PLAYER_FIREBALL.normalExplosionRadius() + 1;
                FireballExplosion.explodeServer(this.level(), this, null, null,
                        this.getX(), this.getY(), this.getZ(), (float) 1,
                        bl, Level.ExplosionInteraction.NONE, settings);
            }
            this.discard();
        }
    }

    @Override
    protected void onHitEntity(EntityHitResult result) {
        super.onHitEntity(result);
        if (level() instanceof ServerLevel serverLevel) {
            var entity = result.getEntity();
            Entity fireballOwner = this.getOwner();
            DamageSource source = fireballDamage(fireballOwner);
            if (this.isExtinguished) {
                entity.hurt(source, ProjectileStats.PLAYER_FIREBALL.damageOnHit());
            } else {
                //actually replace this with explosion
                int fireTick = entity.getRemainingFireTicks();
                entity.igniteForSeconds(ProjectileStats.PLAYER_FIREBALL.directHitFireSeconds()); //same as blaze charge
                if (!entity.hurt(source, ProjectileStats.PLAYER_FIREBALL.damageOnHit())) {
                    entity.setRemainingFireTicks(fireTick);
                } else {
                    EnchantmentHelper.doPostAttackEffects(serverLevel, entity, source);
                }
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
