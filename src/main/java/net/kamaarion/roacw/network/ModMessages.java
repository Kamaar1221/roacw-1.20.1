package net.kamaarion.roacw.network;

import net.kamaarion.roacw.ROACW;
import net.kamaarion.roacw.client.firevisuals.SyncMobEffectPacket;
import net.kamaarion.roacw.network.armorset_abilities.*;
import net.kamaarion.roacw.network.bonespawn.PacketViriliMissileSpawnC2S;
import net.kamaarion.roacw.network.misc.PacketFlightWingsThrustC2S;
import net.kamaarion.roacw.network.misc.PacketFlightWingsTimeS2C;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;

public class ModMessages {

    private static SimpleChannel INSTANCE;
    private static int packetId = 0;

    private static int id() {
        return packetId++;
    }

    public static void register() {
        SimpleChannel net = NetworkRegistry.ChannelBuilder
                .named(ResourceLocation.fromNamespaceAndPath(ROACW.MODID, "messages"))
                .networkProtocolVersion(() -> "1.0")
                .clientAcceptedVersions(s -> true)
                .serverAcceptedVersions(s -> true)
                .simpleChannel();

        INSTANCE = net;

        net.messageBuilder(
                        PacketEvasionScarfDashC2S.class,
                        id(),
                        NetworkDirection.PLAY_TO_SERVER
                )
                .decoder(PacketEvasionScarfDashC2S::new)
                .encoder(PacketEvasionScarfDashC2S::toBytes)
                .consumerMainThread(PacketEvasionScarfDashC2S::handle)
                .add();

        net.messageBuilder(
                        PacketAuricTeslaDashC2S.class,
                        id(),
                        NetworkDirection.PLAY_TO_SERVER
                )
                .decoder(PacketAuricTeslaDashC2S::new)
                .encoder(PacketAuricTeslaDashC2S::toBytes)
                .consumerMainThread(PacketAuricTeslaDashC2S::handle)
                .add();

        net.messageBuilder(
                        PacketEarthenPaladinPulseC2S.class,
                        id(),
                        NetworkDirection.PLAY_TO_SERVER
                )
                .decoder(PacketEarthenPaladinPulseC2S::new)
                .encoder(PacketEarthenPaladinPulseC2S::toBytes)
                .consumerMainThread(PacketEarthenPaladinPulseC2S::handle)
                .add();

        net.messageBuilder(
                        PacketPlaguebringerJetBoostC2S.class,
                        id(),
                        NetworkDirection.PLAY_TO_SERVER
                )
                .decoder(PacketPlaguebringerJetBoostC2S::new)
                .encoder(PacketPlaguebringerJetBoostC2S::toBytes)
                .consumerMainThread(PacketPlaguebringerJetBoostC2S::handle)
                .add();

        net.messageBuilder(
                        PacketPlaguebringerJetBoostStateS2C.class,
                        id(),
                        NetworkDirection.PLAY_TO_CLIENT
                )
                .decoder(PacketPlaguebringerJetBoostStateS2C::new)
                .encoder(PacketPlaguebringerJetBoostStateS2C::toBytes)
                .consumerMainThread(PacketPlaguebringerJetBoostStateS2C::handle)
                .add();

        net.messageBuilder(
                        PacketViriliMissileSpawnC2S.class,
                        id(),
                        NetworkDirection.PLAY_TO_SERVER
                )
                .decoder(PacketViriliMissileSpawnC2S::new)
                .encoder(PacketViriliMissileSpawnC2S::toBytes)
                .consumerMainThread(PacketViriliMissileSpawnC2S::handle)
                .add();

        net.messageBuilder(
                        MarsCannonsSummonC2S.class,
                        id(),
                        NetworkDirection.PLAY_TO_SERVER
                )
                .decoder(MarsCannonsSummonC2S::new)
                .encoder(MarsCannonsSummonC2S::toBytes)
                .consumerMainThread(MarsCannonsSummonC2S::handle)
                .add();

        net.messageBuilder(
                        MarsArmorBoosterStateS2C.class,
                        id(),
                        NetworkDirection.PLAY_TO_CLIENT
                )
                .decoder(MarsArmorBoosterStateS2C::new)
                .encoder(MarsArmorBoosterStateS2C::toBytes)
                .consumerMainThread(MarsArmorBoosterStateS2C::handle)
                .add();

        net.messageBuilder(
                        SyncMobEffectPacket.class,
                        id(),
                        NetworkDirection.PLAY_TO_CLIENT
                )
                .decoder(SyncMobEffectPacket::new)
                .encoder(SyncMobEffectPacket::toBytes)
                .consumerMainThread(SyncMobEffectPacket::handle)
                .add();

        net.messageBuilder(
                        PacketFlightWingsThrustC2S.class,
                        id(),
                        NetworkDirection.PLAY_TO_SERVER
                )
                .decoder(PacketFlightWingsThrustC2S::new)
                .encoder(PacketFlightWingsThrustC2S::toBytes)
                .consumerMainThread(PacketFlightWingsThrustC2S::handle)
                .add();

        net.messageBuilder(
                        PacketFlightWingsTimeS2C.class,
                        id(),
                        NetworkDirection.PLAY_TO_CLIENT
                )
                .decoder(PacketFlightWingsTimeS2C::new)
                .encoder(PacketFlightWingsTimeS2C::toBytes)
                .consumerMainThread(PacketFlightWingsTimeS2C::handle)
                .add();
    }

    public static <MSG> void sendToServer(MSG message) {
        INSTANCE.sendToServer(message);
    }

    public static <MSG> void sendToTrackingAndSelf(
            MSG message,
            ServerPlayer player
    ) {
        INSTANCE.send(
                PacketDistributor.TRACKING_ENTITY_AND_SELF.with(() -> player),
                message
        );
    }

    public static <MSG> void sendToPlayer(
            MSG message,
            ServerPlayer player
    ) {
        INSTANCE.send(
                PacketDistributor.PLAYER.with(() -> player),
                message
        );
    }

    public static <MSG> void sendToTracking(
            MSG message,
            Entity entity
    ) {
        INSTANCE.send(
                PacketDistributor.TRACKING_ENTITY_AND_SELF.with(() -> entity),
                message
        );
    }
}