package dev.ripio.cobbleloots.network;

import dev.architectury.injectables.annotations.ExpectPlatform;

public class CobblelootsClientNetworkSender {
    @ExpectPlatform
    public static void sendLootBallUpdate(CobblelootsLootBallUpdatePayload payload) {
        throw new AssertionError();
    }
}
