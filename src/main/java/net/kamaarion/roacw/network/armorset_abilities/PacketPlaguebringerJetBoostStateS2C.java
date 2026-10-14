package net.kamaarion.roacw.network.armorset_abilities;

import net.kamaarion.roacw.ROACW;
import net.kamaarion.roacw.items.armor.plaguebringerarmorset.PlaguebringerJetBoostVisualEffects;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.network.NetworkEvent;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.function.Supplier;

@Mod.EventBusSubscriber(modid = ROACW.MODID, value = Dist.CLIENT)
public class PacketPlaguebringerJetBoostStateS2C {

    private static final Set<UUID> CLIENT_ACTIVE_JETBOOST_PLAYERS = new HashSet<>();

    private final UUID playerUUID;
    private final boolean active;

    public PacketPlaguebringerJetBoostStateS2C(UUID playerUUID, boolean active) {
        this.playerUUID = playerUUID;
        this.active = active;
    }

    public PacketPlaguebringerJetBoostStateS2C(FriendlyByteBuf buf) {
        this.playerUUID = buf.readUUID();
        this.active = buf.readBoolean();
    }

    public void toBytes(FriendlyByteBuf buf) {
        buf.writeUUID(playerUUID);
        buf.writeBoolean(active);
    }

    public static boolean isJetBoostActive(UUID uuid) {
        return CLIENT_ACTIVE_JETBOOST_PLAYERS.contains(uuid);
    }

    public boolean handle(Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context context = supplier.get();

        context.enqueueWork(() ->
                DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> {
                    Minecraft mc = Minecraft.getInstance();

                    if (active) {
                        CLIENT_ACTIVE_JETBOOST_PLAYERS.add(playerUUID);

                        if (mc.player != null && mc.player.getUUID().equals(playerUUID)) {
                            mc.player.getAbilities().mayfly = true;
                            mc.player.onUpdateAbilities();
                            mc.player.fallDistance = 0;

                            PlaguebringerJetBoostVisualEffects.beginPreFlight();
                        }
                    } else {
                        CLIENT_ACTIVE_JETBOOST_PLAYERS.remove(playerUUID);

                        if (mc.player != null && mc.player.getUUID().equals(playerUUID)) {
                            mc.player.getAbilities().flying = false;
                            mc.player.onUpdateAbilities();

                            PlaguebringerJetBoostVisualEffects.resetPreFlight();
                        }
                    }
                })
        );

        context.setPacketHandled(true);
        return true;
    }

    @SubscribeEvent
    public static void onLoggingOut(ClientPlayerNetworkEvent.LoggingOut event) {
        CLIENT_ACTIVE_JETBOOST_PLAYERS.clear();
    }
}