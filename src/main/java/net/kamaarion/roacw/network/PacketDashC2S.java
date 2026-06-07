package net.kamaarion.roacw.network;

import net.kamaarion.roacw.registeries.ROACWEffectRegistry;
import net.kamaarion.roacw.registeries.ROACWItemRegistry;
import net.kamaarion.roacw.registeries.ROACWSoundRegistry;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.NetworkEvent;
import top.theillusivec4.curios.api.CuriosApi;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Supplier;

public class PacketDashC2S {
    private final int dashType;
    private static final Map<UUID, Long> AURIC_COOLDOWN_TRACKER = new HashMap<>();

    // Required blank constructor for default registry pipelines
    public PacketDashC2S() {
        this.dashType = 1; // Defaults safely to Scarf if called raw
    }

    public PacketDashC2S(int dashType) {
        this.dashType = dashType;
    }

    public PacketDashC2S(FriendlyByteBuf buf) {
        this.dashType = buf.readInt();
    }

    public void toBytes(FriendlyByteBuf buf) {
        buf.writeInt(this.dashType);
    }

    public boolean handle(Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context context = supplier.get();
        context.enqueueWork(() -> {
            ServerPlayer player = context.getSender();
            if (player == null) return;

            // ==========================================
            // START: AURIC TESLA DASH LOGIC (Type 2)
            // ==========================================
            if (this.dashType == 2 && isWearingFullAuricTesla(player)) {
                long currentServerTick = player.server.getTickCount();
                long nextAvailableTick = AURIC_COOLDOWN_TRACKER.getOrDefault(player.getUUID(), 0L);

                if (currentServerTick >= nextAvailableTick) {
                    AURIC_COOLDOWN_TRACKER.put(player.getUUID(), currentServerTick + 200);

                    Vec3 lookDirection = player.getLookAngle();
                    double auricDashSpeed = 2.8;
                    double auricDashHeight = 0.1;

                    Vec3 motion = new Vec3(lookDirection.x, auricDashHeight, lookDirection.z).normalize().scale(auricDashSpeed);

                    player.setDeltaMovement(motion);
                    player.hurtMarked = true;

                    player.level().playSound(null, player.getX(), player.getY(), player.getZ(),
                            ROACWSoundRegistry.DASH_WHOOSH.get(), SoundSource.PLAYERS, 1.0F, 1.2F);

                    if (player.level() instanceof net.minecraft.server.level.ServerLevel serverLevel) {
                        serverLevel.sendParticles(ParticleTypes.CLOUD, player.getX(), player.getY() + 0.5, player.getZ(), 25, 0.3, 0.3, 0.3, 0.15);
                    }

                    // === AURIC TESLA DAMAGE & LIFESTEAL ZONE ===
                    AABB damageZone = player.getBoundingBox().expandTowards(motion.scale(4.0));
                    java.util.List<LivingEntity> targets = player.level().getEntitiesOfClass(
                            LivingEntity.class, damageZone, entity -> entity != player
                    );

                    float totalHealAmount = 0.0F;

                    for (LivingEntity target : targets) {
                        float damageGiven = 30.0F;

                        if (target.hurt(player.damageSources().indirectMagic(player, player), damageGiven)) {
                            totalHealAmount += (damageGiven * 0.20F);
                        }

                        target.knockback(1.2, -motion.x, -motion.z);
                        target.hurtMarked = true;

                        if (player.level() instanceof net.minecraft.server.level.ServerLevel serverLevel) {
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

                        if (player.level() instanceof net.minecraft.server.level.ServerLevel serverLevel) {
                            serverLevel.sendParticles(ParticleTypes.HEART, player.getX(), player.getY() + 1.0, player.getZ(), 5, 0.4, 0.4, 0.4, 0.0);
                        }
                    }

                    return;
                }
            }
            // ==========================================
            // END: AURIC TESLA DASH LOGIC
            // ==========================================

            // ==========================================
            // START: ORIGINAL EVASION SCARF LOGIC (Type 1)
            // ==========================================
            if (this.dashType == 1) {
                Item dashItem = ROACWItemRegistry.EVASION_SCARF.get();
                if (player.getCooldowns().isOnCooldown(dashItem)) {
                    return;
                }

                CuriosApi.getCuriosInventory(player).ifPresent(inv -> {
                    if (!inv.findCurios(dashItem).isEmpty()) {
                        player.getCooldowns().addCooldown(dashItem, 200);

                        Vec3 lookDirection = player.getLookAngle();
                        double dashSpeed = 1.5;
                        Vec3 motion = new Vec3(lookDirection.x, 0.1, lookDirection.z).normalize().scale(dashSpeed);
                        player.setDeltaMovement(motion);
                        player.hurtMarked = true;

                        player.level().playSound(null, player.getX(), player.getY(), player.getZ(), ROACWSoundRegistry.DASH_WHOOSH.get(), SoundSource.PLAYERS, 1.0F, 1.0F);
                        if (player.level() instanceof net.minecraft.server.level.ServerLevel serverLevel) {
                            serverLevel.sendParticles(net.minecraft.core.particles.ParticleTypes.CLOUD, player.getX(), player.getY() + 0.5, player.getZ(), 15, 0.2, 0.2, 0.2, 0.1);
                        }

                        net.minecraft.world.phys.AABB dashZone = player.getBoundingBox().expandTowards(motion.scale(3.0));
                        java.util.List targets = player.level().getEntitiesOfClass(
                                net.minecraft.world.entity.LivingEntity.class, dashZone, entity -> entity != player
                        );

                        net.minecraft.world.scores.Scoreboard scoreboard = player.level().getScoreboard();
                        String teamName = "roacw_dash_" + player.getUUID().toString().substring(0, 8);
                        net.minecraft.world.scores.PlayerTeam dashTeam = scoreboard.getPlayerTeam(teamName);
                        if (dashTeam == null) {
                            dashTeam = scoreboard.addPlayerTeam(teamName);
                        }
                        dashTeam.setCollisionRule(net.minecraft.world.scores.Team.CollisionRule.NEVER);

                        scoreboard.addPlayerToTeam(player.getScoreboardName(), dashTeam);

                        boolean successfullyHitTarget = false;
                        for (Object obj : targets) {
                            if (obj instanceof net.minecraft.world.entity.LivingEntity target) {
                                scoreboard.addPlayerToTeam(target.getScoreboardName(), dashTeam);
                                successfullyHitTarget = true;
                            }
                        }

                        final net.minecraft.world.scores.PlayerTeam finalTeam = dashTeam;
                        player.server.tell(new net.minecraft.server.TickTask(player.server.getTickCount() + 10, () -> {
                            if (finalTeam != null) {
                                scoreboard.removePlayerTeam(finalTeam);
                            }
                        }));

                        if (successfullyHitTarget) {
                            player.addEffect(new net.minecraft.world.effect.MobEffectInstance(
                                    ROACWEffectRegistry.EVASION_SCARF_BUFF.get(), 100, 1, false, false, true
                            ));
                        }
                    }
                });
            }
        });
        return true;
    }


        private static boolean isWearingFullAuricTesla(ServerPlayer player) {
            net.minecraft.world.item.ItemStack head = player.getItemBySlot(EquipmentSlot.HEAD);
            net.minecraft.world.item.ItemStack chest = player.getItemBySlot(EquipmentSlot.CHEST);
            net.minecraft.world.item.ItemStack legs = player.getItemBySlot(EquipmentSlot.LEGS); // FIX: Evaluates the correct leg slot
            net.minecraft.world.item.ItemStack feet = player.getItemBySlot(EquipmentSlot.FEET);

        return head.is(ROACWItemRegistry.AURIC_TESLA_ROYAL_HELM.get()) &&
                chest.is(ROACWItemRegistry.AURIC_TESLA_CUIRASS.get()) &&
                legs.is(ROACWItemRegistry.AURIC_TESLA_CUISSES.get()) &&
                feet.is(ROACWItemRegistry.AURIC_TESLA_BOOTS.get());
    }
}
