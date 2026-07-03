package net.mehvahdjukaar.amendments.mixins;

import net.mehvahdjukaar.amendments.configs.CommonConfigs;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin {

    @Inject(method = "hurt", at = @At("HEAD"), cancellable = true)
    private void amendments$noPassengerSuffocation(DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir) {
        if (CommonConfigs.PASSENGER_NO_SUFFOCATION.get()
                && source.is(DamageTypes.IN_WALL)
                && ((LivingEntity) (Object) this).isPassenger()) {
            cir.setReturnValue(false);
        }
    }
}
