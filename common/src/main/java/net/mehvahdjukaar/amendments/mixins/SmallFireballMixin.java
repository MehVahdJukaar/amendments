package net.mehvahdjukaar.amendments.mixins;

import net.mehvahdjukaar.amendments.configs.ClientConfigs;
import net.mehvahdjukaar.amendments.configs.CommonConfigs;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.projectile.SmallFireball;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(SmallFireball.class)
public abstract class SmallFireballMixin extends Entity {

    public SmallFireballMixin(EntityType<?> entityType, Level level) {
        super(entityType, level);
    }

    @Inject(method = "hurt", at = @At("HEAD"), cancellable = true)
    public void amendments$hurt(DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir) {
        cir.setReturnValue(true);
    }

    @Inject(method = "isPickable", at = @At("HEAD"), cancellable = true)
    public void amendments$isPickable(CallbackInfoReturnable<Boolean> cir) {
        if(this.getType() == EntityType.SMALL_FIREBALL &&
                CommonConfigs.DEFLECT_FIRE_CHARGES.get()){
            cir.setReturnValue(false);
        }
    }
}
