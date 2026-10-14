package net.kamaarion.roacw.items.armor.plaguebringerarmorset;

import com.mojang.blaze3d.systems.RenderSystem;
import net.kamaarion.roacw.ROACW;
import net.kamaarion.roacw.network.armorset_abilities.PacketPlaguebringerJetBoostStateS2C;
import net.kamaarion.roacw.registeries.ROACWParticleRegistry;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.model.geom.ModelPart;
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
import org.joml.Matrix4fc;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.joml.Vector4f;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Mod.EventBusSubscriber(
        modid = ROACW.MODID,
        value = Dist.CLIENT
)
public class PlaguebringerJetBoostVisualEffects {

    private static final int TINT_COLOR = 0x3319FA19;

    private static final double FOV_BOOST_MULTIPLIER = 1.50D;
    private static final double FOV_EASE_SPEED = 0.1D;

    private static double currentFovMultiplier = 1.0D;

    private static final float FOG_NEAR = 40.0F;
    private static final float FOG_FAR = 80.0F;

    private static final double CANISTER_SIDE_OFFSET = 0.25D;
    private static final double CANISTER_HEIGHT_OFFSET = 0.9D;
    private static final double CANISTER_BACK_OFFSET = 0.6D;
    private static final double MODEL_ORIGIN_HEIGHT = 1.501D;

    private static final long MAX_SAMPLE_AGE_TICKS = 3L;
    private static final double MAX_CANISTER_DISTANCE_SQR = 2.5D * 2.5D;

    private record CanisterSample(
            Vec3 left,
            Vec3 right,
            long gameTime
    ) {
    }

    private static final Map<UUID, CanisterSample> CANISTER_SAMPLES =
            new ConcurrentHashMap<>();

    private static boolean waitingForFirstFlight = false;

    public static void onRenderGui(RenderGuiEvent.Pre event) {
        Minecraft mc = Minecraft.getInstance();

        if (mc.player == null) {
            return;
        }

        if (!PacketPlaguebringerJetBoostStateS2C.isJetBoostActive(mc.player.getUUID())) {
            return;
        }

        GuiGraphics guiGraphics = event.getGuiGraphics();
        int width = mc.getWindow().getGuiScaledWidth();
        int height = mc.getWindow().getGuiScaledHeight();

        guiGraphics.fill(0, 0, width, height, TINT_COLOR);
    }

    @SubscribeEvent
    public static void onComputeFov(ViewportEvent.ComputeFov event) {
        Minecraft mc = Minecraft.getInstance();

        boolean active = mc.player != null
                && PacketPlaguebringerJetBoostStateS2C.isJetBoostActive(
                mc.player.getUUID()
        );

        double target = active ? FOV_BOOST_MULTIPLIER : 1.0D;

        currentFovMultiplier +=
                (target - currentFovMultiplier) * FOV_EASE_SPEED;

        event.setFOV(event.getFOV() * currentFovMultiplier);
    }

    @SubscribeEvent
    public static void onRenderFog(ViewportEvent.RenderFog event) {
        Minecraft mc = Minecraft.getInstance();

        if (mc.player == null) {
            return;
        }

        if (!PacketPlaguebringerJetBoostStateS2C.isJetBoostActive(mc.player.getUUID())) {
            return;
        }

        event.setNearPlaneDistance(FOG_NEAR);
        event.setFarPlaneDistance(FOG_FAR);
        event.setCanceled(true);
    }

    @SubscribeEvent
    public static void onComputeFogColor(ViewportEvent.ComputeFogColor event) {
        Minecraft mc = Minecraft.getInstance();

        if (mc.player == null) {
            return;
        }

        if (!PacketPlaguebringerJetBoostStateS2C.isJetBoostActive(mc.player.getUUID())) {
            return;
        }

        event.setRed(0.1F);
        event.setGreen(0.98F);
        event.setBlue(0.1F);
    }

    public static void recordCanisterSample(
            Player wearer,
            Matrix4fc pose,
            ModelPart body
    ) {
        if (wearer == null || pose == null || body == null) {
            return;
        }

        Camera camera = Minecraft.getInstance()
                .gameRenderer
                .getMainCamera();

        Vec3 left = canisterWorldPosition(
                pose,
                body,
                camera,
                CANISTER_SIDE_OFFSET
        );

        Vec3 right = canisterWorldPosition(
                pose,
                body,
                camera,
                -CANISTER_SIDE_OFFSET
        );

        CANISTER_SAMPLES.put(
                wearer.getUUID(),
                new CanisterSample(
                        left,
                        right,
                        wearer.level().getGameTime()
                )
        );
    }

    private static Vec3 canisterWorldPosition(
            Matrix4fc pose,
            ModelPart body,
            Camera camera,
            double side
    ) {
        Vector3f point = new Vector3f(
                (float) side,
                (float) (MODEL_ORIGIN_HEIGHT - CANISTER_HEIGHT_OFFSET),
                (float) CANISTER_BACK_OFFSET
        );

        if (body.xRot != 0.0F
                || body.yRot != 0.0F
                || body.zRot != 0.0F) {
            new Quaternionf()
                    .rotationZYX(
                            body.zRot,
                            body.yRot,
                            body.xRot
                    )
                    .transform(point);
        }

        point.add(
                body.x / 16.0F,
                body.y / 16.0F,
                body.z / 16.0F
        );

        Vector4f view = new Vector4f(
                point.x,
                point.y,
                point.z,
                1.0F
        );

        view.mul(pose);

        Vector3f relative = new Vector3f(
                view.x,
                view.y,
                view.z
        ).mul(RenderSystem.getInverseViewRotationMatrix());

        Vec3 cameraPos = camera.getPosition();

        return new Vec3(
                cameraPos.x + relative.x,
                cameraPos.y + relative.y,
                cameraPos.z + relative.z
        );
    }

    private static CanisterSample freshSample(
            Minecraft mc,
            ClientLevel level,
            Player player
    ) {
        CanisterSample sample = CANISTER_SAMPLES.get(player.getUUID());

        if (sample == null
                || level.getGameTime() - sample.gameTime() > MAX_SAMPLE_AGE_TICKS
                || (player == mc.player && mc.options.getCameraType().isFirstPerson())
                || sample.left().distanceToSqr(player.position()) >= MAX_CANISTER_DISTANCE_SQR
                || sample.right().distanceToSqr(player.position()) >= MAX_CANISTER_DISTANCE_SQR) {
            return null;
        }

        return sample;
    }

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) {
            return;
        }

        Minecraft mc = Minecraft.getInstance();
        ClientLevel level = mc.level;

        if (level == null) {
            CANISTER_SAMPLES.clear();
            return;
        }

        for (Player player : level.players()) {
            if (!PacketPlaguebringerJetBoostStateS2C.isJetBoostActive(player.getUUID())) {
                continue;
            }

            boolean emitParticles;

            if (player == mc.player) {
                if (waitingForFirstFlight) {
                    emitParticles = true;

                    if (player.getAbilities().flying) {
                        waitingForFirstFlight = false;
                    }
                } else {
                    emitParticles = player.getAbilities().flying;
                }
            } else {
                emitParticles = player.getAbilities().flying;
            }

            if (!emitParticles) {
                continue;
            }

            spawnExhaust(level, player);
        }

        long currentTime = level.getGameTime();

        CANISTER_SAMPLES.entrySet().removeIf(entry ->
                currentTime - entry.getValue().gameTime() > MAX_SAMPLE_AGE_TICKS
        );
    }

    private static void spawnExhaust(
            ClientLevel level,
            Player player
    ) {
        Minecraft mc = Minecraft.getInstance();

        float yawRad = (float) Math.toRadians(player.yBodyRot);

        double backX = Math.sin(yawRad) * CANISTER_BACK_OFFSET;
        double backZ = -Math.cos(yawRad) * CANISTER_BACK_OFFSET;

        double sideX = Math.cos(yawRad) * CANISTER_SIDE_OFFSET;
        double sideZ = Math.sin(yawRad) * CANISTER_SIDE_OFFSET;

        Vec3 leftRest = new Vec3(
                backX + sideX,
                CANISTER_HEIGHT_OFFSET,
                backZ + sideZ
        );

        Vec3 rightRest = new Vec3(
                backX - sideX,
                CANISTER_HEIGHT_OFFSET,
                backZ - sideZ
        );

        CanisterSample sample = freshSample(mc, level, player);

        Vec3 velocity = player.getDeltaMovement();

        double prevX = player.getX() - velocity.x;
        double prevY = player.getY() - velocity.y;
        double prevZ = player.getZ() - velocity.z;

        int count = Math.max(
                2,
                Mth.clamp(
                        (int) (velocity.lengthSqr() * 4),
                        1,
                        4
                )
        );

        for (int i = 0; i < count; i++) {
            float f = i / (float) count;

            double baseX = Mth.lerp(f, prevX, player.getX());
            double baseY = Mth.lerp(f, prevY, player.getY());
            double baseZ = Mth.lerp(f, prevZ, player.getZ());

            Vec3 left;
            Vec3 right;

            if (sample != null) {
                Vec3 trail = new Vec3(
                        baseX - player.getX(),
                        baseY - player.getY(),
                        baseZ - player.getZ()
                );

                left = sample.left().add(trail);
                right = sample.right().add(trail);
            } else {
                left = new Vec3(
                        baseX + leftRest.x,
                        baseY + leftRest.y,
                        baseZ + leftRest.z
                );

                right = new Vec3(
                        baseX + rightRest.x,
                        baseY + rightRest.y,
                        baseZ + rightRest.z
                );
            }

            level.addParticle(
                    ROACWParticleRegistry.BOOSTER_EXHAUST.get(),
                    left.x,
                    left.y,
                    left.z,
                    0.0D,
                    -0.15D,
                    0.0D
            );

            level.addParticle(
                    ROACWParticleRegistry.BOOSTER_EXHAUST.get(),
                    right.x,
                    right.y,
                    right.z,
                    0.0D,
                    -0.15D,
                    0.0D
            );
        }
    }

    public static void beginPreFlight() {
        waitingForFirstFlight = true;
    }

    public static void spawnLaunchBurst(Player player) {
        Minecraft mc = Minecraft.getInstance();
        ClientLevel level = mc.level;

        if (level == null || player == null) {
            return;
        }

        float yawRad = (float) Math.toRadians(player.yBodyRot);

        double backX = Math.sin(yawRad) * CANISTER_BACK_OFFSET;
        double backZ = -Math.cos(yawRad) * CANISTER_BACK_OFFSET;

        double sideX = Math.cos(yawRad) * CANISTER_SIDE_OFFSET;
        double sideZ = Math.sin(yawRad) * CANISTER_SIDE_OFFSET;

        CanisterSample sample = freshSample(mc, level, player);

        double leftX = sample != null
                ? sample.left().x
                : player.getX() + backX + sideX;

        double leftY = sample != null
                ? sample.left().y
                : player.getY() + CANISTER_HEIGHT_OFFSET;

        double leftZ = sample != null
                ? sample.left().z
                : player.getZ() + backZ + sideZ;

        double rightX = sample != null
                ? sample.right().x
                : player.getX() + backX - sideX;

        double rightY = sample != null
                ? sample.right().y
                : player.getY() + CANISTER_HEIGHT_OFFSET;

        double rightZ = sample != null
                ? sample.right().z
                : player.getZ() + backZ - sideZ;

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
        currentFovMultiplier = 1.0D;
        CANISTER_SAMPLES.clear();
    }
}