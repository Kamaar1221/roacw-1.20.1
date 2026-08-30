package net.kamaarion.roacw.particle;

import net.kamaarion.roacw.registeries.ROACWParticleRegistry;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;

public final class PlagueParticleHelper {

    private PlagueParticleHelper() {}

    /**
     * Spawns a plague cloud at a position.
     */
    public static void spawnCloud(Level level,
                                  double centerX,
                                  double centerY,
                                  double centerZ,
                                  float radius,
                                  double height,
                                  int cloudCount,
                                  int greenCount,
                                  int redCount) {

        if (!level.isClientSide)
            return;

        RandomSource random = level.getRandom();

        // ===========================
        // Plague Cloud
        // ===========================

        for (int i = 0; i < cloudCount; i++) {

            double angle = random.nextDouble() * Math.PI * 2D;
            double r = random.nextDouble() * radius;

            double x = centerX + Math.cos(angle) * r;
            double z = centerZ + Math.sin(angle) * r;
            double y = centerY + random.nextDouble() * height;
            level.addParticle(
                    ROACWParticleRegistry.PLAGUE_CLOUD.get(),
                    true,
                    x,
                    y,
                    z,
                    0.015,
                    0.01,
                    0.015
            );
        }

        // ===========================
        // Green Nanobots
        // ===========================

        for (int i = 0; i < greenCount; i++) {

            double angle = random.nextDouble() * Math.PI * 2D;
            double r = random.nextDouble() * radius;

            double x = centerX + Math.cos(angle) * r;
            double z = centerZ + Math.sin(angle) * r;
            double y = centerY + random.nextDouble() * height;
            level.addParticle(
                    ROACWParticleRegistry.PLAGUE_NANO_GREEN.get(),
                    true,
                    x,
                    y,
                    z,
                    (random.nextDouble() - 0.5D) * 0.02D,
                    (random.nextDouble() - 0.5D) * 0.02D,
                    (random.nextDouble() - 0.5D) * 0.02D
            );
        }

        // ===========================
        // Red Nanobots
        // ===========================

        for (int i = 0; i < redCount; i++) {

            double angle = random.nextDouble() * Math.PI * 2D;
            double r = random.nextDouble() * radius;

            double x = centerX + Math.cos(angle) * r;
            double z = centerZ + Math.sin(angle) * r;
            double y = centerY + random.nextDouble() * height;
            level.addParticle(
                    ROACWParticleRegistry.PLAGUE_NANO_RED.get(),
                    true,
                    x,
                    y,
                    z,
                    (random.nextDouble() - 0.5D) * 0.02D,
                    (random.nextDouble() - 0.5D) * 0.02D,
                    (random.nextDouble() - 0.5D) * 0.02D
            );
        }
    }

    /**
     * Server-broadcast, one-time version of spawnCloud - use this for impact
     * bursts instead of spawning a PlagueCloudEntity with setBurstOnly(true).
     * That approach has a sync bug: burstOnly is a plain unsynced Java field
     * (PlagueCloudEntity.defineSynchedData() is empty), so the client's own
     * copy of the entity never learns burstOnly was set true, and instead
     * runs the "normal lingering cloud" branch every tick for the entity's
     * full 5-second lifetime - spawning a full-size cloud repeatedly instead
     * of one small one-off puff. This sidesteps that entirely by not
     * spawning any entity at all for a one-time burst.
     */
    public static void spawnCloudBurstServer(ServerLevel serverLevel,
                                             double centerX,
                                             double centerY,
                                             double centerZ,
                                             float radius,
                                             double height,
                                             int cloudCount,
                                             int greenCount,
                                             int redCount) {

        RandomSource random = serverLevel.getRandom();

        for (int i = 0; i < cloudCount; i++) {
            double angle = random.nextDouble() * Math.PI * 2D;
            double r = random.nextDouble() * radius;
            double x = centerX + Math.cos(angle) * r;
            double z = centerZ + Math.sin(angle) * r;
            double y = centerY + random.nextDouble() * height;

            // Loop through all players in the server level
            for (ServerPlayer player : serverLevel.players()) {
                serverLevel.sendParticles(
                        player, // Target player required for overrideLimiter in 1.20.1
                        ROACWParticleRegistry.PLAGUE_CLOUD.get(),
                        true,   // overrideLimiter works here!
                        x, y, z,
                        1,
                        0.0D, 0.0D, 0.0D,
                        0.01D
                );
            }
        }


        for (int i = 0; i < greenCount; i++) {
            double angle = random.nextDouble() * Math.PI * 2D;
            double r = random.nextDouble() * radius;
            double x = centerX + Math.cos(angle) * r;
            double z = centerZ + Math.sin(angle) * r;
            double y = centerY + random.nextDouble() * height;

            for (ServerPlayer player : serverLevel.players()) {
                serverLevel.sendParticles(
                        player,
                        ROACWParticleRegistry.PLAGUE_NANO_GREEN.get(),
                        true,
                        x, y, z,
                        1,
                        0.0D, 0.0D, 0.0D,
                        0.02D
                );
            }
        }

        for (int i = 0; i < redCount; i++) {
            double angle = random.nextDouble() * Math.PI * 2D;
            double r = random.nextDouble() * radius;
            double x = centerX + Math.cos(angle) * r;
            double z = centerZ + Math.sin(angle) * r;
            double y = centerY + random.nextDouble() * height;

            for (ServerPlayer player : serverLevel.players()) {
                serverLevel.sendParticles(
                        player,
                        ROACWParticleRegistry.PLAGUE_NANO_RED.get(),
                        true,
                        x, y, z,
                        1,
                        0.0D, 0.0D, 0.0D,
                        0.02D
                );
            }
        }


    }

    /**
     * Server-side version of spawnAroundEntity - broadcasts particles to all
     * nearby observing clients via ServerLevel.sendParticles(), rather than
     * relying on Level.addParticle() from inside a client-only branch.
     *
     * This exists because applyEffectTick() only reliably runs client-side
     * for the entity's OWNING player - vanilla doesn't sync full
     * MobEffectInstance data to other clients for arbitrary entities (only a
     * packed "effect color" used for the default swirl particles), so
     * addParticle()-based logic inside applyEffectTick silently does nothing
     * for any entity besides the local player. Call this from the server
     * side of applyEffectTick instead.
     */
    public static void spawnAroundEntityServer(ServerLevel serverLevel,
                                               LivingEntity entity,
                                               int greenCount,
                                               int redCount) {

        RandomSource random = serverLevel.getRandom();

        for (int i = 0; i < greenCount; i++) {

            double x = entity.getX() + (random.nextDouble() - 0.5D) * entity.getBbWidth();
            double y = entity.getY() + random.nextDouble() * entity.getBbHeight();
            double z = entity.getZ() + (random.nextDouble() - 0.5D) * entity.getBbWidth();

            serverLevel.sendParticles(
                    ROACWParticleRegistry.PLAGUE_NANO_GREEN.get(),
                    x, y, z,
                    1,
                    0.0D, 0.0D, 0.0D,
                    0.02D
            );
        }

        for (int i = 0; i < redCount; i++) {

            double x = entity.getX() + (random.nextDouble() - 0.5D) * entity.getBbWidth();
            double y = entity.getY() + random.nextDouble() * entity.getBbHeight();
            double z = entity.getZ() + (random.nextDouble() - 0.5D) * entity.getBbWidth();

            serverLevel.sendParticles(
                    ROACWParticleRegistry.PLAGUE_NANO_RED.get(),
                    x, y, z,
                    1,
                    0.0D, 0.0D, 0.0D,
                    0.015D
            );
        }
    }

    /**
     * Spawns plague particles around an infected entity.
     */
    public static void spawnAroundEntity(Level level,
                                         LivingEntity entity,
                                         int greenCount,
                                         int redCount) {

        if (!level.isClientSide)
            return;

        RandomSource random = level.getRandom();

        for (int i = 0; i < greenCount; i++) {

            double x = entity.getX() + (random.nextDouble() - 0.5D) * entity.getBbWidth();
            double y = entity.getY() + random.nextDouble() * entity.getBbHeight();
            double z = entity.getZ() + (random.nextDouble() - 0.5D) * entity.getBbWidth();

            level.addParticle(
                    ROACWParticleRegistry.PLAGUE_NANO_GREEN.get(),
                    true,
                    x,
                    y,
                    z,
                    (random.nextDouble() - 0.5D) * 0.02D,
                    (random.nextDouble() - 0.5D) * 0.02D,
                    (random.nextDouble() - 0.5D) * 0.02D
            );
        }

        for (int i = 0; i < redCount; i++) {

            double x = entity.getX() + (random.nextDouble() - 0.5D) * entity.getBbWidth();
            double y = entity.getY() + random.nextDouble() * entity.getBbHeight();
            double z = entity.getZ() + (random.nextDouble() - 0.5D) * entity.getBbWidth();

            level.addParticle(
                    ROACWParticleRegistry.PLAGUE_NANO_RED.get(),
                    true,
                    x,
                    y,
                    z,
                    (random.nextDouble() - 0.5D) * 0.015D,
                    (random.nextDouble() - 0.5D) * 0.015D,
                    (random.nextDouble() - 0.5D) * 0.015D
            );
        }
    }
}