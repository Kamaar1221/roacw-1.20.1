package net.kamaarion.roacw.items.curios.wings;

import net.kamaarion.roacw.ROACW;
import net.kamaarion.roacw.network.ModMessages;
import net.kamaarion.roacw.network.misc.PacketFlightWingsThrustC2S;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = ROACW.MODID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class FlightWingsClient {
    private static int wingTime = Integer.MAX_VALUE;
    private static FlightWings.FallMode fallMode = FlightWings.FallMode.GLIDE;
    private static boolean lastSentHeld = false;

    private FlightWingsClient() {}

    public static int getWingTime() {
        return wingTime;
    }

    public static void setState(int ticks, FlightWings.FallMode mode) {
        wingTime = ticks;
        fallMode = mode;
    }

    @SubscribeEvent
    public static void onLoggingIn(ClientPlayerNetworkEvent.LoggingIn event) {
        lastSentHeld = false;
        wingTime = Integer.MAX_VALUE;
        fallMode = FlightWings.FallMode.GLIDE;
    }

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.START || !(event.player instanceof LocalPlayer player)) return;

        FlightWings wings = FlightWings.getWorn(player);
        boolean held = wings != null && player.input.jumping;

        if (held != lastSentHeld) {
            ModMessages.sendToServer(new PacketFlightWingsThrustC2S(held));
            lastSentHeld = held;
        }

        if (!held) return;

        boolean grounded = player.onGround() || player.isInWater() || player.isInLava()
                || player.onClimbable() || player.isPassenger();
        if (grounded || player.isFallFlying()) return;

        Vec3 motion = player.getDeltaMovement();

        if (wingTime > 0) {
            double maxRiseSpeed = wings.getMaxRiseSpeed();
            double y = motion.y < maxRiseSpeed ? Math.min(motion.y + wings.getThrustAccel(), maxRiseSpeed) : motion.y;
            player.setDeltaMovement(motion.x, y, motion.z);
        } else if (fallMode == FlightWings.FallMode.SLOW_FALL && motion.y < -FlightWings.SLOW_FALL_SPEED) {
            player.setDeltaMovement(motion.x, -FlightWings.SLOW_FALL_SPEED, motion.z);
        } else {
            return;
        }

        player.moveRelative(wings.getHorizontalBoost(), new Vec3(player.xxa, 0.0D, player.zza));
        player.fallDistance = 0.0F;
    }
}