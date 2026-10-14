package net.kamaarion.roacw.network.misc;

import net.kamaarion.roacw.items.curios.wings.FlightWings;
import net.kamaarion.roacw.items.curios.wings.FlightWingsClient;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class PacketFlightWingsTimeS2C {
    private final int ticks;
    private final FlightWings.FallMode fallMode;

    public PacketFlightWingsTimeS2C(int ticks, FlightWings.FallMode fallMode) {
        this.ticks = ticks;
        this.fallMode = fallMode;
    }

    public PacketFlightWingsTimeS2C(FriendlyByteBuf buf) {
        this.ticks = buf.readVarInt();
        this.fallMode = buf.readEnum(FlightWings.FallMode.class);
    }

    public void toBytes(FriendlyByteBuf buf) {
        buf.writeVarInt(ticks);
        buf.writeEnum(fallMode);
    }

    public void handle(Supplier<NetworkEvent.Context> supplier) {
        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> FlightWingsClient.setState(ticks, fallMode));
    }
}