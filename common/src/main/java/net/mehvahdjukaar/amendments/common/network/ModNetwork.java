package net.mehvahdjukaar.amendments.common.network;

import net.mehvahdjukaar.amendments.Amendments;
import net.mehvahdjukaar.moonlight.api.platform.network.ChannelHandler;
import net.mehvahdjukaar.moonlight.api.platform.network.NetworkDir;

public class ModNetwork {

    public static void init() {
    }

    public static final ChannelHandler CHANNEL = ChannelHandler.builder(Amendments.MOD_ID)
            .register(NetworkDir.PLAY_TO_SERVER, ServerBoundSyncLecternBookMessage.class, ServerBoundSyncLecternBookMessage::new)
            .register(NetworkDir.PLAY_TO_CLIENT, ClientBoundPlaySplashParticlesMessage.class, ClientBoundPlaySplashParticlesMessage::new)
            .register(NetworkDir.PLAY_TO_CLIENT, ClientBoundEntityHitSwayingBlockMessage.class, ClientBoundEntityHitSwayingBlockMessage::new)
            .build();
}
