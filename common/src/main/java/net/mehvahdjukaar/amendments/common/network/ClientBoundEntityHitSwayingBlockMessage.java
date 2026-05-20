package net.mehvahdjukaar.amendments.common.network;

import net.mehvahdjukaar.amendments.Amendments;
import net.mehvahdjukaar.amendments.common.ISwingingTile;
import net.mehvahdjukaar.moonlight.api.platform.network.Message;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;

public record ClientBoundEntityHitSwayingBlockMessage(BlockPos pos, int entity) implements Message {

    public static final TypeAndCodec<RegistryFriendlyByteBuf, ClientBoundEntityHitSwayingBlockMessage> TYPE = Message.makeType(
            Amendments.res("client_bound_sync_swaying_tile"), ClientBoundEntityHitSwayingBlockMessage::new);

    public ClientBoundEntityHitSwayingBlockMessage(FriendlyByteBuf buffer) {
        this(buffer.readBlockPos(), buffer.readVarInt());
    }

    @Override
    public void write(RegistryFriendlyByteBuf buf) {
        buf.writeBlockPos(this.pos);
        buf.writeVarInt(this.entity);
    }

    @Override
    public void handle(Context context) {
        Level level = context.getPlayer().level();
        Entity e = level.getEntity(entity);
        if (level.getBlockEntity(pos) instanceof ISwingingTile tile && e != null) {
            tile.amendments$getAnimation().hitByEntity(e, level.getBlockState(pos), pos);
        }
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE.type();
    }
}
