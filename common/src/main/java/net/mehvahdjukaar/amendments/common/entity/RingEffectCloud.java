package net.mehvahdjukaar.amendments.common.entity;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import net.mehvahdjukaar.amendments.reg.ModRegistry;
import net.mehvahdjukaar.supplementaries.reg.ModEntities;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.AreaEffectCloud;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;

import java.util.List;
import java.util.Map;

public class RingEffectCloud extends AreaEffectCloud {
    private final Map<Entity, Integer> victims = Maps.<Entity, Integer>newHashMap();
    private float ringWidth = 1.0F; // example default ring width


    public RingEffectCloud(EntityType<? extends AreaEffectCloud> entityType, Level level) {
        super(entityType, level);
    }

    public RingEffectCloud(Level level, double x, double y, double z) {
        this(ModRegistry.RING_EFFECT_CLOUD.get(), level);
        this.setPos(x, y, z);
    }

    public void setRingWidth(float ringWidth) {
        this.ringWidth = ringWidth;
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag compound) {
        super.addAdditionalSaveData(compound);
        compound.putFloat("RingWidth", this.ringWidth);
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag compound) {
        super.readAdditionalSaveData(compound);
        if (compound.contains("RingWidth", 5)) {
            this.ringWidth = compound.getFloat("RingWidth");
        }
    }

    @Override
    public void tick() {
        //not ideal but we need to not call super to override this logic. Normally this just calls baseTick anyways. If a mod relies on super being called itsa bad mod. Use eevnts.
        this.baseTick();

        boolean isWaiting = this.isWaiting();
        float radius = this.getRadius();
        if (this.level().isClientSide) {
            if (isWaiting && this.random.nextBoolean()) {
                return;
            }

            ParticleOptions particleOptions = this.getParticle();
            int particleAmount;
            if (isWaiting) {
                particleAmount = 2;
            } else {
                particleAmount = Mth.ceil((float) Math.PI * radius * radius) ;
            }

            float centerParticleRadius = radius - this.ringWidth / 2.0F;
            float particleRingWidth = this.ringWidth *0f;
            float outerParticleRadius = centerParticleRadius + particleRingWidth / 2.0F;
            float innerParticleRadius = centerParticleRadius - particleRingWidth / 2.0F;
            float actualInner = Math.max(0.0F, innerParticleRadius);

            for (int j = 0; j < particleAmount; j++) {
                float k = Mth.sqrt(this.random.nextFloat() * (outerParticleRadius * outerParticleRadius - actualInner * actualInner) + actualInner * actualInner);
                float angle = this.random.nextFloat() * (float) (Math.PI * 2);
                double d = this.getX() + (double) (Mth.cos(angle) * k);
                double e = this.getY();
                double l = this.getZ() + (double) (Mth.sin(angle) * k);
                double n;
                double o;
                double p;
                if (particleOptions.getType() == ParticleTypes.ENTITY_EFFECT) {
                    int m = isWaiting && this.random.nextBoolean() ? 16777215 : this.getColor();
                    n = ((float) (m >> 16 & 0xFF) / 255.0F);
                    o = ((float) (m >> 8 & 0xFF) / 255.0F);
                    p = ((float) (m & 0xFF) / 255.0F);
                } else if (isWaiting) {
                    n = 0.0;
                    o = 0.0;
                    p = 0.0;
                } else {
                    double speed = 0.15;
                    n = (0.5 - this.random.nextDouble()) * speed;
                    o = 0.01F;
                    p = (0.5 - this.random.nextDouble()) * speed;
                }

                this.level().addAlwaysVisibleParticle(particleOptions, d, e, l, n, o, p);
            }
        } else {
            if (this.tickCount >= this.getWaitTime() + this.getDuration()) {
                this.discard();
                return;
            }

            boolean shouldWait = this.tickCount < this.getWaitTime();
            if (isWaiting != shouldWait) {
                this.setWaiting(shouldWait);
            }

            if (shouldWait) {
                return;
            }

            if (this.getRadiusPerTick() != 0.0F) {
                radius += this.getRadiusPerTick();

                this.setRadius(radius);
            }

            if (this.tickCount % 5 == 0) {
                this.victims.entrySet().removeIf(entry -> this.tickCount >= entry.getValue());
                List<MobEffectInstance> list = Lists.newArrayList();

                for (MobEffectInstance mobEffectInstance : this.getPotion().getEffects()) {
                    list.add(
                            new MobEffectInstance(
                                    mobEffectInstance.getEffect(),
                                    mobEffectInstance.mapDuration(i -> i / 4),
                                    mobEffectInstance.getAmplifier(),
                                    mobEffectInstance.isAmbient(),
                                    mobEffectInstance.isVisible()
                            )
                    );
                }

                list.addAll(this.effects);
                if (list.isEmpty()) {
                    this.victims.clear();
                } else {
                    List<LivingEntity> livingEntitiesInBB = this.level().getEntitiesOfClass(LivingEntity.class, this.getBoundingBox());
                    if (!livingEntitiesInBB.isEmpty()) {
                        for (LivingEntity livingEntity : livingEntitiesInBB) {
                            if (!this.victims.containsKey(livingEntity) && livingEntity.isAffectedByPotions()) {
                                double q = livingEntity.getX() - this.getX();
                                double r = livingEntity.getZ() - this.getZ();
                                double s = q * q + r * r;
                                float inner = Math.max(0.0F, radius - ringWidth);
                                if (s >= (double) (inner * inner) && s <= (double) (radius * radius)) {
                                    this.victims.put(livingEntity, this.tickCount + this.reapplicationDelay);

                                    for (MobEffectInstance mobEffectInstance2 : list) {
                                        if (mobEffectInstance2.getEffect().isInstantenous()) {
                                            mobEffectInstance2.getEffect().applyInstantenousEffect(this, this.getOwner(), livingEntity, mobEffectInstance2.getAmplifier(), 0.5);
                                        } else {
                                            livingEntity.addEffect(new MobEffectInstance(mobEffectInstance2), this);
                                        }
                                    }

                                    if (this.getRadiusOnUse() != 0.0F) {
                                        radius += this.getRadiusOnUse();

                                        this.setRadius(radius);
                                    }

                                    if (this.getDurationOnUse() != 0) {
                                        this.setDuration(this.getDuration() + this.getDurationOnUse());
                                        if (this.getDuration() <= 0) {
                                            this.discard();
                                            return;
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }


}
