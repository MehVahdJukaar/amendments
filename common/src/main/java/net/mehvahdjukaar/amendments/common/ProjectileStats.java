package net.mehvahdjukaar.amendments.common;

import net.mehvahdjukaar.amendments.client.TumblingAnimation;
import net.mehvahdjukaar.moonlight.api.entity.ParticleTrailEmitter;

public class ProjectileStats {

    public static final float THROWN_SPEED = 1.25f;

    //TODO:do better
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

    public static final Fire PLAYER_FIREBALL = new Fire(
            2.375f,
            5,
            5,
            1,
            4,
            0);




    public record Fire(float modelSize,
                       float damageOnHit,
                       int directHitFireTicks,
                       float fireballExpRadius,
                       int indirectHitFireTicks,
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



    public static ParticleTrailEmitter makeSnowballTrialEmitter() {
        return ParticleTrailEmitter.builder()
                .spacing(0.35)
                .maxParticlesPerTick(20)
                .minSpeed(0.01)
                .build();
    }



}
