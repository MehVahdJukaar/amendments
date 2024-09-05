package net.mehvahdjukaar.amendments.common.network;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.mehvahdjukaar.amendments.common.ISwingingTile;
import net.mehvahdjukaar.amendments.common.tile.SwayingBlockTile;
import net.mehvahdjukaar.moonlight.api.platform.network.ChannelHandler;
import net.mehvahdjukaar.moonlight.api.platform.network.Message;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.Entity;

public record ClientBoundEntityHitSwayingBlockMessage(BlockPos pos, int entity) implements Message {

    public ClientBoundEntityHitSwayingBlockMessage(FriendlyByteBuf buffer) {
        this(buffer.readBlockPos(), buffer.readVarInt());
    }

    @Override
    public void writeToBuffer(FriendlyByteBuf friendlyByteBuf) {
        friendlyByteBuf.writeBlockPos(this.pos);
        friendlyByteBuf.writeVarInt(this.entity);
    }

    @Override
    public void handle(ChannelHandler.Context context) {
        doOnClient();
    }

    @Environment(EnvType.CLIENT)
    public void doOnClient() {
        ClientLevel level = Minecraft.getInstance().level;
        if (level != null) {
            Entity e = level.getEntity(entity);
            if (level.getBlockEntity(pos) instanceof ISwingingTile tile && e != null) {
                tile.amendments$getAnimation().hitByEntity(e, level.getBlockState(pos), pos);
            }
        }
    }
}
