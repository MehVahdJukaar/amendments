package net.mehvahdjukaar.amendments.mixins;

import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
import net.mehvahdjukaar.amendments.configs.CommonConfigs;
import net.mehvahdjukaar.amendments.reg.ModTags;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.entity.RelativeMovement;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.ai.goal.FollowOwnerGoal;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Set;

@Mixin(ServerGamePacketListenerImpl.class)
public abstract class ServerGamePacketListenerMixin {

    @Shadow
    public ServerPlayer player;

    @Inject(method = "teleport(DDDFFLjava/util/Set;)V", at = @At("HEAD"))
    private void amendments$rememberPosBeforeTeleport(double x, double y, double z, float yaw, float pitch,
                                                      Set<RelativeMovement> relativeSet, CallbackInfo ci,
                                                      @Share("oldPos") LocalRef<Vec3> oldPos) {
        oldPos.set(this.player.position());
    }

    @Inject(method = "teleport(DDDFFLjava/util/Set;)V", at = @At("TAIL"))
    private void amendments$bringPetsAlong(double x, double y, double z, float yaw, float pitch,
                                           Set<RelativeMovement> relativeSet, CallbackInfo ci,
                                           @Share("oldPos") LocalRef<Vec3> oldPos) {
        if (!CommonConfigs.PETS_FOLLOW_TELEPORTS.get()) return;
        var followingPets = player.serverLevel().getEntitiesOfClass(TamableAnimal.class, AABB.ofSize(oldPos.get(), 24, 12, 24),
                pet -> pet.isOwnedBy(player) && !pet.unableToMoveToOwner() && amendments$canFollowsOwner(pet));
        for (TamableAnimal pet : followingPets) {
            if (pet.shouldTryTeleportToOwner()) {
                pet.tryToTeleportToOwner();
            }
        }
    }

    //for mods that have pets that cant follow meaning we probably sholdnt teleport them
    @Unique
    private static boolean amendments$canFollowsOwner(TamableAnimal pet) {
        if (pet.getType().is(ModTags.PETS_FOLLOW_TELEPORTS_WHITELIST)) return true;
        for (var wrapped : ((MobAccessor) pet).amendments$getGoalSelector().getAvailableGoals()) {
            if (wrapped.getGoal() instanceof FollowOwnerGoal) return true;
        }
        return false;
    }
}
