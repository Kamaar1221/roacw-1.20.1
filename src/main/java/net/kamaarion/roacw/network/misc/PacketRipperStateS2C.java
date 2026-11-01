package net.kamaarion.roacw.network.misc;

import net.kamaarion.roacw.client.ripper.ClientRipperData;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class PacketRipperStateS2C {
    private final int rage;
    private final int adrenaline;
    private final int rageActiveTicks;
    private final int adrenalineActiveTicks;

    public PacketRipperStateS2C(int rage, int adrenaline, int rageActiveTicks, int adrenalineActiveTicks) {
        this.rage = rage;
        this.adrenaline = adrenaline;
        this.rageActiveTicks = rageActiveTicks;
        this.adrenalineActiveTicks = adrenalineActiveTicks;
    }

    public PacketRipperStateS2C(FriendlyByteBuf buf) {
        this.rage = buf.readVarInt();
        this.adrenaline = buf.readVarInt();
        this.rageActiveTicks = buf.readVarInt();
        this.adrenalineActiveTicks = buf.readVarInt();
    }

    public void toBytes(FriendlyByteBuf buf) {
        buf.writeVarInt(rage);
        buf.writeVarInt(adrenaline);
        buf.writeVarInt(rageActiveTicks);
        buf.writeVarInt(adrenalineActiveTicks);
    }

    public boolean handle(Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context context = supplier.get();
        context.enqueueWork(() -> ClientRipperData.set(rage, adrenaline, rageActiveTicks, adrenalineActiveTicks));
        return true;
    }
}