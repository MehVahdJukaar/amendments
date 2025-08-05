package net.mehvahdjukaar.amendments.mixins;

import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import net.mehvahdjukaar.amendments.common.entity.FireballExplosion;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Set;

//TODO: change in 1.21
@Mixin(Explosion.class)
public class ExplosionMixin {

    @WrapOperation(method = "explode", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Entity;hurt(Lnet/minecraft/world/damagesource/DamageSource;F)Z"))
    public boolean amendments$HurtWithContext(Entity entity, DamageSource source, float amount, Operation<Boolean> original) {
        if (((Object) this) instanceof FireballExplosion fe) {
            return fe.hurtHitEntity(entity, source, amount);
        } else {
            return original.call(entity, source, amount);
        }
    }

    @WrapWithCondition(method = "explode", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Entity;setDeltaMovement(Lnet/minecraft/world/phys/Vec3;)V"))
    public boolean amendments$cancelKnockback(Entity instance, Vec3 deltaMovement) {
        return !(((Object) this) instanceof FireballExplosion fe) || fe.hasKnockback();
    }

    @Inject(method = "explode", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/level/ExplosionDamageCalculator;getBlockExplosionResistance(Lnet/minecraft/world/level/Explosion;Lnet/minecraft/world/level/BlockGetter;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/world/level/material/FluidState;)Ljava/util/Optional;"))
    public void amendments$addBlockSideEffects(CallbackInfo ci, @Local BlockPos pos, @Local BlockState state, @Local Set<BlockPos> set) {
        if (((Object) this) instanceof FireballExplosion fe) {
            fe.setBlockOnFire(pos, state);
        }
    }

    @WrapWithCondition(method = "finalizeExplosion", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/Level;playLocalSound(DDDLnet/minecraft/sounds/SoundEvent;Lnet/minecraft/sounds/SoundSource;FFZ)V"))
    public boolean amendments$changeSound(Level instance, double x, double y, double z, SoundEvent sound, SoundSource category, float volume, float pitch, boolean distanceDelay) {
        if (((Object) this) instanceof FireballExplosion fe) {
            return fe.playExplosionSound();
        }
        return true;
    }
}
