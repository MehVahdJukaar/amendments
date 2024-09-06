package net.mehvahdjukaar.amendments.mixins;

import net.mehvahdjukaar.amendments.common.IBetterJukebox;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.JukeboxSongPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.JukeboxBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.ticks.ContainerSingleItem;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.awt.geom.Area;

@Mixin(JukeboxBlockEntity.class)
public abstract class JukeboxBlockEntityMixin extends BlockEntity implements IBetterJukebox, ContainerSingleItem.BlockContainerSingleItem {

    @Shadow @Final private JukeboxSongPlayer jukeboxSongPlayer;
    @Unique
    private float amendments$rot = 0;
    @Unique
    private float amendments$prevRot = 0;

    protected JukeboxBlockEntityMixin(BlockEntityType<?> blockEntityType, BlockPos blockPos, BlockState blockState) {
        super(blockEntityType, blockPos, blockState);
    }

    @Inject(method = "onSongChanged", at = @At("TAIL"))
    public void amendments$notifySongChanged(CallbackInfo ci) {
        if (level != null) level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
    }

    @Inject(method = "tick", at = @At("TAIL"))
    private static void amendments$tickAnimation(Level level, BlockPos pos, BlockState state,
                                                 JukeboxBlockEntity jukebox, CallbackInfo ci) {
        if (!level.isClientSide) return;
        ((IBetterJukebox) jukebox).amendments$tickAnimation();
    }

    @Override
    public void amendments$tickAnimation() {
        amendments$prevRot = amendments$rot;
        if (jukeboxSongPlayer.isPlaying()) {
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
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        return this.saveWithoutMetadata(registries);
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
}
