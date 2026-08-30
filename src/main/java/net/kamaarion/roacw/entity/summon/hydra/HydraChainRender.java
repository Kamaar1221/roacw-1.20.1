package net.kamaarion.roacw.entity.summon.hydra;

import com.mojang.blaze3d.vertex.PoseStack;
import io.redspace.ironsspellbooks.IronsSpellbooks;
import io.redspace.ironsspellbooks.render.RenderHelper;
import io.redspace.ironsspellbooks.render.SpellRenderingHelper;
import net.kamaarion.roacw.ROACW;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.joml.Quaternionf;
import org.joml.Vector3f;

@OnlyIn(Dist.CLIENT)
public class HydraChainRender {

    public static final ResourceLocation HYDRA_CHAIN_TEXTURE = ResourceLocation.fromNamespaceAndPath(ROACW.MODID, "textures/entity/summon/hydra_chain.png");

    private HydraChainRender() {
    }

    public static void renderChainBetween(Vec3 start, Vec3 to, PoseStack poseStack, MultiBufferSource bufferSource) {
        renderChainBetween(start.subtract(0,0.6,0), to, poseStack, bufferSource, HYDRA_CHAIN_TEXTURE, 1f);
    }

    public static void renderChainBetween(Vec3 start, Vec3 to, PoseStack poseStack, MultiBufferSource bufferSource,
                                          ResourceLocation texture, float warmupPercent) {
        Vec3 delta = to.subtract(start);
        float distance = (float) delta.length();
        if (distance < 1.0E-4f) {
            return;
        }

        poseStack.pushPose();
        poseStack.translate(start.x, start.y, start.z);

        var consumer = bufferSource.getBuffer(RenderHelper.CustomerRenderType.magicNoCull(texture));

        Vec3 direction = delta.normalize();
        Quaternionf rotation = new Quaternionf().rotationTo(
                new Vector3f(0, 0, 1),
                new Vector3f((float) direction.x, (float) direction.y, (float) direction.z));
        poseStack.mulPose(rotation);

        PoseStack.Pose pose = poseStack.last();
        Vec3 origin = Vec3.ZERO;
        Vec3 destination = new Vec3(0, 0, distance);
        if (warmupPercent < 1f) {
            origin = destination.scale(1f - warmupPercent);
        }

        SpellRenderingHelper.drawQuad(origin, destination, 1, 0, pose, consumer, 255, 255, 255, 255, 0, distance);
        SpellRenderingHelper.drawQuad(origin, destination, 0, 1, pose, consumer, 255, 255, 255, 255, 0, distance);

        poseStack.popPose();
    }
}
