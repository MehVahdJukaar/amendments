package net.mehvahdjukaar.amendments.common.network;

import net.mehvahdjukaar.amendments.Amendments;
import net.mehvahdjukaar.amendments.AmendmentsClient;
import net.mehvahdjukaar.amendments.reg.ModRegistry;
import net.mehvahdjukaar.moonlight.api.platform.network.Message;
import net.minecraft.core.Holder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.phys.Vec3;

import java.util.Optional;

public record ClientBoundFireballExplodePacket(Vec3 center, float radius, Optional<Vec3> knockback,
                                               Holder<SoundEvent> sound, float soundVolume) implements Message {

    public static final TypeAndCodec<RegistryFriendlyByteBuf, ClientBoundFireballExplodePacket> TYPE = Message.makeType(
            Amendments.res("client_bound_fireball_explode"), ClientBoundFireballExplodePacket::new);

    public ClientBoundFireballExplodePacket(RegistryFriendlyByteBuf buffer) {
        this(Vec3.STREAM_CODEC.decode(buffer), buffer.readFloat(),
                buffer.readOptional(Vec3.STREAM_CODEC::decode),
                SoundEvent.STREAM_CODEC.decode(buffer), buffer.readFloat());
    }

    @Override
    public void write(RegistryFriendlyByteBuf buf) {
        Vec3.STREAM_CODEC.encode(buf, this.center);
        buf.writeFloat(this.radius);
        buf.writeOptional(this.knockback, Vec3.STREAM_CODEC::encode);
        SoundEvent.STREAM_CODEC.encode(buf, this.sound);
        buf.writeFloat(this.soundVolume);
    }

    @Override
    public void handle(Context context) {
        AmendmentsClient.withClientLevel(level -> {
            float pitch = (1.0F + (level.getRandom().nextFloat() - level.getRandom().nextFloat()) * 0.2F) * 0.7F;
            level.playLocalSound(center.x, center.y, center.z, this.sound.value(), SoundSource.BLOCKS,
                    this.soundVolume, pitch, false);
            level.addParticle(ModRegistry.FIREBALL_EMITTER_PARTICLE.get(), center.x, center.y, center.z,
                    this.radius, 0.0, 0.0);
            this.knockback.ifPresent(context.getPlayer()::addDeltaMovement);
        });
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE.type();
    }
}
