package net.mehvahdjukaar.amendments.mixins;

import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
import net.mehvahdjukaar.amendments.configs.CommonConfigs;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.entity.PositionMoveRotation;
import net.minecraft.world.entity.Relative;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Set;

@Mixin(ServerGamePacketListenerImpl.class)
public abstract class ServerGamePacketListenerMixin {

    @Shadow
    public ServerPlayer player;

    @Inject(method = "teleport(Lnet/minecraft/world/entity/PositionMoveRotation;Ljava/util/Set;)V", at = @At("HEAD"))
    private void amendments$rememberPosBeforeTeleport(PositionMoveRotation destination, Set<Relative> relatives, CallbackInfo ci,
                                                      @Share("oldPos") LocalRef<Vec3> oldPos) {
        oldPos.set(this.player.position());
    }

    @Inject(method = "teleport(Lnet/minecraft/world/entity/PositionMoveRotation;Ljava/util/Set;)V", at = @At("TAIL"))
    private void amendments$bringPetsAlong(PositionMoveRotation destination, Set<Relative> relatives, CallbackInfo ci,
                                           @Share("oldPos") LocalRef<Vec3> oldPos) {
        if (!CommonConfigs.PETS_FOLLOW_TELEPORTS.get()) return;
        var followingPets = player.level().getEntitiesOfClass(TamableAnimal.class, AABB.ofSize(oldPos.get(), 24, 12, 24),
                pet -> pet.isOwnedBy(player) && !pet.unableToMoveToOwner());
        for (TamableAnimal pet : followingPets) {
            if (pet.shouldTryTeleportToOwner()) {
                pet.tryToTeleportToOwner();
            }
        }
    }
}
