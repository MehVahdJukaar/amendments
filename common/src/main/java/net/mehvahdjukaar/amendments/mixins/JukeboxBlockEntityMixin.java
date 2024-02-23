package net.mehvahdjukaar.amendments.mixins;

import net.mehvahdjukaar.amendments.common.IBetterJukebox;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.JukeboxBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(JukeboxBlockEntity.class)
public abstract class JukeboxBlockEntityMixin extends BlockEntity implements IBetterJukebox {

    @Shadow
    private boolean isPlaying;
    @Unique
    private float amendments$rot = 0;
    @Unique
    private float amendments$prevRot = 0;

    protected JukeboxBlockEntityMixin(BlockEntityType<?> blockEntityType, BlockPos blockPos, BlockState blockState) {
        super(blockEntityType, blockPos, blockState);
    }

    @Inject(method = "removeItem", at = @At("TAIL"))
    public void notifyRemovedItem(int slot, int amount, CallbackInfoReturnable<ItemStack> cir) {
        if (level != null) level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
    }

    @Inject(method = "setItem", at = @At("TAIL"))
    public void notifyAddedItem(int slot, ItemStack stack, CallbackInfo ci) {
        if (level != null) level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
    }

    @Inject(method = "tick", at = @At("TAIL"))
    public void tick(Level level, BlockPos blockPos, BlockState blockState, CallbackInfo ci) {
        amendments$prevRot = amendments$rot;
        if (isPlaying) {
            amendments$rot += 1;
            amendments$rot %= 360;
        } else if (amendments$rot > 0.0) {
            amendments$rot -= 5;
            if (amendments$rot < 0) {
                amendments$rot = 0;
            }
            amendments$rot %= 360;
        }
    }

    @Override
    public CompoundTag getUpdateTag() {
        return this.saveWithoutMetadata();
    }

    @Nullable
    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public float amendments$getRotation(float partialTicks) {
        return Mth.rotLerp(partialTicks, amendments$prevRot, amendments$rot);
    }

    @Override
    public void amendments$setPlaying(boolean playing) {
        isPlaying = playing;
    }
}
