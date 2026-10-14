package net.kamaarion.roacw.network.armorset_abilities;

import net.kamaarion.roacw.items.armor.marsarmorset.MarsArmorParticleHandler;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.UUID;
import java.util.function.Supplier;

public class MarsArmorBoosterStateS2C {

    private final UUID playerUuid;
    private final boolean active;

    public MarsArmorBoosterStateS2C(UUID playerUuid, boolean active) {
        this.playerUuid = playerUuid;
        this.active = active;
    }

    public MarsArmorBoosterStateS2C(FriendlyByteBuf buf) {
        this.playerUuid = buf.readUUID();
        this.active = buf.readBoolean();
    }

    public void toBytes(FriendlyByteBuf buf) {
        buf.writeUUID(playerUuid);
        buf.writeBoolean(active);
    }

    public boolean handle(Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context context = supplier.get();
        context.enqueueWork(() ->
                DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () ->
                        MarsArmorParticleHandler.setBoosterActive(playerUuid, active)));

        context.setPacketHandled(true);
        return true;
    }
}