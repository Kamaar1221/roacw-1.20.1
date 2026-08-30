package net.kamaarion.roacw.network;

import net.kamaarion.roacw.Config;
import net.kamaarion.roacw.entity.projectile.god_killer_dart.GodKillerDartEntity;
import net.kamaarion.roacw.registeries.ROACWEntityRegistry;
import net.kamaarion.roacw.registeries.ROACWItemRegistry;
import net.kamaarion.roacw.registeries.ROACWSoundRegistry;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ambient.AmbientCreature;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.animal.WaterAnimal;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.NetworkEvent;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Supplier;

/**
 * C2S packet for the Auric Tesla armor set's dash ability: a forward burst
 * with an AoE damage + lifesteal zone, gated behind a per-player cooldown
 * and a config kill-switch.
 */
public class PacketAuricTeslaDashC2S {

    private static final Map<UUID, Long> AURIC_COOLDOWN_TRACKER = new HashMap<>();

    private static final int DART_COUNT = 8;
    private static final double DART_SEARCH_RADIUS = 24.0;
    private static final double DART_LAUNCH_SPEED = 1.4;
    private static final double DART_RING_RADIUS = 0.5;
    private static final double DART_RING_BEHIND_DISTANCE = 2.0;

    // Required blank constructor for default registry pipelines
    public PacketAuricTeslaDashC2S() {
    }

    public PacketAuricTeslaDashC2S(FriendlyByteBuf buf) {
        // No payload needed - this packet carries no data.
    }

    public void toBytes(FriendlyByteBuf buf) {
        // No payload needed - this packet carries no data.
    }

    public boolean handle(Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context context = supplier.get();
        context.enqueueWork(() -> {
            ServerPlayer player = context.getSender();
            if (player == null) return;

            // CRITICAL SERVER-SIDE CONFIG KILL-SWITCH FIX:
            // If the server owner turns it off in roacw-common.toml, stop processing immediately!
            if (!Config.ENABLE_AURIC_TESLA_DASH.get()) {
                return;
            }

            if (!isWearingFullAuricTesla(player)) {
                return;
            }

            long currentServerTick = player.server.getTickCount();
            long nextAvailableTick = AURIC_COOLDOWN_TRACKER.getOrDefault(player.getUUID(), 0L);

            if (currentServerTick < nextAvailableTick) {
                long remainingTicks = nextAvailableTick - currentServerTick;
                long remainingSeconds = (remainingTicks + 19) / 20;
                player.displayClientMessage(Component.literal("§cAbility on cooldown! Wait " + remainingSeconds + "s"), true);
                return;
            }

            AURIC_COOLDOWN_TRACKER.put(player.getUUID(), currentServerTick + 60);

            Vec3 lookDirection = player.getLookAngle();
            double auricDashSpeed = 4.0;
            double auricDashHeight = 0.1;

            Vec3 motion = new Vec3(lookDirection.x, auricDashHeight, lookDirection.z).normalize().scale(auricDashSpeed);

            player.setDeltaMovement(motion);
            player.hurtMarked = true;

            player.level().playSound(null, player.getX(), player.getY(), player.getZ(),
                    ROACWSoundRegistry.AURIC_DASH_CHARGE.get(), SoundSource.PLAYERS, 1.0F, 1.2F);

            if (player.level() instanceof ServerLevel serverLevel) {
                serverLevel.sendParticles(ParticleTypes.CLOUD, player.getX(), player.getY() + 0.5, player.getZ(), 25, 0.3, 0.3, 0.3, 0.15);
            }

            spawnGodKillerDartBurst(player);

            // === AURIC TESLA DAMAGE & LIFESTEAL ZONE ===
            AABB damageZone = player.getBoundingBox().expandTowards(motion.scale(4.0));
            List<LivingEntity> targets = player.level().getEntitiesOfClass(
                    LivingEntity.class, damageZone, entity -> entity != player
            );

            float totalHealAmount = 0.0F;

            for (LivingEntity target : targets) {
                float damageGiven = 30.0F;
                if (target.hurt(player.damageSources().indirectMagic(player, player), damageGiven)) {
                    // Adds 10.0 base flat heal + 6.0 lifesteal per enemy hit
                    totalHealAmount += 10.0F + (damageGiven * 0.20F);
                }

                target.knockback(1.2, -motion.x, -motion.z);
                target.hurtMarked = true;

                if (player.level() instanceof ServerLevel serverLevel) {
                    serverLevel.sendParticles(ParticleTypes.EXPLOSION, target.getX(), target.getY() + 1.0, target.getZ(), 3, 0.1, 0.1, 0.1, 0.0);
                    serverLevel.sendParticles(ParticleTypes.ELECTRIC_SPARK, target.getX(), target.getY() + 1.0, target.getZ(), 15, 0.3, 0.3, 0.3, 0.2);
                }

                player.level().playSound(null, target.getX(), target.getY(), target.getZ(),
                        SoundEvents.LIGHTNING_BOLT_THUNDER, SoundSource.PLAYERS, 0.6F, 1.6F);
            }

            if (totalHealAmount > 0.0F) {
                player.heal(totalHealAmount);
                player.level().playSound(null, player.getX(), player.getY(), player.getZ(),
                        SoundEvents.WITCH_DRINK, SoundSource.PLAYERS, 0.5F, 1.4F);

                if (player.level() instanceof ServerLevel serverLevel) {
                    serverLevel.sendParticles(ParticleTypes.HEART, player.getX(), player.getY() + 1.0, player.getZ(), 5, 0.4, 0.4, 0.4, 0.0);
                }
            }
        });
        return true;
    }

    /**
     * Fires a burst of 8 God Killer Darts arranged in a ring behind the
     * player (Calamity-style summon-swarm formation), each launching with a
     * slight outward radial burst before homing takes over. Targets are
     * drawn from a radius search around the player (not the directional
     * dash AABB used for the lightning zone below), so the burst can catch
     * enemies to the side or behind — then round-robins them across darts
     * so a lone target doesn't eat all 8, but a single enemy still gets hit
     * by every dart if it's the only one nearby.
     */
    private static void spawnGodKillerDartBurst(ServerPlayer player) {

        AABB searchArea = player.getBoundingBox().inflate(DART_SEARCH_RADIUS);

        List<LivingEntity> nearbyEnemies = player.level().getEntitiesOfClass(
                LivingEntity.class, searchArea,
                entity -> entity != player && entity.isAlive() && !entity.isAlliedTo(player) && isViableHostileTarget(entity)
        );

        // Horizontal facing direction only.
// Player pitch is intentionally ignored so the dart ring stays upright.
        // Use only the player's horizontal facing direction.
// This prevents the ring from tilting when the player looks up/down.
        Vec3 forward = new Vec3(
                player.getLookAngle().x,
                0.0D,
                player.getLookAngle().z
        );

        if (forward.lengthSqr() < 1.0E-6D) {
            forward = new Vec3(0, 0, 1);
        } else {
            forward = forward.normalize();
        }

// Horizontal right vector.
        Vec3 right = new Vec3(
                -forward.z,
                0.0D,
                forward.x
        ).normalize();

// World vertical.
        Vec3 up = new Vec3(0, 1, 0);

// Put the center of the ring around the player's upper torso.
        Vec3 ringCenter = new Vec3(
                player.getX(),
                player.getY() + 2.0D,
                player.getZ()
        ).subtract(
                forward.scale(DART_RING_BEHIND_DISTANCE)
        );

        for (int i = 0; i < DART_COUNT; i++) {

            double angle = (2.0 * Math.PI / DART_COUNT) * i;

            Vec3 radialDir = right.scale(Math.cos(angle))
                    .add(up.scale(Math.sin(angle)));

            Vec3 spawnPos = ringCenter.add(
                    radialDir.scale(DART_RING_RADIUS)
            );

            GodKillerDartEntity dart = new GodKillerDartEntity(
                    ROACWEntityRegistry.GOD_KILLER_DART.get(),
                    player,
                    player.level()
            );

            dart.setPos(
                    spawnPos.x,
                    spawnPos.y,
                    spawnPos.z
            );

            if (!nearbyEnemies.isEmpty()) {
                dart.setTarget(
                        nearbyEnemies.get(i % nearbyEnemies.size())
                );
            }

            // Blend radial burst with backward bias for the launch vector.
            // (No vertical clamp needed here — that was a workaround for
            // when block collisions used to instantly discard the dart;
            // now that terrain no longer stops a dart mid-homing, a bottom-
            // ring dart briefly clipping ground on launch is harmless, and
            // clamping away its downward component was flattening it into
            // a plain backward shot instead of a real radial burst.)
            Vec3 launchDirection = radialDir.scale(0.4)
                    .add(forward.scale(-0.6))
                    .normalize();

            dart.setDeltaMovement(
                    launchDirection.scale(DART_LAUNCH_SPEED)
            );

            player.level().addFreshEntity(dart);
        }
    }

    /**
     * Excludes passive/non-combat mobs (animals, villagers, players) from
     * being a valid initial dart target. Without this, dashing near a cow
     * or villager while the actual intended enemy is out of the search
     * radius could hand a dart a "valid" target it has no business chasing.
     *
     * NOTE: this also excludes Player, on the assumption this ability
     * isn't meant to be PvP-capable — flip that if it should be. Only
     * covers vanilla's passive-mob base classes; custom passive ROACW/ISS
     * entities that don't extend these may need their own exclusion.
     */
    private static boolean isViableHostileTarget(LivingEntity entity) {
        return !(entity instanceof Animal)
                && !(entity instanceof WaterAnimal)
                && !(entity instanceof AmbientCreature)
                && !(entity instanceof Villager)
                && !(entity instanceof Player);
    }

    private static boolean isWearingFullAuricTesla(ServerPlayer player) {
        ItemStack head = player.getItemBySlot(EquipmentSlot.HEAD);
        ItemStack chest = player.getItemBySlot(EquipmentSlot.CHEST);
        ItemStack legs = player.getItemBySlot(EquipmentSlot.LEGS);
        ItemStack feet = player.getItemBySlot(EquipmentSlot.FEET);

        return head.is(ROACWItemRegistry.AURIC_TESLA_ROYAL_HELM.get()) &&
                chest.is(ROACWItemRegistry.AURIC_TESLA_CUIRASS.get()) &&
                legs.is(ROACWItemRegistry.AURIC_TESLA_CUISSES.get()) &&
                feet.is(ROACWItemRegistry.AURIC_TESLA_BOOTS.get());
    }
}