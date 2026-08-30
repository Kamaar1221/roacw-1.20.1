package net.kamaarion.roacw.network;

import io.redspace.ironsspellbooks.api.util.CameraShakeData;
import io.redspace.ironsspellbooks.api.util.CameraShakeManager;
import net.kamaarion.roacw.ROACW;
import net.kamaarion.roacw.capability.PlaguebringerJetBoostCapability;
import net.kamaarion.roacw.registeries.ROACWItemRegistry;
import net.kamaarion.roacw.registeries.ROACWParticleRegistry;
import net.kamaarion.roacw.registeries.ROACWSoundRegistry;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingFallEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.server.ServerLifecycleHooks;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.Supplier;

@Mod.EventBusSubscriber(modid = ROACW.MODID)
public class PacketPlaguebringerJetBoostC2S {

    private static final int COOLDOWN_TICKS = 1200;   // 60 seconds, TODO: tune
    private static final int DURATION_TICKS = 400;    // 30 seconds



    private static final Map<UUID, Long> JETBOOST_EXPIRY = new HashMap<>();

    private static final Set<UUID> PROTECTED_PLAYERS = new HashSet<>();


    public PacketPlaguebringerJetBoostC2S() {}
    public PacketPlaguebringerJetBoostC2S(FriendlyByteBuf buf) {}
    public void toBytes(FriendlyByteBuf buf) {}

    private static final Set<UUID> ACTIVE_JETBOOST_PLAYERS = new HashSet<>();

    private static final Map<UUID, Long> LAST_ACTIVATION_TICK = new HashMap<>();

    public static boolean isJetBoostActive(UUID uuid) {
        return ACTIVE_JETBOOST_PLAYERS.contains(uuid);
    }

    public boolean handle(Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context context = supplier.get();
        context.enqueueWork(() -> {
            ServerPlayer player = context.getSender();
            if (player == null) return;

            long currentServerTick = player.level().getGameTime();
            Long lastTick = LAST_ACTIVATION_TICK.get(player.getUUID());
            if (lastTick != null) {
                if (lastTick == currentServerTick) {
                    return;
                }
            }
            LAST_ACTIVATION_TICK.put(player.getUUID(), currentServerTick);

            if (!isWearingFullPlaguebringerSet(player)) {
                return;
            }

            player.getCapability(PlaguebringerJetBoostCapability.INSTANCE).ifPresent(data -> {
                if (currentServerTick < data.getNextAvailableTick()) {
                    long remainingTicks = data.getNextAvailableTick() - currentServerTick;
                    long remainingSeconds = (remainingTicks + 19) / 20;
                    player.displayClientMessage(Component.literal("§cAbility on cooldown! Wait " + remainingSeconds + "s"), true);
                    return;
                }

                data.setNextAvailableTick(currentServerTick + COOLDOWN_TICKS);

                player.getAbilities().mayfly = true;
                player.onUpdateAbilities();

                player.hurtMarked = true;
                player.fallDistance = 0.0F;


                JETBOOST_EXPIRY.put(player.getUUID(), currentServerTick + DURATION_TICKS);
                ACTIVE_JETBOOST_PLAYERS.add(player.getUUID());
                PROTECTED_PLAYERS.remove(player.getUUID());

                ModMessages.sendToTrackingAndSelf(
                        new PacketPlaguebringerJetBoostStateS2C(player.getUUID(), true), player
                );

                // Radius-based, falls off with distance - centering on the
                // player themselves gives full intensity to the activator,
                // with a faint tremor for anyone standing close by. Duration
                // is in ticks; ISS's own manager handles the network sync to
                // all clients internally, no packet work needed on our end.
                CameraShakeManager.addCameraShake(
                        new CameraShakeData(player.level(), 60, player.position(), 5.0F)
                );

                player.displayClientMessage(Component.literal("§2Jet Boost Active! (30s Flight)"), true);

                player.level().playSound(null, player.getX(), player.getY(), player.getZ(),
                        ROACWSoundRegistry.PLAGUEBRINGER_JETS_ACTIVATED.get(), SoundSource.PLAYERS, 1.0F, 0.8F);
                // TODO: swap for a plague/booster-specific sound + particle
                // burst, same spot Earthen Paladin's pulse plays its Blastwave ring.
            });
        });
        return true;
    }

    // Runs every server tick; expires flight grants whose time has come.
    // Same shape as PacketArmorPulseC2S.onServerTick - real fix, not a
    // deferred TickTask.
    @SubscribeEvent
    public static void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;

        var server = ServerLifecycleHooks.getCurrentServer();
        if (server == null) return;

        long now = server.overworld().getGameTime();

        // Spawn booster particles for every player currently mid-flight,
        // before the expiry check below potentially removes them this tick.


        JETBOOST_EXPIRY.entrySet().removeIf(entry -> {
            if (now < entry.getValue()) return false;
            ServerPlayer p = server.getPlayerList().getPlayer(entry.getKey());
            if (p != null) revokeFlight(p);
            return true;
        });

        for (UUID uuid : new HashSet<>(PROTECTED_PLAYERS)) {
            ServerPlayer p = server.getPlayerList().getPlayer(uuid);
            if (p == null) {
                PROTECTED_PLAYERS.remove(uuid);
                continue;
            }
            if (!p.onGround()) {
                Vec3 motion = p.getDeltaMovement();

                p.fallDistance = 0.0F;
            } else {
                PROTECTED_PLAYERS.remove(uuid);
            }
        }
    }

    /**
     * Spawns trailing exhaust particles at both booster nozzle positions,
     * transformed into world space from the player's current look yaw.
     * Called every server tick while the player has an active flight grant
     * and isn't grounded.
     */

    private static void revokeFlight(ServerPlayer player) {
        if (!player.isCreative() && !player.isSpectator()) {
            player.getAbilities().mayfly = false;
            player.getAbilities().flying = false;
            player.onUpdateAbilities();

            if (!player.onGround()) {
                PROTECTED_PLAYERS.add(player.getUUID());
            }
        }

        // Always clear our own ability state/visuals regardless of game mode
        // - isJetBoostActive() (and therefore the tint/FOV/particles) is
        // gated on ACTIVE_JETBOOST_PLAYERS, which has nothing to do with
        // whether we're actually allowed to touch mayfly/flying for this
        // player. Leaving this inside the creative guard above meant
        // creative players never got the "deactivate" state at all.
        ACTIVE_JETBOOST_PLAYERS.remove(player.getUUID());

        ModMessages.sendToTrackingAndSelf(
                new PacketPlaguebringerJetBoostStateS2C(player.getUUID(), false), player
        );
    }

    @SubscribeEvent
    public static void onPlayerLoggedOut(PlayerEvent.PlayerLoggedOutEvent event) {
        UUID playerUUID = event.getEntity().getUUID();
        JETBOOST_EXPIRY.remove(playerUUID);
        PROTECTED_PLAYERS.remove(playerUUID);
        LAST_ACTIVATION_TICK.remove(playerUUID);
    }

    @SubscribeEvent
    public static void onPlayerFall(LivingFallEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            UUID playerUUID = player.getUUID();
            if (PROTECTED_PLAYERS.contains(playerUUID)) {
                event.setDistance(0.0F);
                event.setCanceled(true);
                PROTECTED_PLAYERS.remove(playerUUID);
            }
        }
    }

    private static boolean isWearingFullPlaguebringerSet(ServerPlayer player) {
        ItemStack head = player.getItemBySlot(EquipmentSlot.HEAD);
        ItemStack chest = player.getItemBySlot(EquipmentSlot.CHEST);
        ItemStack legs = player.getItemBySlot(EquipmentSlot.LEGS);
        ItemStack feet = player.getItemBySlot(EquipmentSlot.FEET);

        return head.is(ROACWItemRegistry.PLAGUEBRINGER_VISOR.get()) &&
                chest.is(ROACWItemRegistry.PLAGUEBRINGER_FRAME.get()) &&
                legs.is(ROACWItemRegistry.PLAGUEBRINGER_LEGGINGS.get()) &&
                feet.is(ROACWItemRegistry.PLAGUEBRINGER_PISTONS.get());
    }
}