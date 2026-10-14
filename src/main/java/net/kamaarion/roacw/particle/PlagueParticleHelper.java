package net.kamaarion.roacw.particle;

import net.kamaarion.roacw.registeries.ROACWParticleRegistry;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;

public final class PlagueParticleHelper {

    private PlagueParticleHelper() {}

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

            for (ServerPlayer player : serverLevel.players()) {
                serverLevel.sendParticles(
                        player,
                        ROACWParticleRegistry.PLAGUE_CLOUD.get(),
                        true,
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