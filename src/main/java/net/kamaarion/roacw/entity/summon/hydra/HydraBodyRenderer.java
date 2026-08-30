package net.kamaarion.roacw.entity.summon.hydra;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

@OnlyIn(Dist.CLIENT)
public class HydraBodyRenderer extends GeoEntityRenderer<HydraBodyEntity> {

    public HydraBodyRenderer(EntityRendererProvider.Context context) {
        super(context, new HydraBodyModel());
        this.shadowRadius = 0.8f;
    }

    @Override
    public void render(HydraBodyEntity entity, float entityYaw, float partialTick, PoseStack poseStack,
                       MultiBufferSource bufferSource, int packedLight) {
        renderChains(entity, partialTick, poseStack, bufferSource);
        super.render(entity, entityYaw, partialTick, poseStack, bufferSource, packedLight);
    }

    private void renderChains(HydraBodyEntity entity, float partialTick, PoseStack poseStack, MultiBufferSource bufferSource) {
        var heads = entity.getHeads();
        if (heads.isEmpty()) {
            return;
        }
        Vec3 renderOrigin = entity.getPosition(partialTick);
        Vec3 anchor = entity.getChainAnchor(partialTick).subtract(renderOrigin);

        for (HydraHead head : heads) {
            Vec3 headPos = head.getPosition(partialTick)
                    .add(0, head.getBbHeight() * 0.5, 0)
                    .subtract(renderOrigin);
            HydraChainRender.renderChainBetween(anchor, headPos, poseStack, bufferSource);
        }
    }
}
