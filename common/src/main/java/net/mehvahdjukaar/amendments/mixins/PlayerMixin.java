package net.mehvahdjukaar.amendments.mixins;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import net.mehvahdjukaar.amendments.configs.CommonConfigs;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(Player.class)
public abstract class PlayerMixin {

    //blaze fireballs arent in the redirectable tag
    @ModifyExpressionValue(method = "deflectProjectile", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/entity/Entity;is(Lnet/minecraft/tags/TagKey;)Z"))
    private boolean amendments$deflectFireCharges(boolean original, Entity entity) {
        return original || (entity.getType() == EntityType.SMALL_FIREBALL && CommonConfigs.DEFLECT_FIRE_CHARGES.get());
    }
}
