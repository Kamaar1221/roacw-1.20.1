package net.kamaarion.roacw.network.misc;

import net.kamaarion.roacw.ripper.RipperManager;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class PacketActivateRipperC2S {
    private final boolean adrenaline;

    public PacketActivateRipperC2S(boolean adrenaline) {
        this.adrenaline = adrenaline;
    }

    public PacketActivateRipperC2S(FriendlyByteBuf buf) {
        this.adrenaline = buf.readBoolean();
    }

    public void toBytes(FriendlyByteBuf buf) {
        buf.writeBoolean(adrenaline);
    }

    public boolean handle(Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context context = supplier.get();
        context.enqueueWork(() -> {
            ServerPlayer player = context.getSender();
            if (player != null) {
                RipperManager.activate(player, adrenaline);
            }
        });
        return true;
    }
}