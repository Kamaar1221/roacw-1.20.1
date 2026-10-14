package net.kamaarion.roacw.items.armor.marsarmorset;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.kamaarion.roacw.registeries.ROACWParticleRegistry;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.joml.Matrix4fc;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.joml.Vector4f;

import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Mod.EventBusSubscriber(
        modid = "roacw",
        bus = Mod.EventBusSubscriber.Bus.FORGE,
        value = Dist.CLIENT
)
public class MarsArmorParticleHandler {

    private static final double JET_SIDE_OFFSET = 0.73D;
    private static final double JET_HEIGHT_OFFSET = 0.8D;
    private static final double JET_BACK_OFFSET = 0.68D;
    private static final double MODEL_ORIGIN_HEIGHT = 1.501D;

    private static final long MAX_SAMPLE_AGE_TICKS = 3L;
    private static final long MAX_CORE_SAMPLE_AGE_TICKS = 3L;

    private static final double MAX_JET_DISTANCE_SQR = 2.5D * 2.5D;

    private record JetSample(
            Vec3 left,
            Vec3 right,
            long gameTime
    ) {
    }

    private record CoreSample(
            Vec3 position,
            long gameTime
    ) {
    }

    private static final Map<UUID, JetSample> JET_SAMPLES =
            new ConcurrentHashMap<>();

    private static final Map<UUID, CoreSample> CORE_SAMPLES =
            new ConcurrentHashMap<>();

    private static final Set<UUID> ACTIVE_BOOSTERS =
            ConcurrentHashMap.newKeySet();

    public static void setBoosterActive(
            UUID playerUuid,
            boolean active
    ) {
        if (active) {
            ACTIVE_BOOSTERS.add(playerUuid);
        } else {
            ACTIVE_BOOSTERS.remove(playerUuid);
        }
    }

    public static void recordJetSample(
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

        Vec3 left = jetWorldPosition(
                pose,
                body,
                camera,
                JET_SIDE_OFFSET
        );

        Vec3 right = jetWorldPosition(
                pose,
                body,
                camera,
                -JET_SIDE_OFFSET
        );

        JET_SAMPLES.put(
                wearer.getUUID(),
                new JetSample(
                        left,
                        right,
                        wearer.level().getGameTime()
                )
        );
    }

    public static void recordCoreSample(
            Player wearer,
            PoseStack poseStack
    ) {
        if (wearer == null || poseStack == null) {
            return;
        }

        Minecraft minecraft = Minecraft.getInstance();

        if (minecraft.level == null) {
            return;
        }

        Camera camera = minecraft.gameRenderer.getMainCamera();

        Vector4f corePosition = new Vector4f(
                0.0F,
                0.0F,
                0.0F,
                1.0F
        );

        corePosition.mul(poseStack.last().pose());

        Vector3f relative = new Vector3f(
                corePosition.x(),
                corePosition.y(),
                corePosition.z()
        );

        relative.mul(RenderSystem.getInverseViewRotationMatrix());

        Vec3 cameraPosition = camera.getPosition();

        Vec3 worldPosition = new Vec3(
                cameraPosition.x + relative.x(),
                cameraPosition.y + relative.y(),
                cameraPosition.z + relative.z()
        );

        CORE_SAMPLES.put(
                wearer.getUUID(),
                new CoreSample(
                        worldPosition,
                        minecraft.level.getGameTime()
                )
        );
    }

    public static Vec3 getCorePosition(Player wearer) {
        if (wearer == null) {
            return null;
        }

        Minecraft minecraft = Minecraft.getInstance();

        if (minecraft.level == null) {
            return null;
        }

        CoreSample sample = CORE_SAMPLES.get(wearer.getUUID());

        if (sample == null) {
            return null;
        }

        long age = minecraft.level.getGameTime() - sample.gameTime();

        if (age < 0 || age > MAX_CORE_SAMPLE_AGE_TICKS) {
            return null;
        }

        return sample.position();
    }

    private static Vec3 jetWorldPosition(
            Matrix4fc pose,
            ModelPart body,
            Camera camera,
            double side
    ) {
        Vector3f point = new Vector3f(
                (float) side,
                (float) (MODEL_ORIGIN_HEIGHT - JET_HEIGHT_OFFSET),
                (float) JET_BACK_OFFSET
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

        Vec3 cameraPosition = camera.getPosition();

        return new Vec3(
                cameraPosition.x + relative.x,
                cameraPosition.y + relative.y,
                cameraPosition.z + relative.z
        );
    }

    @SubscribeEvent
    public static void onLoggingOut(
            ClientPlayerNetworkEvent.LoggingOut event
    ) {
        ACTIVE_BOOSTERS.clear();
        JET_SAMPLES.clear();
        CORE_SAMPLES.clear();
    }

    @SubscribeEvent
    public static void onClientTick(
            TickEvent.ClientTickEvent event
    ) {
        if (event.phase != TickEvent.Phase.END) {
            return;
        }

        Minecraft mc = Minecraft.getInstance();
        ClientLevel level = mc.level;

        if (level == null) {
            CORE_SAMPLES.clear();
            JET_SAMPLES.clear();
            return;
        }

        for (Player player : level.players()) {
            if (!ACTIVE_BOOSTERS.contains(player.getUUID())) {
                continue;
            }

            spawnBoosterParticles(
                    mc,
                    level,
                    player
            );
        }

        long currentTime = level.getGameTime();

        CORE_SAMPLES.entrySet().removeIf(entry ->
                currentTime - entry.getValue().gameTime()
                        > MAX_CORE_SAMPLE_AGE_TICKS
        );

        JET_SAMPLES.entrySet().removeIf(entry ->
                currentTime - entry.getValue().gameTime()
                        > MAX_SAMPLE_AGE_TICKS
        );
    }

    private static void spawnBoosterParticles(
            Minecraft mc,
            ClientLevel level,
            Player player
    ) {
        float yawRad = (float) Math.toRadians(player.yBodyRot);

        double backX = Math.sin(yawRad) * JET_BACK_OFFSET;
        double backZ = -Math.cos(yawRad) * JET_BACK_OFFSET;

        double sideX = Math.cos(yawRad) * JET_SIDE_OFFSET;
        double sideZ = Math.sin(yawRad) * JET_SIDE_OFFSET;

        Vec3 leftRest = new Vec3(
                backX + sideX,
                JET_HEIGHT_OFFSET,
                backZ + sideZ
        );

        Vec3 rightRest = new Vec3(
                backX - sideX,
                JET_HEIGHT_OFFSET,
                backZ - sideZ
        );

        JetSample sample = JET_SAMPLES.get(player.getUUID());

        boolean fresh = sample != null
                && level.getGameTime() - sample.gameTime()
                <= MAX_SAMPLE_AGE_TICKS
                && !(player == mc.player
                && mc.options.getCameraType().isFirstPerson())
                && sample.left().distanceToSqr(player.position())
                < MAX_JET_DISTANCE_SQR
                && sample.right().distanceToSqr(player.position())
                < MAX_JET_DISTANCE_SQR;

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

            if (fresh) {
                Vec3 trail = new Vec3(
                        baseX - player.getX(),
                        baseY - player.getY(),
                        baseZ - player.getZ()
                );

                Vec3 left = sample.left().add(trail);
                Vec3 right = sample.right().add(trail);

                spawnExhaustParticle(
                        level,
                        left.x,
                        left.y,
                        left.z
                );

                spawnExhaustParticle(
                        level,
                        right.x,
                        right.y,
                        right.z
                );
            } else {
                spawnExhaustParticle(
                        level,
                        baseX + leftRest.x,
                        baseY + leftRest.y,
                        baseZ + leftRest.z
                );

                spawnExhaustParticle(
                        level,
                        baseX + rightRest.x,
                        baseY + rightRest.y,
                        baseZ + rightRest.z
                );
            }
        }
    }

    private static void spawnExhaustParticle(
            ClientLevel level,
            double x,
            double y,
            double z
    ) {
        double spreadX = (level.random.nextDouble() - 0.5D) * 0.08D;
        double spreadZ = (level.random.nextDouble() - 0.5D) * 0.08D;

        level.addParticle(
                ROACWParticleRegistry.BOOSTER_EXHAUST.get(),
                x,
                y,
                z,
                spreadX,
                -0.15D,
                spreadZ
        );
    }
}