package net.mehvahdjukaar.amendments.mixins;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.block.entity.BellBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BrewingStandBlockEntity;
import net.minecraft.world.level.block.entity.JukeboxBlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

//avoids overrides overwrites so no mixin conflicts
@Mixin(BlockEntity.class)
public abstract class BlockEntityUpdateMixin {

    @Shadow
    public abstract CompoundTag saveWithoutMetadata(HolderLookup.Provider registries);

    @ModifyReturnValue(method = "getUpdatePacket", at = @At("RETURN"))
    private Packet<ClientGamePacketListener> amendments$syncedTilesPacket(Packet<ClientGamePacketListener> original) {
        if (original == null && amendments$isSyncedTile()) {
            return ClientboundBlockEntityDataPacket.create((BlockEntity) (Object) this);
        }
        return original;
    }

    @ModifyReturnValue(method = "getUpdateTag", at = @At("RETURN"))
    private CompoundTag amendments$syncedTilesTag(CompoundTag original, HolderLookup.Provider registries) {
        if (amendments$isSyncedTile()) return this.saveWithoutMetadata(registries);
        return original;
    }


    @Unique
    private boolean amendments$isSyncedTile() {
        Object self = this;
        return self instanceof BrewingStandBlockEntity || self instanceof BellBlockEntity || self instanceof JukeboxBlockEntity;
    }
}
