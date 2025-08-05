package net.mehvahdjukaar.amendments.mixins;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.mehvahdjukaar.amendments.common.ProjectileStats;
import net.mehvahdjukaar.amendments.common.entity.FireballExplosion;
import net.mehvahdjukaar.amendments.configs.CommonConfigs;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.projectile.LargeFireball;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(LargeFireball.class)
public abstract class LargeFireballMixin extends Entity {

    public LargeFireballMixin(EntityType<?> entityType, Level level) {
        super(entityType, level);
    }//TODO:

    @WrapOperation(method = "onHit", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/Level;explode(Lnet/minecraft/world/entity/Entity;DDDFZLnet/minecraft/world/level/Level$ExplosionInteraction;)Lnet/minecraft/world/level/Explosion;"))
    public Explosion amendments$fireballExplosion(Level instance, Entity source, double x, double y, double z, float radius, boolean fire, Level.ExplosionInteraction explosionInteraction, Operation<Explosion> original) {
        if (CommonConfigs.FIREBALL_EXPLOSION.get()) {
            var settings = new FireballExplosion.ExtraSettings();
            settings.onFireTicks = ProjectileStats.GHAST_FIREBALL.indirectHitFireTicks();
            settings.soundVolume = ProjectileStats.GHAST_FIREBALL.soundVolume();
            return FireballExplosion.explodeServer(instance, source, null, null,
                    x, y, z, radius, fire, explosionInteraction, settings);
        }
        return original.call(instance, source, x, y, z, radius, fire, explosionInteraction);
    }
}
