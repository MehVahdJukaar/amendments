package net.mehvahdjukaar.amendments.common;

import net.mehvahdjukaar.amendments.common.entity.ParticleTrailEmitter2;
import net.mehvahdjukaar.amendments.common.entity.TumblingAnimation;
import net.mehvahdjukaar.moonlight.api.entity.ParticleTrailEmitter;

public class ProjectileStats {

    public static final Fire BLAZE_FIREBALL = new Fire(
            0.75f,
            5,
            5,
            0,
            0,
            0);

    public static final Fire GHAST_FIREBALL = new Fire(
            2.375f,
            6,
            5,
            1,
            4,
            1);




    public record Fire(float modelSize,
                       float damageOnHit,
                       float directHitFireTicks,
                       float fireballExpRadius,
                       float indirectHitFireTicks,
                       float normalExplosionRadius) {

    }

    public static TumblingAnimation makeTumbler() {
        return new TumblingAnimation(
                4f, 7f, 0.5f);
    }

    public static TumblingAnimation makeFasterTumbler() {
        return new TumblingAnimation(
                6f, 9f, 0.5f);
    }


    public static ParticleTrailEmitter makeFireballTrialEmitter() {
        return ParticleTrailEmitter.builder()
                .spacing(0.5)
                .maxParticlesPerTick(20)
                .minSpeed(0.01)
                .build();
    }


    public static ParticleTrailEmitter2 makeFireballTrialEmitter2() {
        return ParticleTrailEmitter2.builder()
                .spacing(0.5)
                .maxParticlesPerTick(20)
                .minSpeed(0.01)
                .build();
    }



    public static ParticleTrailEmitter makeSnowballTrialEmitter() {
        return ParticleTrailEmitter.builder()
                .spacing(0.35)
                .maxParticlesPerTick(20)
                .minSpeed(0.01)
                .build();
    }



}
