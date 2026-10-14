package net.kamaarion.roacw.network.armorset_abilities;

import net.kamaarion.roacw.Config;
import net.kamaarion.roacw.entity.armorset_summon.railgun_cannon.RailgunCannonEntity;
import net.kamaarion.roacw.entity.armorset_summon.unstable_energy_cannon.UnstableEnergyCannonEntity;
import net.kamaarion.roacw.entity.projectile.mars_missile.MarsMissileEntity;
import net.kamaarion.roacw.items.armor.marsarmorset.MarsArmorFlightHandler;
import net.kamaarion.roacw.registeries.ROACWSoundRegistry;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.server.ServerLifecycleHooks;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Supplier;

public class MarsCannonsSummonC2S {
    private static final int MISSILE_INTERVAL_TICKS = 140;

    private static final Map<UUID, MissileState> ACTIVE_PLAYERS = new HashMap<>();

    private final Vec3 corePosition;

    private static final double MISSILE_SIDE_OFFSET = 0.0D;
    private static final double MISSILE_UP_OFFSET = 1.27D;
    private static final double MISSILE_FORWARD_OFFSET = 0.34D;
    private static final double MISSILE_HALF_LENGTH = 0.94D;

    public MarsCannonsSummonC2S(Vec3 corePosition) {
        this.corePosition = corePosition;
    }

    public MarsCannonsSummonC2S(FriendlyByteBuf buffer) {
        this.corePosition = new Vec3(
                buffer.readDouble(),
                buffer.readDouble(),
                buffer.readDouble()
        );
    }

    public void toBytes(FriendlyByteBuf buffer) {
        buffer.writeDouble(corePosition.x);
        buffer.writeDouble(corePosition.y);
        buffer.writeDouble(corePosition.z);
    }

    public void handle(Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context context = supplier.get();

        context.enqueueWork(() -> {
            ServerPlayer player = context.getSender();

            if (player == null) {
                return;
            }

            if (!Config.ENABLE_MARS_CANNONS.get()) {
                discardCannons(player);
                ACTIVE_PLAYERS.remove(player.getUUID());
                return;
            }

            if (!MarsArmorFlightHandler.isWearingFullSet(player)) {
                return;
            }

            RailgunCannonEntity existingRailgun = findExistingRailgun(player);
            UnstableEnergyCannonEntity existingEnergyCannon = findExistingEnergyCannon(player);

            if (existingRailgun != null || existingEnergyCannon != null) {
                if (existingRailgun != null) {
                    existingRailgun.discard();
                }

                if (existingEnergyCannon != null) {
                    existingEnergyCannon.discard();
                }

                ACTIVE_PLAYERS.remove(player.getUUID());
                return;
            }

            RailgunCannonEntity railgun = new RailgunCannonEntity(player.level(), player);
            UnstableEnergyCannonEntity energyCannon = new UnstableEnergyCannonEntity(player.level(), player);

            player.level().addFreshEntity(railgun);
            player.level().addFreshEntity(energyCannon);

            MissileState state = new MissileState();
            state.ticksUntilNextMissile = MISSILE_INTERVAL_TICKS;
            ACTIVE_PLAYERS.put(player.getUUID(), state);

            player.level().playSound(
                    null,
                    player.blockPosition(),
                    ROACWSoundRegistry.DRAEDON_LAUGH.get(),
                    player.getSoundSource(),
                    1.0F,
                    1.0F
            );
        });

        context.setPacketHandled(true);
    }

    public static void tick() {
        if (ACTIVE_PLAYERS.isEmpty()) {
            return;
        }

        var server = ServerLifecycleHooks.getCurrentServer();

        if (server == null) {
            ACTIVE_PLAYERS.clear();
            return;
        }

        boolean cannonsEnabled = Config.ENABLE_MARS_CANNONS.get();

        ACTIVE_PLAYERS.entrySet().removeIf(entry -> {
            UUID uuid = entry.getKey();
            MissileState state = entry.getValue();
            ServerPlayer player = server.getPlayerList().getPlayer(uuid);

            if (player == null) {
                return true;
            }

            if (!cannonsEnabled) {
                discardCannons(player);
                return true;
            }

            if (!MarsArmorFlightHandler.isWearingFullSet(player)) {
                discardCannons(player);
                return true;
            }

            RailgunCannonEntity railgun = findExistingRailgun(player);
            UnstableEnergyCannonEntity energyCannon = findExistingEnergyCannon(player);

            if (railgun == null && energyCannon == null) {
                return true;
            }

            state.ticksUntilNextMissile--;

            if (state.ticksUntilNextMissile <= 0) {
                state.ticksUntilNextMissile = MISSILE_INTERVAL_TICKS;
                spawnMarsMissile(player, chestPosition(player));
            }

            return false;
        });
    }

    private static Vec3 chestPosition(ServerPlayer player) {
        float yawRad = player.yBodyRot * ((float) Math.PI / 180.0F);

        Vec3 bodyLocal = new Vec3(
                MISSILE_SIDE_OFFSET,
                MISSILE_UP_OFFSET,
                MISSILE_FORWARD_OFFSET
        );

        return player.position().add(bodyLocal.yRot(-yawRad));
    }

    private static void spawnMarsMissile(ServerPlayer player, Vec3 corePosition) {
        LivingEntity target = findTarget(player);

        if (target == null) {
            return;
        }

        float yawRad = player.yBodyRot * ((float) Math.PI / 180.0F);
        Vec3 launchDirection = new Vec3(0.0D, 0.0D, 1.0D).yRot(-yawRad);

        Vec3 spawnPosition = corePosition.add(
                launchDirection.scale(MISSILE_HALF_LENGTH)
        );

        MarsMissileEntity missile = new MarsMissileEntity(
                player.level(),
                player,
                target,
                20.0F,
                spawnPosition,
                launchDirection
        );

        player.level().addFreshEntity(missile);
    }

    private static LivingEntity findTarget(ServerPlayer player) {
        return player.level()
                .getEntitiesOfClass(
                        LivingEntity.class,
                        player.getBoundingBox().inflate(32.0D),
                        entity -> entity != player
                                && entity.isAlive()
                                && !entity.isSpectator()
                                && !player.isAlliedTo(entity)
                )
                .stream()
                .min(Comparator.comparingDouble(player::distanceToSqr))
                .orElse(null);
    }

    private static RailgunCannonEntity findExistingRailgun(ServerPlayer player) {
        return player.level()
                .getEntitiesOfClass(
                        RailgunCannonEntity.class,
                        player.getBoundingBox().inflate(8.0D),
                        entity -> entity.getOwner() == player
                )
                .stream()
                .findFirst()
                .orElse(null);
    }

    private static UnstableEnergyCannonEntity findExistingEnergyCannon(ServerPlayer player) {
        return player.level()
                .getEntitiesOfClass(
                        UnstableEnergyCannonEntity.class,
                        player.getBoundingBox().inflate(8.0D),
                        entity -> entity.getOwner() == player
                )
                .stream()
                .findFirst()
                .orElse(null);
    }

    private static void discardCannons(ServerPlayer player) {
        RailgunCannonEntity railgun = findExistingRailgun(player);
        UnstableEnergyCannonEntity energyCannon = findExistingEnergyCannon(player);

        if (railgun != null) {
            railgun.discard();
        }

        if (energyCannon != null) {
            energyCannon.discard();
        }
    }

    private static class MissileState {
        private int ticksUntilNextMissile;
    }
}