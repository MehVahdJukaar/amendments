package net.mehvahdjukaar.amendments.common;

import net.mehvahdjukaar.amendments.client.TumblingAnimation;
import net.mehvahdjukaar.moonlight.api.entity.ParticleTrailEmitter;

public class ProjectileStats {

    public static final float THROWN_SPEED = 1.1f;

    //TODO:do better
    public static final Fire BLAZE_FIREBALL = new Fire(
            0.75f,
            5,
            5,
            0,
            0,
            0,
            0);

    public static final Fire PLAYER_FIREBALL = new Fire(
            0.75f,
            5,
            5,
            1,
            4,
            0,
            1);

    public static final Fire GHAST_FIREBALL = new Fire(
            2.375f,
            6,
            5,
            1,
            4,
            1,
            4);


    public static final Dragon DRAGON_FIREBALL = new Dragon(
            2.375f);

    public static final Dragon DRAGON_CHARGE = new Dragon(
            0.75f);


    public record Fire(float modelSize,
                       float damageOnHit,
                       int directHitFireSeconds,
                    float fireballExpRadius,
                       int indirectHitFireSeconds,
                       float normalExplosionRadius,
                       float soundVolume) {

    }

    public record Dragon(float modelSize) {
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


    public static ParticleTrailEmitter makeDragonTrialEmitter(boolean isLarge) {
        return ParticleTrailEmitter.builder()
                .spacing(0.7)
                .maxParticlesPerTick(5)
                .minSpeed(0.0)
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
