package net.kamaarion.roacw.network.armorset_abilities;

import io.redspace.ironsspellbooks.api.util.CameraShakeData;
import io.redspace.ironsspellbooks.api.util.CameraShakeManager;
import net.kamaarion.roacw.Config;
import net.kamaarion.roacw.ROACW;
import net.kamaarion.roacw.entity.projectile.plague_bee.PlagueBeeEntity;
import net.kamaarion.roacw.network.ModMessages;
import net.kamaarion.roacw.registeries.ROACWEntityRegistry;
import net.kamaarion.roacw.registeries.ROACWItemRegistry;
import net.kamaarion.roacw.registeries.ROACWSoundRegistry;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingFallEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.server.ServerStoppedEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.server.ServerLifecycleHooks;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.Supplier;

@Mod.EventBusSubscriber(modid = ROACW.MODID)
public class PacketPlaguebringerJetBoostC2S {

    private static final int COOLDOWN_TICKS = 1800;
    private static final int DURATION_TICKS = 400;
    private static final int PLAGUE_BEE_INTERVAL = 100;
    private static final int MIN_BEES_PER_WAVE = 1;
    private static final int MAX_BEES_PER_WAVE = 3;
    private static final int RESYNC_DELAY_TICKS = 2;
    private static final double BEE_SPAWN_RADIUS = 2.0D;
    private static final double BEE_TARGET_SEARCH_RADIUS = 24.0D;

    private static final Map<UUID, Long> JETBOOST_EXPIRY = new HashMap<>();
    private static final Map<UUID, Long> PLAGUE_BEE_NEXT_SPAWN = new HashMap<>();
    private static final Map<UUID, Long> PENDING_RESYNC = new HashMap<>();
    private static final Set<UUID> PROTECTED_PLAYERS = new HashSet<>();
    private static final Set<UUID> ACTIVE_JETBOOST_PLAYERS = new HashSet<>();
    private static final Map<UUID, Long> LAST_ACTIVATION_TICK = new HashMap<>();
    private static final Map<UUID, Long> COOLDOWN_TRACKER = new HashMap<>();

    public PacketPlaguebringerJetBoostC2S() {
    }

    public PacketPlaguebringerJetBoostC2S(FriendlyByteBuf buf) {
    }

    public void toBytes(FriendlyByteBuf buf) {
    }

    public static boolean isJetBoostActive(UUID uuid) {
        return ACTIVE_JETBOOST_PLAYERS.contains(uuid);
    }

    public boolean handle(Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context context = supplier.get();

        context.enqueueWork(() -> {
            ServerPlayer player = context.getSender();

            ROACW.LOGGER.info("[AbilityDebug] Plaguebringer packet received, sender={}", player);

            if (player != null) {
                ROACW.LOGGER.info(
                        "[AbilityDebug] Plaguebringer checks: fullSet={} nextAvailableTick={} configEnabled={}",
                        isWearingFullPlaguebringerSet(player),
                        COOLDOWN_TRACKER.getOrDefault(player.getUUID(), 0L),
                        Config.ENABLE_PLAGUEBRINGER_JET_BOOST.get()
                );
            }

            if (player == null || !Config.ENABLE_PLAGUEBRINGER_JET_BOOST.get()) {
                return;
            }

            long currentServerTick = player.level().getGameTime();
            UUID playerUUID = player.getUUID();
            Long lastTick = LAST_ACTIVATION_TICK.get(playerUUID);

            if (lastTick != null && lastTick == currentServerTick) {
                return;
            }

            LAST_ACTIVATION_TICK.put(playerUUID, currentServerTick);

            if (!isWearingFullPlaguebringerSet(player)) {
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

            player.getAbilities().mayfly = true;
            player.onUpdateAbilities();

            player.hurtMarked = true;
            player.fallDistance = 0.0F;

            JETBOOST_EXPIRY.put(playerUUID, currentServerTick + DURATION_TICKS);
            PLAGUE_BEE_NEXT_SPAWN.put(playerUUID, currentServerTick + PLAGUE_BEE_INTERVAL);
            ACTIVE_JETBOOST_PLAYERS.add(playerUUID);
            PROTECTED_PLAYERS.remove(playerUUID);

            ModMessages.sendToTrackingAndSelf(
                    new PacketPlaguebringerJetBoostStateS2C(playerUUID, true),
                    player
            );

            CameraShakeManager.addCameraShake(
                    new CameraShakeData(
                            player.level(),
                            60,
                            player.position(),
                            5.0F
                    )
            );

            player.displayClientMessage(
                    Component.literal("§2Jet Boost Active! (30s Flight)"),
                    true
            );

            player.level().playSound(
                    null,
                    player.getX(),
                    player.getY(),
                    player.getZ(),
                    ROACWSoundRegistry.PLAGUEBRINGER_JETS_ACTIVATED.get(),
                    SoundSource.PLAYERS,
                    0.5F,
                    0.8F
            );
        });

        return true;
    }

    @SubscribeEvent
    public static void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) {
            return;
        }

        var server = ServerLifecycleHooks.getCurrentServer();

        if (server == null) {
            return;
        }

        long now = server.overworld().getGameTime();

        if (!Config.ENABLE_PLAGUEBRINGER_JET_BOOST.get() && !ACTIVE_JETBOOST_PLAYERS.isEmpty()) {
            for (UUID uuid : new HashSet<>(ACTIVE_JETBOOST_PLAYERS)) {
                ServerPlayer player = server.getPlayerList().getPlayer(uuid);

                if (player != null) {
                    revokeFlight(player);
                }

                ACTIVE_JETBOOST_PLAYERS.remove(uuid);
                JETBOOST_EXPIRY.remove(uuid);
                PLAGUE_BEE_NEXT_SPAWN.remove(uuid);
            }
        }

        PENDING_RESYNC.entrySet().removeIf(entry -> {
            if (now < entry.getValue()) {
                return false;
            }

            ServerPlayer player = server.getPlayerList().getPlayer(entry.getKey());

            if (player != null) {
                resyncJetBoost(player);
            }

            return true;
        });

        for (UUID uuid : new HashSet<>(ACTIVE_JETBOOST_PLAYERS)) {
            ServerPlayer player = server.getPlayerList().getPlayer(uuid);

            if (player == null) {
                PLAGUE_BEE_NEXT_SPAWN.remove(uuid);
                continue;
            }

            Long nextBeeSpawn = PLAGUE_BEE_NEXT_SPAWN.get(uuid);

            if (nextBeeSpawn == null) {
                PLAGUE_BEE_NEXT_SPAWN.put(uuid, now + PLAGUE_BEE_INTERVAL);
                continue;
            }

            if (now >= nextBeeSpawn) {
                spawnPlagueBeeWave(player);
                PLAGUE_BEE_NEXT_SPAWN.put(uuid, now + PLAGUE_BEE_INTERVAL);
            }
        }

        JETBOOST_EXPIRY.entrySet().removeIf(entry -> {
            if (now < entry.getValue()) {
                return false;
            }

            ServerPlayer player = server.getPlayerList().getPlayer(entry.getKey());

            if (player != null) {
                revokeFlight(player);
            }

            PLAGUE_BEE_NEXT_SPAWN.remove(entry.getKey());

            return true;
        });

        for (UUID uuid : new HashSet<>(PROTECTED_PLAYERS)) {
            ServerPlayer player = server.getPlayerList().getPlayer(uuid);

            if (player == null) {
                PROTECTED_PLAYERS.remove(uuid);
                continue;
            }

            if (!player.onGround()) {
                player.fallDistance = 0.0F;
            } else {
                PROTECTED_PLAYERS.remove(uuid);
            }
        }
    }

    private static void spawnPlagueBeeWave(ServerPlayer player) {
        if (!(player.level() instanceof ServerLevel serverLevel)) {
            return;
        }

        List<LivingEntity> targets = serverLevel.getEntitiesOfClass(
                LivingEntity.class,
                player.getBoundingBox().inflate(BEE_TARGET_SEARCH_RADIUS),
                entity -> entity != player && entity.isAlive() && entity instanceof Enemy
        );

        if (targets.isEmpty()) {
            return;
        }

        int beeCount = MIN_BEES_PER_WAVE
                + player.getRandom().nextInt(MAX_BEES_PER_WAVE - MIN_BEES_PER_WAVE + 1);

        for (int i = 0; i < beeCount; i++) {
            LivingEntity target = targets.get(player.getRandom().nextInt(targets.size()));

            double angle = player.getRandom().nextDouble() * Math.PI * 2.0D;
            double radius = 1.25D
                    + player.getRandom().nextDouble() * (BEE_SPAWN_RADIUS - 1.25D);

            double spawnX = player.getX() + Math.cos(angle) * radius;
            double spawnY = player.getY() + 0.5D + player.getRandom().nextDouble() * 1.5D;
            double spawnZ = player.getZ() + Math.sin(angle) * radius;

            PlagueBeeEntity bee = new PlagueBeeEntity(
                    ROACWEntityRegistry.PLAGUE_BEE.get(),
                    serverLevel,
                    player,
                    target
            );

            bee.setPos(spawnX, spawnY, spawnZ);

            Vec3 direction = target.getEyePosition()
                    .subtract(bee.position())
                    .normalize();

            bee.setDeltaMovement(direction.scale(0.8D));

            serverLevel.addFreshEntity(bee);
        }
    }

    private static void revokeFlight(ServerPlayer player) {
        if (!player.isCreative() && !player.isSpectator()) {
            player.getAbilities().mayfly = false;
            player.getAbilities().flying = false;
            player.onUpdateAbilities();

            if (!player.onGround()) {
                PROTECTED_PLAYERS.add(player.getUUID());
            }
        }

        UUID playerUUID = player.getUUID();

        ACTIVE_JETBOOST_PLAYERS.remove(playerUUID);
        PLAGUE_BEE_NEXT_SPAWN.remove(playerUUID);

        ModMessages.sendToTrackingAndSelf(
                new PacketPlaguebringerJetBoostStateS2C(playerUUID, false),
                player
        );
    }

    private static void resyncJetBoost(ServerPlayer player) {
        UUID playerUUID = player.getUUID();

        if (!ACTIVE_JETBOOST_PLAYERS.contains(playerUUID)) {
            return;
        }

        if (!Config.ENABLE_PLAGUEBRINGER_JET_BOOST.get() || !isWearingFullPlaguebringerSet(player)) {
            JETBOOST_EXPIRY.remove(playerUUID);
            revokeFlight(player);
            return;
        }

        player.getAbilities().mayfly = true;
        player.onUpdateAbilities();

        ModMessages.sendToTrackingAndSelf(
                new PacketPlaguebringerJetBoostStateS2C(playerUUID, true),
                player
        );
    }

    private static void endJetBoostSilently(ServerPlayer player) {
        UUID playerUUID = player.getUUID();
        boolean wasActive = ACTIVE_JETBOOST_PLAYERS.remove(playerUUID);

        JETBOOST_EXPIRY.remove(playerUUID);
        PLAGUE_BEE_NEXT_SPAWN.remove(playerUUID);
        PROTECTED_PLAYERS.remove(playerUUID);
        PENDING_RESYNC.remove(playerUUID);

        if (wasActive) {
            ModMessages.sendToTrackingAndSelf(
                    new PacketPlaguebringerJetBoostStateS2C(playerUUID, false),
                    player
            );
        }
    }

    @SubscribeEvent
    public static void onPlayerChangedDimension(PlayerEvent.PlayerChangedDimensionEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) {
            return;
        }

        if (!ACTIVE_JETBOOST_PLAYERS.contains(player.getUUID())) {
            return;
        }

        resyncJetBoost(player);
        PENDING_RESYNC.put(player.getUUID(), player.level().getGameTime() + RESYNC_DELAY_TICKS);
    }

    @SubscribeEvent
    public static void onPlayerRespawn(PlayerEvent.PlayerRespawnEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) {
            return;
        }

        if (!event.isEndConquered()) {
            endJetBoostSilently(player);
            return;
        }

        if (!ACTIVE_JETBOOST_PLAYERS.contains(player.getUUID())) {
            return;
        }

        resyncJetBoost(player);
        PENDING_RESYNC.put(player.getUUID(), player.level().getGameTime() + RESYNC_DELAY_TICKS);
    }

    @SubscribeEvent
    public static void onServerStopped(ServerStoppedEvent event) {
        COOLDOWN_TRACKER.clear();
    }

    @SubscribeEvent
    public static void onStartTracking(PlayerEvent.StartTracking event) {
        if (event.getTarget() instanceof ServerPlayer target
                && event.getEntity() instanceof ServerPlayer watcher) {
            ModMessages.sendToPlayer(
                    new PacketPlaguebringerJetBoostStateS2C(
                            target.getUUID(),
                            ACTIVE_JETBOOST_PLAYERS.contains(target.getUUID())
                    ),
                    watcher
            );
        }
    }

    @SubscribeEvent
    public static void onPlayerLoggedOut(PlayerEvent.PlayerLoggedOutEvent event) {
        UUID playerUUID = event.getEntity().getUUID();

        JETBOOST_EXPIRY.remove(playerUUID);
        PLAGUE_BEE_NEXT_SPAWN.remove(playerUUID);
        PENDING_RESYNC.remove(playerUUID);
        PROTECTED_PLAYERS.remove(playerUUID);
        ACTIVE_JETBOOST_PLAYERS.remove(playerUUID);
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

        return head.is(ROACWItemRegistry.PLAGUEBRINGER_VISOR.get())
                && chest.is(ROACWItemRegistry.PLAGUEBRINGER_FRAME.get())
                && legs.is(ROACWItemRegistry.PLAGUEBRINGER_LEGGINGS.get())
                && feet.is(ROACWItemRegistry.PLAGUEBRINGER_PISTONS.get());
    }
}