package net.kamaarion.roacw.items.armor.plaguebringerarmorset;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import software.bernie.geckolib.renderer.GeoArmorRenderer;

public class PlaguebringerArmorRenderer extends GeoArmorRenderer<PlaguebringerArmorItem> {

    public PlaguebringerArmorRenderer() {
        super(new PlaguebringerArmorModel());

        this.addRenderLayer(new PlaguebringerArmorLayer(this));
    }

    public RenderType getRenderType(
            PlaguebringerArmorItem animatable,
            ResourceLocation texture,
            MultiBufferSource bufferSource,
            float partialTick
    ) {
        return RenderType.entityTranslucent(this.getTextureLocation(animatable));
    }

    @Override
    public void renderToBuffer(PoseStack poseStack, VertexConsumer buffer, int packedLight,
                               int packedOverlay, float red, float green, float blue, float alpha) {

        if (this.currentEntity instanceof Player wearer && this.baseModel != null) {
            PlaguebringerJetBoostVisualEffects.recordCanisterSample(wearer, poseStack.last().pose(), this.baseModel.body);
        }

        super.renderToBuffer(poseStack, buffer, packedLight, packedOverlay, red, green, blue, alpha);
    }
}