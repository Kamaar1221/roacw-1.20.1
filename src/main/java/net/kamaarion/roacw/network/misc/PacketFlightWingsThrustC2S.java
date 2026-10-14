package net.kamaarion.roacw.network.misc;

import net.kamaarion.roacw.items.curios.wings.FlightWingsHandler;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class PacketFlightWingsThrustC2S {
    private final boolean held;

    public PacketFlightWingsThrustC2S(boolean held) {
        this.held = held;
    }

    public PacketFlightWingsThrustC2S(FriendlyByteBuf buf) {
        this.held = buf.readBoolean();
    }

    public void toBytes(FriendlyByteBuf buf) {
        buf.writeBoolean(held);
    }

    public void handle(Supplier<NetworkEvent.Context> supplier) {
        ServerPlayer player = supplier.get().getSender();
        if (player != null) {
            FlightWingsHandler.setHeld(player, held);
        }
    }
}