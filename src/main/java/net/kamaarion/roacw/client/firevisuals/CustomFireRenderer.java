package net.kamaarion.roacw.client.firevisuals;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.kamaarion.roacw.ROACW;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderLivingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = ROACW.MODID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class CustomFireRenderer {

    private CustomFireRenderer() {
    }

    @SubscribeEvent
    public static void onRenderLiving(RenderLivingEvent.Post<?, ?> event) {
        LivingEntity entity = event.getEntity();

        // TEMP DEBUG: remove once mob flames are confirmed working.

        if (entity.isSpectator()) {
            return;
        }

        CustomFireType type = CustomFireRegistry.getActive(entity);
        if (type == null) {
            return;
        }
        if (!type.showWithVanillaFire() && entity.displayFireAnimation()) {
            return;
        }

        renderFlame(event.getPoseStack(), event.getMultiBufferSource(), entity, type);
    }

    private static void renderFlame(PoseStack poseStack, MultiBufferSource buffer, Entity entity, CustomFireType type) {
        TextureAtlasSprite sprite0 = type.frontSprite();
        TextureAtlasSprite sprite1 = type.backSprite();

        poseStack.pushPose();
        float scale = entity.getBbWidth() * type.worldScale();
        poseStack.scale(scale, scale, scale);

        float halfWidth = 0.5F;
        float yOffset = 0.0F;
        float remaining = entity.getBbHeight() / scale;
        float zOffset = 0.0F;

        float camYaw = Minecraft.getInstance().gameRenderer.getMainCamera().getYRot();
        poseStack.mulPose(Axis.YP.rotationDegrees(-camYaw));
        poseStack.translate(0.0F, 0.0F, -0.3F + (float) ((int) remaining) * 0.02F);

        VertexConsumer consumer = buffer.getBuffer(Sheets.cutoutBlockSheet());
        PoseStack.Pose pose = poseStack.last();

        for (int i = 0; remaining > 0.0F; i++) {
            TextureAtlasSprite sprite = (i % 2 == 0) ? sprite0 : sprite1;
            float u0 = sprite.getU0();
            float v0 = sprite.getV0();
            float u1 = sprite.getU1();
            float v1 = sprite.getV1();
            if ((i / 2) % 2 == 0) { // flip every other pair of layers
                float t = u1;
                u1 = u0;
                u0 = t;
            }

            vertex(pose, consumer, type, halfWidth, 0.0F - yOffset, zOffset, u1, v1);
            vertex(pose, consumer, type, -halfWidth, 0.0F - yOffset, zOffset, u0, v1);
            vertex(pose, consumer, type, -halfWidth, 1.4F - yOffset, zOffset, u0, v0);
            vertex(pose, consumer, type, halfWidth, 1.4F - yOffset, zOffset, u1, v0);

            remaining -= 0.45F;
            yOffset -= 0.45F;
            halfWidth *= 0.9F;
            zOffset += 0.03F;
        }

        poseStack.popPose();
    }

    private static void vertex(PoseStack.Pose pose, VertexConsumer consumer, CustomFireType type,
                               float x, float y, float z, float u, float v) {
        consumer.vertex(pose.pose(), x, y, z)
                .color(type.red(), type.green(), type.blue(), 1.0F)
                .uv(u, v)
                .overlayCoords(0, 10)
                .uv2(240) // fullbright
                .normal(pose.normal(), 0.0F, 1.0F, 0.0F)
                .endVertex();
    }
}