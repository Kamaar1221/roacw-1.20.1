package net.kamaarion.roacw.items.armor.marsarmorset;

import net.kamaarion.roacw.Config;
import net.kamaarion.roacw.network.ModMessages;
import net.kamaarion.roacw.network.armorset_abilities.MarsArmorBoosterStateS2C;
import net.kamaarion.roacw.network.armorset_abilities.MarsCannonsSummonC2S;
import net.kamaarion.roacw.registeries.ROACWItemRegistry;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingFallEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Mod.EventBusSubscriber(modid = "roacw", bus = Mod.EventBusSubscriber.Bus.FORGE)
public class MarsArmorFlightHandler {

    private static final Set<UUID> SERVER_FLIGHT_GRANTED = ConcurrentHashMap.newKeySet();
    private static final Set<UUID> CLIENT_FLIGHT_GRANTED = ConcurrentHashMap.newKeySet();
    private static final Set<UUID> SERVER_PROTECTED_PLAYERS = ConcurrentHashMap.newKeySet();
    private static final Set<UUID> CLIENT_PROTECTED_PLAYERS = ConcurrentHashMap.newKeySet();
    private static final Map<UUID, Boolean> LAST_SENT_BOOSTER_STATE = new ConcurrentHashMap<>();

    private static Set<UUID> flightGranted(Player player) {
        return player.level().isClientSide ? CLIENT_FLIGHT_GRANTED : SERVER_FLIGHT_GRANTED;
    }

    private static Set<UUID> protectedPlayers(Player player) {
        return player.level().isClientSide ? CLIENT_PROTECTED_PLAYERS : SERVER_PROTECTED_PLAYERS;
    }

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) {
            return;
        }

        Player player = event.player;
        UUID playerUUID = player.getUUID();
        Set<UUID> granted = flightGranted(player);
        Set<UUID> protectedSet = protectedPlayers(player);

        if (Config.ENABLE_MARS_FLIGHT.get() && isWearingFullSet(player)) {
            if (!player.getAbilities().mayfly) {
                player.getAbilities().mayfly = true;
                player.onUpdateAbilities();
            }

            granted.add(playerUUID);
            protectedSet.remove(playerUUID);
        } else {
            if (granted.remove(playerUUID)
                    && !player.isCreative()
                    && !player.isSpectator()) {
                player.getAbilities().mayfly = false;
                player.getAbilities().flying = false;
                player.onUpdateAbilities();

                if (!player.onGround()) {
                    protectedSet.add(playerUUID);
                }
            }
        }

        if (protectedSet.contains(playerUUID)) {
            if (!player.onGround()) {
                Vec3 motion = player.getDeltaMovement();

                if (motion.y < -0.15) {
                    player.setDeltaMovement(motion.x, -0.15, motion.z);
                    player.hurtMarked = true;
                }

                player.fallDistance = 0.0F;
            } else {
                protectedSet.remove(playerUUID);
            }
        }

        if (player instanceof ServerPlayer serverPlayer) {
            syncBoosterState(serverPlayer);
        }
    }

    @SubscribeEvent
    public static void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) {
            return;
        }

        MarsCannonsSummonC2S.tick();
    }

    private static void syncBoosterState(ServerPlayer player) {
        boolean active = player.getAbilities().flying && isWearingFullSet(player);
        boolean last = LAST_SENT_BOOSTER_STATE.getOrDefault(player.getUUID(), false);

        if (active == last) {
            return;
        }

        LAST_SENT_BOOSTER_STATE.put(player.getUUID(), active);

        ModMessages.sendToTrackingAndSelf(
                new MarsArmorBoosterStateS2C(player.getUUID(), active),
                player
        );
    }

    private static void resyncPlayer(ServerPlayer player) {
        UUID playerUUID = player.getUUID();

        if (SERVER_FLIGHT_GRANTED.contains(playerUUID)) {
            player.onUpdateAbilities();
        }

        boolean active = player.getAbilities().flying && isWearingFullSet(player);
        LAST_SENT_BOOSTER_STATE.put(playerUUID, active);

        ModMessages.sendToTrackingAndSelf(
                new MarsArmorBoosterStateS2C(playerUUID, active),
                player
        );
    }

    @SubscribeEvent
    public static void onPlayerChangedDimension(PlayerEvent.PlayerChangedDimensionEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            resyncPlayer(player);
        }
    }

    @SubscribeEvent
    public static void onPlayerRespawn(PlayerEvent.PlayerRespawnEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            resyncPlayer(player);
        }
    }

    @SubscribeEvent
    public static void onStartTracking(PlayerEvent.StartTracking event) {
        if (event.getTarget() instanceof ServerPlayer target
                && event.getEntity() instanceof ServerPlayer watcher) {
            ModMessages.sendToPlayer(
                    new MarsArmorBoosterStateS2C(
                            target.getUUID(),
                            LAST_SENT_BOOSTER_STATE.getOrDefault(target.getUUID(), false)
                    ),
                    watcher
            );
        }
    }

    @SubscribeEvent
    public static void onPlayerLoggedOut(PlayerEvent.PlayerLoggedOutEvent event) {
        UUID playerUUID = event.getEntity().getUUID();

        SERVER_FLIGHT_GRANTED.remove(playerUUID);
        CLIENT_FLIGHT_GRANTED.remove(playerUUID);
        SERVER_PROTECTED_PLAYERS.remove(playerUUID);
        CLIENT_PROTECTED_PLAYERS.remove(playerUUID);
        LAST_SENT_BOOSTER_STATE.remove(playerUUID);
    }

    @SubscribeEvent
    public static void onPlayerFall(LivingFallEvent event) {
        if (event.getEntity() instanceof Player player) {
            UUID playerUUID = player.getUUID();
            Set<UUID> protectedSet = protectedPlayers(player);

            if (protectedSet.contains(playerUUID)) {
                event.setDistance(0.0F);
                event.setCanceled(true);
                protectedSet.remove(playerUUID);
            }
        }
    }

    public static boolean isWearingFullSet(Player player) {
        ItemStack helmet = player.getItemBySlot(EquipmentSlot.HEAD);
        ItemStack chest = player.getItemBySlot(EquipmentSlot.CHEST);
        ItemStack legs = player.getItemBySlot(EquipmentSlot.LEGS);
        ItemStack boots = player.getItemBySlot(EquipmentSlot.FEET);

        return helmet.is(ROACWItemRegistry.MARS_VISOR.get())
                && chest.is(ROACWItemRegistry.MARS_ENGINE.get())
                && legs.is(ROACWItemRegistry.MARS_LEG_GUARDS.get())
                && boots.is(ROACWItemRegistry.MARS_BOOSTERS.get());
    }
}