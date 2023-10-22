package net.mehvahdjukaar.amendments.common.network;

import net.mehvahdjukaar.amendments.Amendments;
import net.mehvahdjukaar.moonlight.api.platform.network.ChannelHandler;
import net.mehvahdjukaar.moonlight.api.platform.network.NetworkDir;

public class ModNetwork {

    public static void init(){

    }

    public static final ChannelHandler CHANNEL ;

    static {
        CHANNEL = ChannelHandler.createChannel(Amendments.res("network"));
        CHANNEL.register(NetworkDir.PLAY_TO_SERVER, SyncLecternBookMessage.class, SyncLecternBookMessage::new);
    }

}
