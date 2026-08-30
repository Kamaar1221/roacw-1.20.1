package net.kamaarion.roacw.network;

import net.kamaarion.roacw.registeries.ROACWEffectRegistry;
import net.kamaarion.roacw.registeries.ROACWItemRegistry;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.TickTask;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.scores.PlayerTeam;
import net.minecraft.world.scores.Scoreboard;
import net.minecraft.world.scores.Team;
import net.minecraftforge.network.NetworkEvent;
import top.theillusivec4.curios.api.CuriosApi;

import java.util.List;
import java.util.function.Supplier;

import static net.kamaarion.roacw.registeries.ROACWSoundRegistry.DASH_WHOOSH;

/**
 * C2S packet for the Evasion Scarf curio's dash ability: a short forward
 * burst that temporarily disables collision with anything it passes
 * through, via a scratch scoreboard team, and buffs the player on a
 * successful pass-through hit.
 */
public class PacketEvasionScarfDashC2S {

    private static final int SCARF_COOLDOWN_TICKS = 200;

    // Required blank constructor for default registry pipelines
    public PacketEvasionScarfDashC2S() {
    }

    public PacketEvasionScarfDashC2S(FriendlyByteBuf buf) {
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

            Item dashItem = ROACWItemRegistry.EVASION_SCARF.get();
            if (player.getCooldowns().isOnCooldown(dashItem)) {
                float cooldownPercent = player.getCooldowns().getCooldownPercent(dashItem, 0.0F);
                long remainingTicks = (long) (cooldownPercent * SCARF_COOLDOWN_TICKS);
                long remainingSeconds = (remainingTicks + 19) / 20;
                player.displayClientMessage(Component.literal("§cAbility on cooldown! Wait " + remainingSeconds + "s"), true);
                return;
            }

            CuriosApi.getCuriosInventory(player).ifPresent(inv -> {
                if (inv.findCurios(dashItem).isEmpty()) {
                    return;
                }

                player.getCooldowns().addCooldown(dashItem, SCARF_COOLDOWN_TICKS);

                Vec3 lookDirection = player.getLookAngle();
                double dashSpeed = 2.0;
                Vec3 motion = new Vec3(lookDirection.x, 0.1, lookDirection.z).normalize().scale(dashSpeed);
                player.setDeltaMovement(motion);
                player.hurtMarked = true;

                player.level().playSound(null, player.getX(), player.getY(), player.getZ(), DASH_WHOOSH.get(), SoundSource.PLAYERS, 1.0F, 1.0F);
                if (player.level() instanceof ServerLevel serverLevel) {
                    serverLevel.sendParticles(ParticleTypes.CLOUD, player.getX(), player.getY() + 0.5, player.getZ(), 15, 0.2, 0.2, 0.2, 0.1);
                }

                AABB dashZone = player.getBoundingBox().expandTowards(motion.scale(3.0));
                List<LivingEntity> targets = player.level().getEntitiesOfClass(
                        LivingEntity.class, dashZone, entity -> entity != player
                );

                Scoreboard scoreboard = player.level().getScoreboard();
                String teamName = "roacw_dash_" + player.getUUID().toString().substring(0, 8);
                PlayerTeam dashTeam = scoreboard.getPlayerTeam(teamName);
                if (dashTeam == null) {
                    dashTeam = scoreboard.addPlayerTeam(teamName);
                }
                dashTeam.setCollisionRule(Team.CollisionRule.NEVER);

                scoreboard.addPlayerToTeam(player.getScoreboardName(), dashTeam);

                boolean successfullyHitTarget = false;
                for (LivingEntity target : targets) {
                    scoreboard.addPlayerToTeam(target.getScoreboardName(), dashTeam);
                    successfullyHitTarget = true;
                }

                final PlayerTeam finalTeam = dashTeam;
                player.server.tell(new TickTask(player.server.getTickCount() + 10, () -> {
                    if (finalTeam != null) {
                        scoreboard.removePlayerTeam(finalTeam);
                    }
                }));

                if (successfullyHitTarget) {
                    player.addEffect(new MobEffectInstance(
                            ROACWEffectRegistry.EVASION_SCARF_BUFF.get(), 100, 1, false, false, true
                    ));
                }
            });
        });
        return true;
    }
}