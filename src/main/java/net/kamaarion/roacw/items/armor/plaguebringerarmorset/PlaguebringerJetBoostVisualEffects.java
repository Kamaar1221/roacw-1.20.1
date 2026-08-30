package net.kamaarion.roacw.items.armor.plaguebringerarmorset;

import net.kamaarion.roacw.ROACW;
import net.kamaarion.roacw.network.PacketPlaguebringerJetBoostStateS2C;
import net.kamaarion.roacw.registeries.ROACWParticleRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.client.event.RenderGuiEvent;
import net.minecraftforge.client.event.ViewportEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * All client-side visual feedback for Plaguebringer jet boost, in one place:
 * - Full-screen green tint while active (RenderGuiEvent.Pre).
 * - Smoothly-eased FOV widening while active (ViewportEvent.ComputeFov).
 * - Canister exhaust particles, both continuous (pre-flight + in-flight) and
 *   one-shot launch bursts (TickEvent.ClientTickEvent).
 *
 * Camera shake is intentionally NOT here - it's handled by ISS's own
 * CameraShakeManager, triggered server-side in PacketPlaguebringerJetBoostC2S.
 */
@Mod.EventBusSubscriber(modid = ROACW.MODID, value = Dist.CLIENT)
public class PlaguebringerJetBoostVisualEffects {

    // ===== Screen tint =====

    // ARGB - alpha in the top byte. TODO: tune to taste; low alpha (~15-20%)
    // reads as a hue rather than a heavy overlay. 0x0AFF6E is a sickly toxic
    // green fitting the plague theme.
    private static final int TINT_COLOR = 0x3319FA19;

    // Hooks RenderGuiEvent.Pre (not Post) so the tint draws UNDER the
    // hotbar/crosshair/text rather than over them - same layering vanilla
    // uses for things like the nausea or low-health vignette.
    @SubscribeEvent
    public static void onRenderGui(RenderGuiEvent.Pre event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return;

        if (!PacketPlaguebringerJetBoostStateS2C.isJetBoostActive(mc.player.getUUID())) return;

        GuiGraphics guiGraphics = event.getGuiGraphics();
        int width = mc.getWindow().getGuiScaledWidth();
        int height = mc.getWindow().getGuiScaledHeight();

        guiGraphics.fill(0, 0, width, height, TINT_COLOR);
    }

    // ===== FOV =====

    private static final double FOV_BOOST_MULTIPLIER = 1.50D; // TODO: tune - vanilla sprint is ~1.0 to ~1.1x, elytra diving goes higher
    private static final double FOV_EASE_SPEED = 0.1D; // fraction of the gap closed per frame - higher = snappier

    private static double currentFovMultiplier = 1.0D;

    @SubscribeEvent
    public static void onComputeFov(ViewportEvent.ComputeFov event) {
        Minecraft mc = Minecraft.getInstance();
        boolean active = mc.player != null
                && PacketPlaguebringerJetBoostStateS2C.isJetBoostActive(mc.player.getUUID());

        double target = active ? FOV_BOOST_MULTIPLIER : 1.0D;
        currentFovMultiplier += (target - currentFovMultiplier) * FOV_EASE_SPEED;

        event.setFOV(event.getFOV() * currentFovMultiplier);
    }

    // ===== Particles =====

    private static final double CANISTER_SIDE_OFFSET = 0.25;
    private static final double CANISTER_HEIGHT_OFFSET = 0.9;
    private static final double CANISTER_BACK_OFFSET = 0.6;


    private static boolean waitingForFirstFlight = false;

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {

        if (event.phase != TickEvent.Phase.END)
            return;

        Minecraft mc = Minecraft.getInstance();
        ClientLevel level = mc.level;

        if (level == null)
            return;

        for (Player player : level.players()) {

            if (!PacketPlaguebringerJetBoostStateS2C.isJetBoostActive(player.getUUID()))
                continue;

            boolean emitParticles = false;

            //
            // Local player
            //

            if (player == mc.player) {

                // Before first flight, always emit.
                if (waitingForFirstFlight) {

                    emitParticles = true;

                    // Switch to flight-only mode once actual flight begins.
                    if (player.getAbilities().flying) {
                        waitingForFirstFlight = false;
                    }

                } else {

                    // After first flight, only emit while flying.
                    emitParticles = player.getAbilities().flying;
                }

            } else {

                // Remote players only emit while flying.
                emitParticles = player.getAbilities().flying;
            }

            if (!emitParticles)
                continue;

            Vec3 velocity = player.getDeltaMovement();

            float yawRad = (float) Math.toRadians(player.yBodyRot);

            double backX = Math.sin(yawRad) * CANISTER_BACK_OFFSET;
            double backZ = -Math.cos(yawRad) * CANISTER_BACK_OFFSET;

            double sideX = Math.cos(yawRad) * CANISTER_SIDE_OFFSET;
            double sideZ = Math.sin(yawRad) * CANISTER_SIDE_OFFSET;

            double prevX = player.getX() - velocity.x;
            double prevY = player.getY() - velocity.y;
            double prevZ = player.getZ() - velocity.z;

            int count = Math.max(2, Mth.clamp((int) (velocity.lengthSqr() * 4), 1, 4));

            for (int i = 0; i < count; i++) {

                float f = i / (float) count;

                double baseX = Mth.lerp(f, prevX, player.getX());
                double baseY = Mth.lerp(f, prevY, player.getY());
                double baseZ = Mth.lerp(f, prevZ, player.getZ());

                level.addParticle(
                        ROACWParticleRegistry.BOOSTER_EXHAUST.get(),
                        baseX + backX + sideX,
                        baseY + CANISTER_HEIGHT_OFFSET,
                        baseZ + backZ + sideZ,
                        0.0,
                        -0.15,
                        0.0
                );

                level.addParticle(
                        ROACWParticleRegistry.BOOSTER_EXHAUST.get(),
                        baseX + backX - sideX,
                        baseY + CANISTER_HEIGHT_OFFSET,
                        baseZ + backZ - sideZ,
                        0.0,
                        -0.15,
                        0.0
                );
            }
        }
    }


    public static void beginPreFlight() {
        waitingForFirstFlight = true;
    }


    public static void spawnLaunchBurst(Player player) {

        Minecraft mc = Minecraft.getInstance();
        ClientLevel level = mc.level;

        if (level == null)
            return;

        float yawRad = (float) Math.toRadians(player.yBodyRot);

        double backX = Math.sin(yawRad) * CANISTER_BACK_OFFSET;
        double backZ = -Math.cos(yawRad) * CANISTER_BACK_OFFSET;

        double sideX = Math.cos(yawRad) * CANISTER_SIDE_OFFSET;
        double sideZ = Math.sin(yawRad) * CANISTER_SIDE_OFFSET;

        double leftX = player.getX() + backX + sideX;
        double leftY = player.getY() + CANISTER_HEIGHT_OFFSET;
        double leftZ = player.getZ() + backZ + sideZ;

        double rightX = player.getX() + backX - sideX;
        double rightY = player.getY() + CANISTER_HEIGHT_OFFSET;
        double rightZ = player.getZ() + backZ - sideZ;

        for (int i = 0; i < 20; i++) {

            double spreadX = (level.random.nextDouble() - 0.5D) * 0.08D;
            double spreadZ = (level.random.nextDouble() - 0.5D) * 0.08D;

            level.addParticle(
                    ROACWParticleRegistry.BOOSTER_EXHAUST.get(),
                    leftX,
                    leftY,
                    leftZ,
                    spreadX,
                    -0.25D - level.random.nextDouble() * 0.08D,
                    spreadZ
            );

            spreadX = (level.random.nextDouble() - 0.5D) * 0.08D;
            spreadZ = (level.random.nextDouble() - 0.5D) * 0.08D;

            level.addParticle(
                    ROACWParticleRegistry.BOOSTER_EXHAUST.get(),
                    rightX,
                    rightY,
                    rightZ,
                    spreadX,
                    -0.25D - level.random.nextDouble() * 0.08D,
                    spreadZ
            );
        }
    }

    public static void resetPreFlight() {
        waitingForFirstFlight = false;
    }

    @SubscribeEvent
    public static void onLoggingOut(ClientPlayerNetworkEvent.LoggingOut event) {
        waitingForFirstFlight = false;
    }
}