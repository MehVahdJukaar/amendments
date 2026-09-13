package net.mehvahdjukaar.amendments.mixins;

import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import net.mehvahdjukaar.amendments.common.entity.FireballExplosion;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.ServerExplosion;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

@Mixin(ServerExplosion.class)
public class ExplosionMixin {

    @WrapOperation(method = "hurtEntities(Ljava/util/List;)V", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/entity/Entity;hurtServer(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/damagesource/DamageSource;F)Z"))
    public boolean amendments$HurtWithContext(Entity entity, ServerLevel level, DamageSource source, float amount,
                                              Operation<Boolean> original) {
        if (((Object) this) instanceof FireballExplosion fe) {
            return fe.hurtHitEntity(entity, source, amount);
        }
        return original.call(entity, level, source, amount);
    }

    @WrapWithCondition(method = "hurtEntities(Ljava/util/List;)V", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/entity/Entity;push(Lnet/minecraft/world/phys/Vec3;)V"))
    public boolean amendments$cancelKnockback(Entity instance, Vec3 knockback) {
        return !(((Object) this) instanceof FireballExplosion fe) || fe.hasKnockback();
    }

    //every position the blast walks through, not just the ones that end up broken
    @Inject(method = "calculateExplodedPositions", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/level/ExplosionDamageCalculator;getBlockExplosionResistance(Lnet/minecraft/world/level/Explosion;Lnet/minecraft/world/level/BlockGetter;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/world/level/material/FluidState;)Ljava/util/Optional;"))
    public void amendments$addBlockSideEffects(CallbackInfoReturnable<List<BlockPos>> cir, @Local BlockPos pos) {
        if (((Object) this) instanceof FireballExplosion fe) {
            fe.addVisitedBlock(pos);
        }
    }
}
