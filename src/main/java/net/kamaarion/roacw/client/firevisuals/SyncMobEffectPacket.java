package net.kamaarion.roacw.client.firevisuals;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class SyncMobEffectPacket {
    private final int entityId;
    private final ResourceLocation effectId;
    private final boolean active;

    public SyncMobEffectPacket(int entityId, ResourceLocation effectId, boolean active) {
        this.entityId = entityId;
        this.effectId = effectId;
        this.active = active;
    }

    public SyncMobEffectPacket(FriendlyByteBuf buf) {
        this.entityId = buf.readVarInt();
        this.effectId = buf.readResourceLocation();
        this.active = buf.readBoolean();
    }

    public void toBytes(FriendlyByteBuf buf) {
        buf.writeVarInt(entityId);
        buf.writeResourceLocation(effectId);
        buf.writeBoolean(active);
    }

    public void handle(Supplier<NetworkEvent.Context> ctx) {
        NetworkEvent.Context context = ctx.get();

        context.enqueueWork(() ->
                DistExecutor.unsafeRunWhenOn(
                        Dist.CLIENT,
                        () -> () -> ClientFireEffectCache.set(entityId, effectId, active)
                )
        );

        context.setPacketHandled(true);
    }
}