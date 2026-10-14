package net.kamaarion.roacw.items.curios.ethereal_talisman;

import io.redspace.ironsspellbooks.api.magic.MagicData;
import io.redspace.ironsspellbooks.api.registry.AttributeRegistry;
import io.redspace.ironsspellbooks.damage.SpellDamageSource;
import io.redspace.ironsspellbooks.network.SyncManaPacket;
import io.redspace.ironsspellbooks.setup.PacketDistributor;
import net.kamaarion.roacw.ROACW;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;

@Mod.EventBusSubscriber(
        modid = ROACW.MODID,
        bus = Mod.EventBusSubscriber.Bus.FORGE
)
public class EtherealTalismanEvents {


    private static final float MANA_PER_KILL = 20.0F;

    private static final float MANA_DAMAGE_SCALING = 0.5F;

    private static final int MANA_REWARD_DELAY_TICKS = 5;

    private static final Map<UUID, Integer> PENDING_MANA_REWARDS = new HashMap<>();

    private static final Map<UUID, Integer> MANA_REWARD_TIMERS = new HashMap<>();

    @SubscribeEvent
    public static void onLivingDeath(LivingDeathEvent event) {
        LivingEntity killedEntity = event.getEntity();

        LivingEntity killCredit = killedEntity.getKillCredit();

        if (killCredit instanceof ServerPlayer player) {
            queueManaReward(player);
            return;
        }

        Entity damageEntity = event.getSource().getEntity();

        if (damageEntity == null) {
            return;
        }

        ServerPlayer player = findOwningPlayer(damageEntity);

        if (player != null) {
            queueManaReward(player);
        }
    }

    @SubscribeEvent
    public static void onLivingHurt(LivingHurtEvent event) {
        if (!(event.getSource() instanceof SpellDamageSource)) {
            return;
        }

        if (!(event.getSource().getEntity() instanceof ServerPlayer player)) {
            return;
        }

        if (!EtherealTalisman.isEquipped(player)) {
            return;
        }

        MagicData magicData = MagicData.getPlayerMagicData(player);

        if (magicData == null) {
            return;
        }

        float currentMana = magicData.getMana();

        double maxMana = player.getAttributeValue(
                AttributeRegistry.MAX_MANA.get()
        );

        if (maxMana <= 0.0D) {
            return;
        }

        float manaPercentage = (float) Math.max(
                0.0D,
                Math.min(
                        1.0D,
                        currentMana / maxMana
                )
        );

        float damageMultiplier =
                1.0F + (manaPercentage * MANA_DAMAGE_SCALING);

        event.setAmount(event.getAmount() * damageMultiplier);
    }

    @SubscribeEvent
    public static void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) {
            return;
        }

        if (PENDING_MANA_REWARDS.isEmpty()) {
            return;
        }

        Iterator<Map.Entry<UUID, Integer>> iterator =
                PENDING_MANA_REWARDS.entrySet().iterator();

        while (iterator.hasNext()) {
            Map.Entry<UUID, Integer> entry = iterator.next();

            UUID playerUUID = entry.getKey();
            int pendingRewards = entry.getValue();

            ServerPlayer player = event.getServer()
                    .getPlayerList()
                    .getPlayer(playerUUID);

            if (player == null) {
                MANA_REWARD_TIMERS.remove(playerUUID);
                iterator.remove();
                continue;
            }

            if (!EtherealTalisman.isEquipped(player)) {
                MANA_REWARD_TIMERS.remove(playerUUID);
                iterator.remove();
                continue;
            }

            int timer = MANA_REWARD_TIMERS.getOrDefault(
                    playerUUID,
                    0
            );

            if (timer > 0) {
                timer--;

                MANA_REWARD_TIMERS.put(
                        playerUUID,
                        timer
                );

                continue;
            }

            giveManaReward(player);

            pendingRewards--;

            if (pendingRewards > 0) {
                PENDING_MANA_REWARDS.put(
                        playerUUID,
                        pendingRewards
                );

                MANA_REWARD_TIMERS.put(
                        playerUUID,
                        MANA_REWARD_DELAY_TICKS
                );
            } else {
                iterator.remove();
                MANA_REWARD_TIMERS.remove(playerUUID);
            }
        }
    }

    private static void queueManaReward(ServerPlayer player) {
        if (!EtherealTalisman.isEquipped(player)) {
            return;
        }

        UUID playerUUID = player.getUUID();

        int pendingRewards = PENDING_MANA_REWARDS.getOrDefault(
                playerUUID,
                0
        );

        pendingRewards++;

        PENDING_MANA_REWARDS.put(
                playerUUID,
                pendingRewards
        );

        if (!MANA_REWARD_TIMERS.containsKey(playerUUID)) {
            MANA_REWARD_TIMERS.put(
                    playerUUID,
                    MANA_REWARD_DELAY_TICKS
            );
        }
    }

    private static void giveManaReward(ServerPlayer player) {
        MagicData magicData = MagicData.getPlayerMagicData(player);

        if (magicData == null) {
            return;
        }

        magicData.addMana(MANA_PER_KILL);

        PacketDistributor.sendToPlayer(
                player,
                new SyncManaPacket(magicData)
        );
    }

    private static ServerPlayer findOwningPlayer(Entity entity) {
        if (entity instanceof ServerPlayer player) {
            return player;
        }

        if (entity instanceof Projectile projectile) {
            Entity owner = projectile.getOwner();

            if (owner instanceof ServerPlayer player) {
                return player;
            }

            if (owner != null) {
                return findOwningPlayer(owner);
            }
        }

        return null;
    }
}