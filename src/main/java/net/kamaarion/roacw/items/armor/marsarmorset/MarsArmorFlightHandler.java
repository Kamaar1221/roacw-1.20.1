package net.kamaarion.roacw.items.armor.marsarmorset;

import net.kamaarion.roacw.registeries.ROACWItemRegistry;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingFallEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Mod.EventBusSubscriber(modid = "roacw", bus = Mod.EventBusSubscriber.Bus.FORGE)
public class MarsArmorFlightHandler {


    private static final Set<UUID> PROTECTED_PLAYERS = new HashSet<>();

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END || event.player.level().isClientSide()) return;

        Player player = event.player;
        UUID playerUUID = player.getUUID();

        if (hasFullMarsSet(player)) {
            if (!player.getAbilities().mayfly) {
                player.getAbilities().mayfly = true;
                player.onUpdateAbilities();
            }

            PROTECTED_PLAYERS.remove(playerUUID);
        } else {
            if (!player.isCreative() && !player.isSpectator() && player.getAbilities().mayfly) {
                player.getAbilities().mayfly = false;
                player.getAbilities().flying = false;
                player.onUpdateAbilities();

                if (!player.onGround()) {
                    PROTECTED_PLAYERS.add(playerUUID);
                }
            }

            if (PROTECTED_PLAYERS.contains(playerUUID) && !player.onGround()) {
                Vec3 motion = player.getDeltaMovement();

                if (motion.y < -0.15) {
                    player.setDeltaMovement(motion.x, -0.15, motion.z);
                    player.hurtMarked = true;
                }
            }


            if (player.onGround()) {
                PROTECTED_PLAYERS.remove(playerUUID);
            }
        }

        if (player.onGround()) {
            PROTECTED_PLAYERS.remove(playerUUID);
        }
    }

    @SubscribeEvent
    public static void onPlayerFall(LivingFallEvent event) {

        if (event.getEntity() instanceof Player player) {
            UUID playerUUID = player.getUUID();

            if (PROTECTED_PLAYERS.contains(playerUUID)) {
                event.setCanceled(true);
                PROTECTED_PLAYERS.remove(playerUUID);
            }
        }
    }


    private static boolean hasFullMarsSet(Player player) {
        ItemStack helmet = player.getItemBySlot(EquipmentSlot.HEAD);
        ItemStack chest = player.getItemBySlot(EquipmentSlot.CHEST);
        ItemStack legs = player.getItemBySlot(EquipmentSlot.LEGS);
        ItemStack boots = player.getItemBySlot(EquipmentSlot.FEET);

        // Assumes your registry names match standard conventions; update if necessary
        return helmet.getItem() == ROACWItemRegistry.MARS_VISOR.get() &&
                chest.getItem() == ROACWItemRegistry.MARS_ENGINE.get() &&
                legs.getItem() == ROACWItemRegistry.MARS_LEG_GUARDS.get() &&
                boots.getItem() == ROACWItemRegistry.MARS_BOOSTERS.get();
    }
}
