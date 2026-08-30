package net.kamaarion.roacw.entity.summon.dark_raven;

import com.mojang.blaze3d.vertex.PoseStack;
import net.kamaarion.roacw.client.entity.summon.DarkRavenModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

public class DarkRavenRenderer extends GeoEntityRenderer<DarkRavenEntity> {

    public DarkRavenRenderer(EntityRendererProvider.Context context) {
        super(context, new DarkRavenModel());
        this.shadowRadius = 0.4F;
    }

    @Override
    public void render(
            DarkRavenEntity entity,
            float entityYaw,
            float partialTick,
            PoseStack poseStack,
            MultiBufferSource bufferSource,
            int packedLight
    ) {
        poseStack.pushPose();

        // Lower only the visible raven model.
        // This does NOT move the entity's hitbox or perch position.
        poseStack.translate(0.0D, -0.25D, 0.0D);

        super.render(
                entity,
                entityYaw,
                partialTick,
                poseStack,
                bufferSource,
                packedLight
        );

        poseStack.popPose();
    }
}