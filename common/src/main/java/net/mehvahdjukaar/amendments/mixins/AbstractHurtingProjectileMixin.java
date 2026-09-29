package net.mehvahdjukaar.amendments.mixins;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.projectile.hurtingprojectile.AbstractHurtingProjectile;
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
    @ModifyArg(method = "createParticleTrail",
            index = 2,
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/world/level/Level;addParticle(Lnet/minecraft/core/particles/ParticleOptions;DDDDDD)V"))
    public double amendments$fixDumbMcParticleY(double y) {
        return this.getY() + this.getBbHeight() / 2.0;
    }

}
