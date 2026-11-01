package net.kamaarion.roacw.network.armorset_abilities;

import net.kamaarion.roacw.Config;
import net.kamaarion.roacw.ROACW;
import net.kamaarion.roacw.items.armor.shrimparmorset.ShrimpArmorSetBonusHandler;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraftforge.common.ForgeMod;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.server.ServerStoppedEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.server.ServerLifecycleHooks;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Supplier;

@Mod.EventBusSubscriber(modid = ROACW.MODID)
public class PacketShrimpTidalSurgeC2S {

    private static final int COOLDOWN_TICKS = 1800;
    private static final int DURATION_TICKS = 600;
    private static final int PARTICLE_INTERVAL_TICKS = 2;

    private static final double MOVEMENT_SPEED_BONUS = 0.4D;
    private static final double SWIM_SPEED_BONUS = 0.75D;
    private static final double ATTACK_DAMAGE_BONUS = 0.3D;

    private static final UUID SURGE_MOVEMENT_ID = UUID.fromString("a3f1c7e2-4b8d-4e60-9a15-52d7c0b8e101");
    private static final UUID SURGE_SWIM_ID = UUID.fromString("b5e29d84-7c13-4a07-8f3b-6a1e4d9c2202");
    private static final UUID SURGE_DAMAGE_ID = UUID.fromString("d8c40f6a-1e95-4b72-a3d0-9f58b7e1c303");

    private static final Map<UUID, Long> SURGE_EXPIRY = new HashMap<>();
    private static final Map<UUID, Long> LAST_ACTIVATION_TICK = new HashMap<>();
    private static final Map<UUID, Long> COOLDOWN_TRACKER = new HashMap<>();

    public PacketShrimpTidalSurgeC2S() {
    }

    public PacketShrimpTidalSurgeC2S(FriendlyByteBuf buf) {
    }

    public void toBytes(FriendlyByteBuf buf) {
    }

    public static boolean isSurgeActive(UUID uuid) {
        return SURGE_EXPIRY.containsKey(uuid);
    }

    public boolean handle(Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context context = supplier.get();

        context.enqueueWork(() -> {
            ServerPlayer player = context.getSender();

            if (player == null || !Config.ENABLE_SHRIMP_TIDAL_SURGE.get()) {
                return;
            }

            long currentServerTick = player.level().getGameTime();
            UUID playerUUID = player.getUUID();
            Long lastTick = LAST_ACTIVATION_TICK.get(playerUUID);

            if (lastTick != null && lastTick == currentServerTick) {
                return;
            }

            LAST_ACTIVATION_TICK.put(playerUUID, currentServerTick);

            if (!ShrimpArmorSetBonusHandler.hasFullSet(player)) {
                return;
            }

            long nextAvailableTick = COOLDOWN_TRACKER.getOrDefault(playerUUID, 0L);

            if (currentServerTick < nextAvailableTick) {
                long remainingTicks = nextAvailableTick - currentServerTick;
                long remainingSeconds = (remainingTicks + 19) / 20;

                player.displayClientMessage(
                        Component.literal("§cAbility on cooldown! Wait " + remainingSeconds + "s"),
                        true
                );
                return;
            }

            COOLDOWN_TRACKER.put(playerUUID, currentServerTick + COOLDOWN_TICKS);
            SURGE_EXPIRY.put(playerUUID, currentServerTick + DURATION_TICKS);

            applyModifiers(player);

            player.level().playSound(
                    null,
                    player.getX(),
                    player.getY(),
                    player.getZ(),
                    SoundEvents.DOLPHIN_SPLASH,
                    SoundSource.PLAYERS,
                    0.8F,
                    0.9F
            );

            player.displayClientMessage(
                    Component.literal("§bTidal Surge Active! (" + (DURATION_TICKS / 20) + "s)"),
                    true
            );
        });

        return true;
    }

    @SubscribeEvent
    public static void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase != TickEvent.Phase.END || SURGE_EXPIRY.isEmpty()) {
            return;
        }

        var server = ServerLifecycleHooks.getCurrentServer();

        if (server == null) {
            return;
        }

        long now = server.overworld().getGameTime();

        for (Map.Entry<UUID, Long> entry : new HashMap<>(SURGE_EXPIRY).entrySet()) {
            UUID uuid = entry.getKey();
            ServerPlayer player = server.getPlayerList().getPlayer(uuid);

            if (player == null) {
                SURGE_EXPIRY.remove(uuid);
                continue;
            }

            boolean expired = now >= entry.getValue();

            if (expired
                    || !Config.ENABLE_SHRIMP_TIDAL_SURGE.get()
                    || !ShrimpArmorSetBonusHandler.hasFullSet(player)) {
                endSurge(player, expired);
                continue;
            }

            applyModifiers(player);

            if (now % PARTICLE_INTERVAL_TICKS == 0) {
                spawnParticles(player);
            }
        }
    }

    private static void spawnParticles(ServerPlayer player) {
        if (!(player.level() instanceof ServerLevel level)) {
            return;
        }

        level.sendParticles(
                ParticleTypes.SPLASH,
                player.getX(),
                player.getY() + 1.0D,
                player.getZ(),
                6,
                0.4D,
                0.6D,
                0.4D,
                0.05D
        );

        level.sendParticles(
                ParticleTypes.BUBBLE,
                player.getX(),
                player.getY() + 1.0D,
                player.getZ(),
                3,
                0.4D,
                0.6D,
                0.4D,
                0.02D
        );
    }

    private static void applyModifiers(ServerPlayer player) {
        addModifier(player.getAttribute(Attributes.MOVEMENT_SPEED), SURGE_MOVEMENT_ID, "Shrimp tidal surge movement speed", MOVEMENT_SPEED_BONUS);
        addModifier(player.getAttribute(ForgeMod.SWIM_SPEED.get()), SURGE_SWIM_ID, "Shrimp tidal surge swim speed", SWIM_SPEED_BONUS);
        addModifier(player.getAttribute(Attributes.ATTACK_DAMAGE), SURGE_DAMAGE_ID, "Shrimp tidal surge attack damage", ATTACK_DAMAGE_BONUS);
    }

    private static void removeModifiers(ServerPlayer player) {
        removeModifier(player.getAttribute(Attributes.MOVEMENT_SPEED), SURGE_MOVEMENT_ID);
        removeModifier(player.getAttribute(ForgeMod.SWIM_SPEED.get()), SURGE_SWIM_ID);
        removeModifier(player.getAttribute(Attributes.ATTACK_DAMAGE), SURGE_DAMAGE_ID);
    }

    private static void addModifier(AttributeInstance instance, UUID id, String name, double amount) {
        if (instance != null && instance.getModifier(id) == null) {
            instance.addTransientModifier(
                    new AttributeModifier(id, name, amount, AttributeModifier.Operation.MULTIPLY_TOTAL)
            );
        }
    }

    private static void removeModifier(AttributeInstance instance, UUID id) {
        if (instance != null && instance.getModifier(id) != null) {
            instance.removeModifier(id);
        }
    }

    private static void endSurge(ServerPlayer player, boolean expired) {
        SURGE_EXPIRY.remove(player.getUUID());
        removeModifiers(player);

        if (expired) {
            player.displayClientMessage(Component.literal("§bTidal Surge faded"), true);
        }
    }

    private static void endSurgeSilently(ServerPlayer player) {
        SURGE_EXPIRY.remove(player.getUUID());
        removeModifiers(player);
    }

    @SubscribeEvent
    public static void onPlayerRespawn(PlayerEvent.PlayerRespawnEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) {
            return;
        }

        if (!event.isEndConquered()) {
            endSurgeSilently(player);
        }
    }

    @SubscribeEvent
    public static void onPlayerLoggedOut(PlayerEvent.PlayerLoggedOutEvent event) {
        UUID playerUUID = event.getEntity().getUUID();

        SURGE_EXPIRY.remove(playerUUID);
        LAST_ACTIVATION_TICK.remove(playerUUID);
    }

    @SubscribeEvent
    public static void onServerStopped(ServerStoppedEvent event) {
        COOLDOWN_TRACKER.clear();
        SURGE_EXPIRY.clear();
        LAST_ACTIVATION_TICK.clear();
    }
}