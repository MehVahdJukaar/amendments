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

    public ClientBoundFireballExplodePacket(double x, double y, double z, float power, List<BlockPos> toBlow, @Nullable Vec3 knockback) {
        super(x, y, z, power, toBlow, knockback);
    }

    public ClientBoundFireballExplodePacket(FriendlyByteBuf buffer) {
        super(buffer);
    }

    @Override
    public void writeToBuffer(FriendlyByteBuf friendlyByteBuf) {
        this.write(friendlyByteBuf);
    }

    @Override
    public void handle(ChannelHandler.Context context) {
        AmendmentsClient.withClientLevel(level -> {
            FireballExplosion explosion = new FireballExplosion(level,  null, this.getX(), this.getY(), this.getZ(), this.getPower(), this.getToBlow());
            explosion.finalizeExplosion(true);
            context.getSender().setDeltaMovement(context.getSender().getDeltaMovement().add((double) this.getKnockbackX(), (double) this.getKnockbackY(), (double) this.getKnockbackZ()));

        });
     }
}
