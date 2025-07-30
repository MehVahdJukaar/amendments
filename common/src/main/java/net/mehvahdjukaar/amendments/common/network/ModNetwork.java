package net.mehvahdjukaar.amendments.common.network;

import net.mehvahdjukaar.moonlight.api.platform.network.NetworkHelper;

public class ModNetwork {

    public static void init() {
        NetworkHelper.addNetworkRegistration(ModNetwork::registerMessages, 2);
    }

    private static void registerMessages(NetworkHelper.RegisterMessagesEvent event) {
        event.registerServerBound(ServerBoundSyncLecternBookMessage.TYPE);
        event.registerClientBound(ClientBoundEntityHitSwayingBlockMessage.TYPE);
        event.registerClientBound(ClientBoundPlaySplashParticlesMessage.TYPE);
    }

    public static final ChannelHandler CHANNEL = ChannelHandler.builder(Amendments.MOD_ID)
            .version(2)
            .register(NetworkDir.PLAY_TO_SERVER, ServerBoundSyncLecternBookMessage.class, ServerBoundSyncLecternBookMessage::new)
            .register(NetworkDir.PLAY_TO_CLIENT, ClientBoundPlaySplashParticlesMessage.class, ClientBoundPlaySplashParticlesMessage::new)
            .register(NetworkDir.PLAY_TO_CLIENT, ClientBoundEntityHitSwayingBlockMessage.class, ClientBoundEntityHitSwayingBlockMessage::new)
            .register(NetworkDir.PLAY_TO_CLIENT, ClientBoundFireballExplodePacket.class, ClientBoundFireballExplodePacket::new)
            .build();
}
