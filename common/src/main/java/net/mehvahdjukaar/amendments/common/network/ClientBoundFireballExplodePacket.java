package net.mehvahdjukaar.amendments.common.network;

import net.mehvahdjukaar.amendments.Amendments;
import net.mehvahdjukaar.amendments.AmendmentsClient;
import net.mehvahdjukaar.amendments.common.entity.FireballExplosion;
import net.mehvahdjukaar.moonlight.api.platform.network.Message;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.network.protocol.game.ClientboundExplodePacket;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public record ClientBoundFireballExplodePacket(ClientboundExplodePacket vanilla, float soundVolume) implements Message {

    public static final TypeAndCodec<RegistryFriendlyByteBuf, ClientBoundFireballExplodePacket> TYPE = Message.makeType(
            Amendments.res("client_bound_fireball_explode"), ClientBoundFireballExplodePacket::new);

    public ClientBoundFireballExplodePacket(double x, double y, double z, float power, List<BlockPos> toBlow,
                                            @Nullable Vec3 knockback, Explosion.BlockInteraction blockInteraction,
                                            ParticleOptions smallExplosionParticles, ParticleOptions largeExplosionParticles,
                                            Holder<SoundEvent> explosionSound, float soundVolume) {
        this(new ClientboundExplodePacket(x, y, z, power, toBlow, knockback, blockInteraction,
                smallExplosionParticles, largeExplosionParticles, explosionSound), soundVolume);
    }

    public ClientBoundFireballExplodePacket(RegistryFriendlyByteBuf buffer) {
        this(new ClientboundExplodePacket(buffer),buffer.readFloat());
    }

    @Override
    public void write(RegistryFriendlyByteBuf buf) {
        vanilla.write(buf);
        buf.writeFloat(this.soundVolume);
    }

    @Override
    public void handle(Context context) {
        AmendmentsClient.withClientLevel(level -> {
            var settings = new FireballExplosion.ExtraSettings();
            settings.soundVolume = this.soundVolume;
            FireballExplosion explosion = new FireballExplosion(level, null, vanilla.getX(), vanilla.getY(), vanilla.getZ(),
                    vanilla.getPower(), vanilla.getToBlow(),
                    vanilla.getBlockInteraction(), vanilla.getSmallExplosionParticles(),
                    vanilla.getLargeExplosionParticles(), vanilla.getExplosionSound(),
                    settings);
            explosion.finalizeExplosion(true);
            context.getPlayer().setDeltaMovement(context.getPlayer().getDeltaMovement()
                    .add(vanilla.getKnockbackX(), vanilla.getKnockbackY(), vanilla.getKnockbackZ()));

        });
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE.type();
    }


}
