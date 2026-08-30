package net.kamaarion.roacw.items.armor.marsarmorset;

import net.kamaarion.roacw.Config; // Added config import
import net.kamaarion.roacw.registeries.ROACWItemRegistry;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingFallEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Mod.EventBusSubscriber(modid = "roacw", bus = Mod.EventBusSubscriber.Bus.FORGE)
public class MarsArmorFlightHandler {
    // Players whose mayfly/flying was granted BY THIS MOD. Only these players
    // should ever have flight revoked by this handler.
    private static final Set<UUID> MARS_FLIGHT_GRANTED = new HashSet<>();
    // Players currently getting a soft-landing (fall damage negation) after losing Mars flight.
    private static final Set<UUID> PROTECTED_PLAYERS = new HashSet<>();

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {

        if (event.phase != TickEvent.Phase.END) return;
        Player player = event.player;
        UUID playerUUID = player.getUUID();

        // Check if config allows flight AND player has the full set equipped
        if (Config.ENABLE_MARS_FLIGHT.get() && hasFullMarsSet(player)) {
            if (!player.getAbilities().mayfly) {
                player.getAbilities().mayfly = true;
                player.onUpdateAbilities();
            }
            MARS_FLIGHT_GRANTED.add(playerUUID);
            PROTECTED_PLAYERS.remove(playerUUID);
        } else {
            // Only strip flight if WE granted it. Never touch flight another mod gave the player.
            if (MARS_FLIGHT_GRANTED.remove(playerUUID)
                    && !player.isCreative() && !player.isSpectator()) {
                player.getAbilities().mayfly = false;
                player.getAbilities().flying = false;
                player.onUpdateAbilities();
                if (!player.onGround()) {
                    PROTECTED_PLAYERS.add(playerUUID);
                }
            }
        }

        if (PROTECTED_PLAYERS.contains(playerUUID)) {
            if (!player.onGround()) {
                Vec3 motion = player.getDeltaMovement();
                if (motion.y < -0.15) {
                    player.setDeltaMovement(motion.x, -0.15, motion.z);
                    player.hurtMarked = true;
                }
                player.fallDistance = 0.0F;
            } else {
                PROTECTED_PLAYERS.remove(playerUUID);
            }
        }
    }

    @SubscribeEvent
    public static void onPlayerLoggedOut(PlayerEvent.PlayerLoggedOutEvent event) {
        UUID playerUUID = event.getEntity().getUUID();
        MARS_FLIGHT_GRANTED.remove(playerUUID);
        PROTECTED_PLAYERS.remove(playerUUID);
    }

    @SubscribeEvent
    public static void onPlayerFall(LivingFallEvent event) {
        if (event.getEntity() instanceof Player player) {
            UUID playerUUID = player.getUUID();
            if (PROTECTED_PLAYERS.contains(playerUUID)) {
                event.setDistance(0.0F);
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

        return helmet.getItem() == ROACWItemRegistry.MARS_VISOR.get() &&
                chest.getItem() == ROACWItemRegistry.MARS_ENGINE.get() &&
                legs.getItem() == ROACWItemRegistry.MARS_LEG_GUARDS.get() &&
                boots.getItem() == ROACWItemRegistry.MARS_BOOSTERS.get();
    }
}