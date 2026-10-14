package net.kamaarion.roacw.client.firevisuals;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import org.joml.Matrix4f;
import org.lwjgl.opengl.GL11;

public final class CustomFireScreenRenderer {

    private CustomFireScreenRenderer() {
    }

    public static void render(Minecraft mc, PoseStack poseStack) {
        LocalPlayer player = mc.player;
        if (player == null || player.isSpectator()) {
            return;
        }

        CustomFireType type = CustomFireRegistry.getActive(player);
        if (type == null) {
            return;
        }
        if (!type.showWithVanillaFire() && player.isOnFire()) {
            return; // vanilla is already drawing its own flames
        }

        TextureAtlasSprite sprite = type.backSprite(); // vanilla uses fire_1 here
        float u0 = sprite.getU0();
        float u1 = sprite.getU1();
        float v0 = sprite.getV0();
        float v1 = sprite.getV1();
        float r = type.red();
        float g = type.green();
        float b = type.blue();
        float a = type.overlayAlpha();

        BufferBuilder buffer = Tesselator.getInstance().getBuilder();
        RenderSystem.setShader(GameRenderer::getPositionColorTexShader);
        RenderSystem.depthFunc(GL11.GL_ALWAYS); // draw over everything, including the hand
        RenderSystem.depthMask(false);
        RenderSystem.enableBlend();
        RenderSystem.setShaderTexture(0, sprite.atlasLocation());

        for (int i = 0; i < 2; i++) {
            poseStack.pushPose();
            poseStack.translate((float) (-(i * 2 - 1)) * 0.24F, -0.3F, 0.0F);
            poseStack.mulPose(Axis.YP.rotationDegrees((float) (i * 2 - 1) * 10.0F));
            Matrix4f matrix = poseStack.last().pose();

            buffer.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR_TEX);
            buffer.vertex(matrix, -0.5F, -0.5F, -0.5F).color(r, g, b, a).uv(u1, v1).endVertex();
            buffer.vertex(matrix, 0.5F, -0.5F, -0.5F).color(r, g, b, a).uv(u0, v1).endVertex();
            buffer.vertex(matrix, 0.5F, 0.5F, -0.5F).color(r, g, b, a).uv(u0, v0).endVertex();
            buffer.vertex(matrix, -0.5F, 0.5F, -0.5F).color(r, g, b, a).uv(u1, v0).endVertex();
            BufferUploader.drawWithShader(buffer.end());

            poseStack.popPose();
        }

        RenderSystem.disableBlend();
        RenderSystem.depthMask(true);
        RenderSystem.depthFunc(GL11.GL_LEQUAL);
    }
}