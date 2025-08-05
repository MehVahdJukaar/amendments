package net.mehvahdjukaar.amendments.common.network;

import net.mehvahdjukaar.amendments.AmendmentsClient;
import net.mehvahdjukaar.amendments.common.entity.FireballExplosion;
import net.mehvahdjukaar.moonlight.api.platform.network.ChannelHandler;
import net.mehvahdjukaar.moonlight.api.platform.network.Message;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.PacketUtils;
import net.minecraft.network.protocol.game.ClientboundExplodePacket;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class ClientBoundFireballExplodePacket extends ClientboundExplodePacket implements Message {

    private float soundVolume = 4;
    public ClientBoundFireballExplodePacket(double x, double y, double z, float power, List<BlockPos> toBlow,
                                            @Nullable Vec3 knockback, float soundVolume) {
        super(x, y, z, power, toBlow, knockback);
        this.soundVolume = soundVolume;
    }

    public ClientBoundFireballExplodePacket(FriendlyByteBuf buffer) {
        super(buffer);
        this.soundVolume = buffer.readFloat();
    }

    @Override
    public void writeToBuffer(FriendlyByteBuf friendlyByteBuf) {
        this.write(friendlyByteBuf);
        friendlyByteBuf.writeFloat(this.soundVolume);
    }

    @Override
    public void handle(ChannelHandler.Context context) {
        AmendmentsClient.withClientLevel(level -> {
            var settings = new FireballExplosion.ExtraSettings();
            settings.soundVolume = this.soundVolume;
            FireballExplosion explosion = new FireballExplosion(level,  null, this.getX(), this.getY(), this.getZ(),
                    this.getPower(), this.getToBlow(), settings);
            explosion.finalizeExplosion(true);
            context.getSender().setDeltaMovement(context.getSender().getDeltaMovement().add((double) this.getKnockbackX(), (double) this.getKnockbackY(), (double) this.getKnockbackZ()));

        });
     }
}
