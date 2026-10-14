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

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Mod.EventBusSubscriber(modid = "roacw", bus = Mod.EventBusSubscriber.Bus.FORGE)
public class MarsArmorFlightHandler {

    private static final Set<UUID> MARS_FLIGHT_GRANTED = new HashSet<>();
    private static final Set<UUID> PROTECTED_PLAYERS = new HashSet<>();
    private static final Map<UUID, Boolean> LAST_SENT_BOOSTER_STATE = new ConcurrentHashMap<>();

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) {
            return;
        }

        Player player = event.player;
        UUID playerUUID = player.getUUID();

        if (Config.ENABLE_MARS_FLIGHT.get() && isWearingFullSet(player)) {
            if (!player.getAbilities().mayfly) {
                player.getAbilities().mayfly = true;
                player.onUpdateAbilities();
            }

            MARS_FLIGHT_GRANTED.add(playerUUID);
            PROTECTED_PLAYERS.remove(playerUUID);
        } else {
            if (MARS_FLIGHT_GRANTED.remove(playerUUID)
                    && !player.isCreative()
                    && !player.isSpectator()) {
                player.getAbilities().mayfly = false;
                player.getAbilities().flying = false;
                player.onUpdateAbilities();

                if (!player.onGround()) {
                    PROTECTED_PLAYERS.add(playerUUID);
                }
            }
        }

        if (PROTECTED_PLAYERS.contains(playerUUID)) {
            if (!player.onGround()) {
                Vec3 motion = player.getDeltaMovement();

                if (motion.y < -0.15) {
                    player.setDeltaMovement(motion.x, -0.15, motion.z);
                    player.hurtMarked = true;
                }

                player.fallDistance = 0.0F;
            } else {
                PROTECTED_PLAYERS.remove(playerUUID);
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

    @SubscribeEvent
    public static void onStartTracking(PlayerEvent.StartTracking event) {
        if (event.getTarget() instanceof ServerPlayer target
                && event.getEntity() instanceof ServerPlayer watcher
                && LAST_SENT_BOOSTER_STATE.getOrDefault(target.getUUID(), false)) {
            ModMessages.sendToPlayer(
                    new MarsArmorBoosterStateS2C(target.getUUID(), true),
                    watcher
            );
        }
    }

    @SubscribeEvent
    public static void onPlayerLoggedOut(PlayerEvent.PlayerLoggedOutEvent event) {
        UUID playerUUID = event.getEntity().getUUID();

        MARS_FLIGHT_GRANTED.remove(playerUUID);
        PROTECTED_PLAYERS.remove(playerUUID);
        LAST_SENT_BOOSTER_STATE.remove(playerUUID);
    }

    @SubscribeEvent
    public static void onPlayerFall(LivingFallEvent event) {
        if (event.getEntity() instanceof Player player) {
            UUID playerUUID = player.getUUID();

            if (PROTECTED_PLAYERS.contains(playerUUID)) {
                event.setDistance(0.0F);
                event.setCanceled(true);
                PROTECTED_PLAYERS.remove(playerUUID);
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