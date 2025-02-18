package net.mehvahdjukaar.amendments.mixins;

import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.projectile.AbstractHurtingProjectile;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(AbstractHurtingProjectile.class)
public abstract class AbstractHurtingProjectileMixin extends Entity {

    public AbstractHurtingProjectileMixin(EntityType<?> entityType, Level level) {
        super(entityType, level);
    }

    // fixing this shit because it bugs me. its offset by 0.5 for no reason at all
    @ModifyArg(method = "tick",
            index = 2,
            at = @At(value = "INVOKE",
                    ordinal = 1,
                    target = "Lnet/minecraft/world/level/Level;addParticle(Lnet/minecraft/core/particles/ParticleOptions;DDDDDD)V"))
    public double amendments$fixDumbMcParticleY(ParticleOptions particleData, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed,
                                             @Local(ordinal = 1) double yLocal) {

        return yLocal + this.getBbHeight() / 2.0;
    }

    @WrapOperation(method = "part")
}
