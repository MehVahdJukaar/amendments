package net.mehvahdjukaar.amendments.common;

import net.mehvahdjukaar.amendments.common.entity.TumblingAnimation;

public class FireballStats {

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
}
