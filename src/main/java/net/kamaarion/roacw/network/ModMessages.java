package net.kamaarion.roacw.network;

import net.kamaarion.roacw.ROACW;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;

public class ModMessages {
    private static SimpleChannel INSTANCE;
    private static int packetId = 0;
    private static int id() { return packetId++; }

    public static void register() {
        SimpleChannel net = NetworkRegistry.ChannelBuilder
                .named(new ResourceLocation(ROACW.MODID, "messages"))
                .networkProtocolVersion(() -> "1.0")
                .clientAcceptedVersions(s -> true)
                .serverAcceptedVersions(s -> true)
                .simpleChannel();

        INSTANCE = net;

        net.messageBuilder(PacketEvasionScarfDashC2S.class, id(), NetworkDirection.PLAY_TO_SERVER)
                .decoder(PacketEvasionScarfDashC2S::new)
                .encoder(PacketEvasionScarfDashC2S::toBytes)
                .consumerMainThread(PacketEvasionScarfDashC2S::handle)
                .add();

        net.messageBuilder(PacketAuricTeslaDashC2S.class, id(), NetworkDirection.PLAY_TO_SERVER)
                .decoder(PacketAuricTeslaDashC2S::new)
                .encoder(PacketAuricTeslaDashC2S::toBytes)
                .consumerMainThread(PacketAuricTeslaDashC2S::handle)
                .add();

        net.messageBuilder(PacketEarthenPaladinPulseC2S.class, id(), NetworkDirection.PLAY_TO_SERVER)
                .decoder(PacketEarthenPaladinPulseC2S::new)
                .encoder(PacketEarthenPaladinPulseC2S::toBytes)
                .consumerMainThread(PacketEarthenPaladinPulseC2S::handle)
                .add();

        net.messageBuilder(PacketPlaguebringerJetBoostC2S.class, id(), NetworkDirection.PLAY_TO_SERVER)
                .decoder(PacketPlaguebringerJetBoostC2S::new)
                .encoder(PacketPlaguebringerJetBoostC2S::toBytes)
                .consumerMainThread(PacketPlaguebringerJetBoostC2S::handle)
                .add();

        net.messageBuilder(PacketPlaguebringerJetBoostStateS2C.class, id(), NetworkDirection.PLAY_TO_CLIENT)
                .decoder(PacketPlaguebringerJetBoostStateS2C::new)
                .encoder(PacketPlaguebringerJetBoostStateS2C::toBytes)
                .consumerMainThread(PacketPlaguebringerJetBoostStateS2C::handle)
                .add();

    }

    public static <MSG> void sendToServer(MSG message) {
        INSTANCE.sendToServer(message);
    }

    // Sends to the given player plus everyone currently tracking them -
    // use for state that needs to be visible in third person too (e.g.
    // jet boost active state for armor particle rendering).
    public static <MSG> void sendToTrackingAndSelf(MSG message, ServerPlayer player) {
        INSTANCE.send(PacketDistributor.TRACKING_ENTITY_AND_SELF.with(() -> player), message);
    }
}