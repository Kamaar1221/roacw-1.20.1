package net.kamaarion.roacw.items.curios.wings;

import net.kamaarion.roacw.Config;
import net.kamaarion.roacw.ROACW;
import net.kamaarion.roacw.mixin.ServerGamePacketListenerImplAccessor;
import net.kamaarion.roacw.network.ModMessages;
import net.kamaarion.roacw.network.misc.PacketFlightWingsTimeS2C;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.entity.living.LivingFallEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Mod.EventBusSubscriber(modid = ROACW.MODID)
public final class FlightWingsHandler {
    private static final String WING_TIME_TAG = "roacw_wing_time";
    private static final ResourceLocation FALL_FLYING_ID = ResourceLocation.fromNamespaceAndPath("caelus", "fall_flying");
    private static final UUID GLIDE_MODIFIER_ID = UUID.fromString("5d0c4e7a-3f4b-4b9e-9a57-7a1d6f0e9b21");

    private static final Set<UUID> JUMP_HELD = ConcurrentHashMap.newKeySet();

    private FlightWingsHandler() {}

    public static void setHeld(Player player, boolean held) {
        if (held) JUMP_HELD.add(player.getUUID());
        else JUMP_HELD.remove(player.getUUID());
    }

    public static int getWingTime(Player player, int max) {
        CompoundTag data = player.getPersistentData();
        return data.contains(WING_TIME_TAG) ? Math.min(data.getInt(WING_TIME_TAG), max) : max;
    }

    public static void sync(ServerPlayer player, FlightWings wings) {
        ModMessages.sendToPlayer(new PacketFlightWingsTimeS2C(
                getWingTime(player, wings.getMaxWingTime()),
                Config.WINGS_FALL_MODE.get()), player);
    }

    public static void tick(ServerPlayer player, FlightWings wings) {
        if (player.tickCount % 100 == 0) {
            sync(player, wings);
        }

        int max = wings.getMaxWingTime();
        int wingTime = getWingTime(player, max);
        int newWingTime = wingTime;

        boolean grounded = player.onGround() || player.isInWater() || player.isInLava()
                || player.onClimbable() || player.isPassenger();
        boolean gliding = player.isFallFlying();

        if (!grounded && player.connection != null) {
            ((ServerGamePacketListenerImplAccessor) player.connection).roacw$setAboveGroundTickCount(0);
        }

        if (grounded) {
            newWingTime = max;
        } else if (!gliding && wingTime > 0 && JUMP_HELD.contains(player.getUUID())) {
            newWingTime = wingTime - 1;
            player.fallDistance = 0.0F;
        }

        if (newWingTime != wingTime) {
            player.getPersistentData().putInt(WING_TIME_TAG, newWingTime);
            sync(player, wings);
        }

        boolean glideEnabled = newWingTime <= 0 && Config.WINGS_FALL_MODE.get() == FlightWings.FallMode.GLIDE;
        setGlideEnabled(player, glideEnabled);

        if (glideEnabled && !grounded && !gliding
                && player.getDeltaMovement().y < 0.0D
                && !player.hasEffect(MobEffects.LEVITATION)) {
            player.startFallFlying();
        }
    }

    public static void clear(Player player) {
        JUMP_HELD.remove(player.getUUID());
        setGlideEnabled(player, false);
    }

    private static void setGlideEnabled(Player player, boolean enabled) {
        Attribute fallFlying = ForgeRegistries.ATTRIBUTES.getValue(FALL_FLYING_ID);
        if (fallFlying == null) return;
        AttributeInstance instance = player.getAttribute(fallFlying);
        if (instance == null) return;

        AttributeModifier current = instance.getModifier(GLIDE_MODIFIER_ID);
        if (enabled && current == null) {
            instance.addTransientModifier(new AttributeModifier(GLIDE_MODIFIER_ID, "Flight Wings Glide", 1.0D,
                    AttributeModifier.Operation.ADDITION));
        } else if (!enabled && current != null) {
            instance.removeModifier(GLIDE_MODIFIER_ID);
        }
    }

    @SubscribeEvent
    public static void onFall(LivingFallEvent event) {
        if (FlightWings.getWorn(event.getEntity()) != null) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void onLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            FlightWings wings = FlightWings.getWorn(player);
            if (wings != null) sync(player, wings);
        }
    }

    @SubscribeEvent
    public static void onLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        JUMP_HELD.remove(event.getEntity().getUUID());
    }
}