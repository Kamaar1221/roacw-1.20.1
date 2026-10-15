package net.kamaarion.roacw.network.armorset_abilities;

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

public class PacketAuricTeslaDashC2S {
    private static final Map<UUID, Long> AURIC_COOLDOWN_TRACKER = new HashMap<>();

    private static final int DART_COUNT = 8;
    private static final double DART_SEARCH_RADIUS = 24.0D;
    private static final double DART_LAUNCH_SPEED = 1.4D;
    private static final double DART_RING_RADIUS = 0.5D;
    private static final double DART_RING_BEHIND_DISTANCE = 2.0D;

    public PacketAuricTeslaDashC2S() {
    }

    public PacketAuricTeslaDashC2S(FriendlyByteBuf buf) {
    }

    public void toBytes(FriendlyByteBuf buf) {
    }

    public boolean handle(Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context context = supplier.get();

        context.enqueueWork(() -> {
            ServerPlayer player = context.getSender();

            if (player == null) {
                return;
            }

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

                player.displayClientMessage(
                        Component.literal("§cAbility on cooldown! Wait " + remainingSeconds + "s"),
                        true
                );

                return;
            }

            AURIC_COOLDOWN_TRACKER.put(player.getUUID(), currentServerTick + 600);

            Vec3 lookDirection = player.getLookAngle();
            double auricDashSpeed = 4.0D;
            double auricDashHeight = 0.1D;

            Vec3 motion = new Vec3(
                    lookDirection.x,
                    auricDashHeight,
                    lookDirection.z
            ).normalize().scale(auricDashSpeed);

            player.setDeltaMovement(motion);
            player.hurtMarked = true;

            player.level().playSound(
                    null,
                    player.getX(),
                    player.getY(),
                    player.getZ(),
                    ROACWSoundRegistry.AURIC_DASH_CHARGE.get(),
                    SoundSource.PLAYERS,
                    1.0F,
                    1.2F
            );

            if (player.level() instanceof ServerLevel serverLevel) {
                serverLevel.sendParticles(
                        ParticleTypes.CLOUD,
                        player.getX(),
                        player.getY() + 0.5D,
                        player.getZ(),
                        25,
                        0.3D,
                        0.3D,
                        0.3D,
                        0.15D
                );
            }

            spawnGodKillerDartBurst(player);

            AABB damageZone = player.getBoundingBox().expandTowards(motion.scale(4.0D));
            List<LivingEntity> targets = player.level().getEntitiesOfClass(
                    LivingEntity.class,
                    damageZone,
                    entity -> entity != player
            );

            float totalHealAmount = 0.0F;

            for (LivingEntity target : targets) {
                float damageGiven = 30.0F;

                if (target.hurt(
                        player.damageSources().indirectMagic(player, player),
                        damageGiven
                )) {
                    totalHealAmount += 10.0F + damageGiven * 0.20F;
                }

                target.knockback(1.2D, -motion.x, -motion.z);
                target.hurtMarked = true;

                if (player.level() instanceof ServerLevel serverLevel) {
                    serverLevel.sendParticles(
                            ParticleTypes.EXPLOSION,
                            target.getX(),
                            target.getY() + 1.0D,
                            target.getZ(),
                            3,
                            0.1D,
                            0.1D,
                            0.1D,
                            0.0D
                    );

                    serverLevel.sendParticles(
                            ParticleTypes.ELECTRIC_SPARK,
                            target.getX(),
                            target.getY() + 1.0D,
                            target.getZ(),
                            15,
                            0.3D,
                            0.3D,
                            0.3D,
                            0.2D
                    );
                }

                player.level().playSound(
                        null,
                        target.getX(),
                        target.getY(),
                        target.getZ(),
                        SoundEvents.LIGHTNING_BOLT_THUNDER,
                        SoundSource.PLAYERS,
                        0.6F,
                        1.6F
                );
            }

            if (totalHealAmount > 0.0F) {
                player.heal(totalHealAmount);

                player.level().playSound(
                        null,
                        player.getX(),
                        player.getY(),
                        player.getZ(),
                        SoundEvents.WITCH_DRINK,
                        SoundSource.PLAYERS,
                        0.5F,
                        1.4F
                );

                if (player.level() instanceof ServerLevel serverLevel) {
                    serverLevel.sendParticles(
                            ParticleTypes.HEART,
                            player.getX(),
                            player.getY() + 1.0D,
                            player.getZ(),
                            5,
                            0.4D,
                            0.4D,
                            0.4D,
                            0.0D
                    );
                }
            }
        });

        return true;
    }

    private static void spawnGodKillerDartBurst(ServerPlayer player) {
        AABB searchArea = player.getBoundingBox().inflate(DART_SEARCH_RADIUS);

        List<LivingEntity> nearbyEnemies = player.level().getEntitiesOfClass(
                LivingEntity.class,
                searchArea,
                entity -> entity != player
                        && entity.isAlive()
                        && !entity.isAlliedTo(player)
                        && isViableHostileTarget(entity)
        );

        Vec3 lookDirection = player.getLookAngle();

        Vec3 forward = new Vec3(
                lookDirection.x,
                0.0D,
                lookDirection.z
        );

        if (forward.lengthSqr() < 1.0E-6D) {
            forward = new Vec3(0.0D, 0.0D, 1.0D);
        } else {
            forward = forward.normalize();
        }

        Vec3 right = new Vec3(
                -forward.z,
                0.0D,
                forward.x
        ).normalize();

        Vec3 up = new Vec3(0.0D, 1.0D, 0.0D);

        Vec3 ringCenter = new Vec3(
                player.getX(),
                player.getY() + 2.0D,
                player.getZ()
        ).subtract(forward.scale(DART_RING_BEHIND_DISTANCE));

        for (int i = 0; i < DART_COUNT; i++) {
            double angle = (2.0D * Math.PI / DART_COUNT) * i;

            Vec3 radialDirection = right.scale(Math.cos(angle))
                    .add(up.scale(Math.sin(angle)));

            Vec3 spawnPosition = ringCenter.add(
                    radialDirection.scale(DART_RING_RADIUS)
            );

            GodKillerDartEntity dart = new GodKillerDartEntity(
                    ROACWEntityRegistry.GOD_KILLER_DART.get(),
                    player,
                    player.level()
            );

            dart.setPos(
                    spawnPosition.x,
                    spawnPosition.y,
                    spawnPosition.z
            );

            if (!nearbyEnemies.isEmpty()) {
                dart.setTarget(nearbyEnemies.get(i % nearbyEnemies.size()));
            }

            Vec3 launchDirection = radialDirection.scale(0.4D)
                    .add(forward.scale(-0.6D))
                    .normalize();

            dart.setDeltaMovement(launchDirection.scale(DART_LAUNCH_SPEED));

            player.level().addFreshEntity(dart);
        }
    }

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

        return head.is(ROACWItemRegistry.AURIC_TESLA_ROYAL_HELM.get())
                && chest.is(ROACWItemRegistry.AURIC_TESLA_CUIRASS.get())
                && legs.is(ROACWItemRegistry.AURIC_TESLA_CUISSES.get())
                && feet.is(ROACWItemRegistry.AURIC_TESLA_BOOTS.get());
    }
}