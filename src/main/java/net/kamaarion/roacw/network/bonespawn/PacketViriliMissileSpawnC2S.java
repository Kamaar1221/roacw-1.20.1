package net.kamaarion.roacw.network.bonespawn;

import net.kamaarion.roacw.entity.summon.virili.ViriliEntity;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class PacketViriliMissileSpawnC2S {

    private static final double MAX_OFFSET_BLOCKS = 4.0D;
    private static final double MAX_OFFSET_BLOCKS_SQR = MAX_OFFSET_BLOCKS * MAX_OFFSET_BLOCKS;

    private final int entityId;
    private final double worldX;
    private final double worldY;
    private final double worldZ;
    private final double entityX;
    private final double entityY;
    private final double entityZ;

    public PacketViriliMissileSpawnC2S(
            int entityId,
            double worldX,
            double worldY,
            double worldZ,
            double entityX,
            double entityY,
            double entityZ
    ) {
        this.entityId = entityId;
        this.worldX = worldX;
        this.worldY = worldY;
        this.worldZ = worldZ;
        this.entityX = entityX;
        this.entityY = entityY;
        this.entityZ = entityZ;
    }

    public PacketViriliMissileSpawnC2S(FriendlyByteBuf buf) {
        this.entityId = buf.readInt();
        this.worldX = buf.readDouble();
        this.worldY = buf.readDouble();
        this.worldZ = buf.readDouble();
        this.entityX = buf.readDouble();
        this.entityY = buf.readDouble();
        this.entityZ = buf.readDouble();
    }

    public void toBytes(FriendlyByteBuf buf) {
        buf.writeInt(this.entityId);
        buf.writeDouble(this.worldX);
        buf.writeDouble(this.worldY);
        buf.writeDouble(this.worldZ);
        buf.writeDouble(this.entityX);
        buf.writeDouble(this.entityY);
        buf.writeDouble(this.entityZ);
    }

    public void handle(Supplier<NetworkEvent.Context> ctxSupplier) {
        NetworkEvent.Context ctx = ctxSupplier.get();

        ctx.enqueueWork(() -> {
            ServerPlayer sender = ctx.getSender();

            if (sender == null) {
                return;
            }

            Entity entity = sender.level().getEntity(this.entityId);

            if (!(entity instanceof ViriliEntity virili)) {
                return;
            }

            double boneOffsetX = this.worldX - this.entityX;
            double boneOffsetY = this.worldY - this.entityY;
            double boneOffsetZ = this.worldZ - this.entityZ;

            double offsetDistanceSqr = boneOffsetX * boneOffsetX
                    + boneOffsetY * boneOffsetY
                    + boneOffsetZ * boneOffsetZ;

            if (offsetDistanceSqr > MAX_OFFSET_BLOCKS_SQR) {
                return;
            }

            virili.setServerMissileSpawnPosition(
                    this.worldX,
                    this.worldY,
                    this.worldZ
            );
        });

        ctx.setPacketHandled(true);
    }
}